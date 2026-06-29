package dev.damido.pomanalyzer.parser.services;

import dev.damido.pomanalyzer.parser.domain.BuildPluginMetadata;
import dev.damido.pomanalyzer.parser.domain.Dependency;
import dev.damido.pomanalyzer.parser.domain.DeveloperMetadata;
import dev.damido.pomanalyzer.parser.domain.ParentMetadata;
import dev.damido.pomanalyzer.parser.domain.PomMetadata;
import dev.damido.pomanalyzer.parser.domain.ScmMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

@Service
public class PomParserService {

    public PomMetadata parsePomFile(File pomFile) throws Exception {
        Document document = parseDocument(pomFile);
        return extractPomMetadata(document);
    }

    public PomMetadata parsePomFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try (InputStream inputStream = connection.getInputStream()) {
            byte[] payload = inputStream.readAllBytes();
            assertLikelyPomXml(payload, connection.getContentType());
            Document document = parseDocument(new ByteArrayInputStream(payload));
            return extractPomMetadata(document);
        }
    }

    public PomMetadata parsePomFromByteArray(byte[] pomContent) throws Exception {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pomContent)) {
            Document document = parseDocument(byteArrayInputStream);
            return extractPomMetadata(document);
        }
    }

    public PomMetadata parsePomFromInputStream(InputStream inputStream) throws Exception {
        Document document = parseDocument(inputStream);
        return extractPomMetadata(document);
    }

    private Document parseDocument(File file) throws Exception {
        DocumentBuilder builder = createSafeDocumentBuilder();
        return builder.parse(file);
    }

    private Document parseDocument(InputStream inputStream) throws Exception {
        DocumentBuilder builder = createSafeDocumentBuilder();
        return builder.parse(inputStream);
    }

    private DocumentBuilder createSafeDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Secure parser settings to prevent XXE and entity expansion attacks.
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // Allow DOCTYPE so valid remote XML doesn't fail upfront, but keep external entities disabled.
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        // Reject all external entities/DTDs even if declared in DOCTYPE.
        EntityResolver blockingResolver = (publicId, systemId) -> new InputSource(new java.io.StringReader(""));
        builder.setEntityResolver(blockingResolver);
        return builder;
    }

    private PomMetadata extractPomMetadata(Document document) {
        Element projectElement = document.getDocumentElement();
        if (projectElement == null || !"project".equals(localName(projectElement))) {
            throw new IllegalArgumentException("XML root element is not <project>.");
        }

        PomMetadata metadata = new PomMetadata();
        metadata.setGroupId(resolveProjectText(projectElement, "groupId"));
        metadata.setArtifactId(getDirectChildText(projectElement, "artifactId"));
        metadata.setVersion(resolveProjectText(projectElement, "version"));
        metadata.setName(getDirectChildText(projectElement, "name"));
        metadata.setDescription(getDirectChildText(projectElement, "description"));
        metadata.setDependencies(extractDependencies(projectElement));
        metadata.setProperties(extractProperties(projectElement));
        metadata.setParent(extractParent(projectElement));
        metadata.setScm(extractScm(projectElement));
        metadata.setDevelopers(extractDevelopers(projectElement));
        metadata.setBuildPlugins(extractBuildPlugins(projectElement));
        return metadata;
    }

    private ParentMetadata extractParent(Element projectElement) {
        Element parentElement = getDirectChildElement(projectElement, "parent");
        if (parentElement == null) {
            return null;
        }

        return new ParentMetadata(
            getDirectChildText(parentElement, "groupId"),
            getDirectChildText(parentElement, "artifactId"),
            getDirectChildText(parentElement, "version"),
            getDirectChildText(parentElement, "relativePath")
        );
    }

    private ScmMetadata extractScm(Element projectElement) {
        Element scmElement = getDirectChildElement(projectElement, "scm");
        if (scmElement == null) {
            return null;
        }

        return new ScmMetadata(
            getDirectChildText(scmElement, "connection"),
            getDirectChildText(scmElement, "developerConnection"),
            getDirectChildText(scmElement, "tag"),
            getDirectChildText(scmElement, "url")
        );
    }

    private List<DeveloperMetadata> extractDevelopers(Element projectElement) {
        List<DeveloperMetadata> developers = new ArrayList<>();
        Element developersElement = getDirectChildElement(projectElement, "developers");
        if (developersElement == null) {
            return developers;
        }

        List<Element> developerElements = getDirectChildElements(developersElement, "developer");
        for (Element developerElement : developerElements) {
            developers.add(new DeveloperMetadata(
                getDirectChildText(developerElement, "id"),
                getDirectChildText(developerElement, "name"),
                getDirectChildText(developerElement, "email"),
                getDirectChildText(developerElement, "url"),
                getDirectChildText(developerElement, "organization")
            ));
        }

        return developers;
    }

    private List<BuildPluginMetadata> extractBuildPlugins(Element projectElement) {
        List<BuildPluginMetadata> plugins = new ArrayList<>();
        Element buildElement = getDirectChildElement(projectElement, "build");
        if (buildElement == null) {
            return plugins;
        }

        Element pluginsElement = getDirectChildElement(buildElement, "plugins");
        if (pluginsElement == null) {
            return plugins;
        }

        List<Element> pluginElements = getDirectChildElements(pluginsElement, "plugin");
        for (Element pluginElement : pluginElements) {
            plugins.add(new BuildPluginMetadata(
                getDirectChildText(pluginElement, "groupId"),
                getDirectChildText(pluginElement, "artifactId"),
                getDirectChildText(pluginElement, "version")
            ));
        }

        return plugins;
    }

    private String resolveProjectText(Element projectElement, String tagName) {
        String value = getDirectChildText(projectElement, tagName);
        if (!value.isBlank()) {
            return value;
        }

        Element parentElement = getDirectChildElement(projectElement, "parent");
        if (parentElement == null) {
            return "";
        }

        return getDirectChildText(parentElement, tagName);
    }

    private List<Dependency> extractDependencies(Element projectElement) {
        List<Dependency> dependencies = new ArrayList<>();
        Element dependenciesElement = getDirectChildElement(projectElement, "dependencies");
        if (dependenciesElement == null) {
            return dependencies;
        }

        List<Element> dependencyElements = getDirectChildElements(dependenciesElement, "dependency");
        for (Element dependencyElement : dependencyElements) {
            Dependency dependency = new Dependency();
            dependency.setGroupId(getDirectChildText(dependencyElement, "groupId"));
            dependency.setArtifactId(getDirectChildText(dependencyElement, "artifactId"));
            dependency.setVersion(getDirectChildText(dependencyElement, "version"));
            dependency.setScope(getDirectChildText(dependencyElement, "scope"));
            dependencies.add(dependency);
        }

        return dependencies;
    }

    private Map<String, String> extractProperties(Element projectElement) {
        Map<String, String> properties = new HashMap<>();
        Element propertiesElement = getDirectChildElement(projectElement, "properties");
        if (propertiesElement == null) {
            return properties;
        }

        NodeList children = propertiesElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            properties.put(localName(element), textOrEmpty(element.getTextContent()));
        }

        return properties;
    }

    private Element getDirectChildElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element) node;
            if (tagName.equals(localName(child))) {
                return child;
            }
        }
        return null;
    }

    private List<Element> getDirectChildElements(Element parent, String tagName) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element) node;
            if (tagName.equals(localName(child))) {
                elements.add(child);
            }
        }
        return elements;
    }

    private String getDirectChildText(Element parent, String tagName) {
        Element element = getDirectChildElement(parent, tagName);
        if (element == null) {
            return "";
        }
        return textOrEmpty(element.getTextContent());
    }

    private String localName(Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void assertLikelyPomXml(byte[] payload, String contentType) {
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (normalizedContentType.contains("text/html")) {
            throw new IllegalArgumentException("URL did not return a Maven pom.xml. Use a raw pom.xml URL.");
        }

        String preview = new String(payload, 0, Math.min(payload.length, 4096), StandardCharsets.UTF_8)
            .toLowerCase(Locale.ROOT);
        if (!preview.contains("<project")) {
            throw new IllegalArgumentException("URL did not return a Maven pom.xml. Use a raw pom.xml URL.");
        }
    }
}