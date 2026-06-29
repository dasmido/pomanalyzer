package dev.damido.pomanalyzer.parser.domain;

import java.util.List;
import java.util.Map;

public class PomMetadata {
    private String groupId;
    private String artifactId;
    private String version;
    private String name;
    private String description;
    private List<Dependency> dependencies;
    private Map<String, String> properties;
    private ParentMetadata parent;
    private ScmMetadata scm;
    private List<DeveloperMetadata> developers;
    private List<BuildPluginMetadata> buildPlugins;

    public PomMetadata() {
    }

    public PomMetadata(
        String groupId,
        String artifactId,
        String version,
        String name,
        String description,
        List<Dependency> dependencies,
        Map<String, String> properties,
        ParentMetadata parent,
        ScmMetadata scm,
        List<DeveloperMetadata> developers,
        List<BuildPluginMetadata> buildPlugins
    ) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.dependencies = dependencies;
        this.properties = properties;
        this.parent = parent;
        this.scm = scm;
        this.developers = developers;
        this.buildPlugins = buildPlugins;
    }

    public PomMetadata(
        String groupId,
        String artifactId,
        String version,
        String name,
        String description,
        List<Dependency> dependencies,
        Map<String, String> properties
    ) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.dependencies = dependencies;
        this.properties = properties;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public ParentMetadata getParent() {
        return parent;
    }

    public void setParent(ParentMetadata parent) {
        this.parent = parent;
    }

    public ScmMetadata getScm() {
        return scm;
    }

    public void setScm(ScmMetadata scm) {
        this.scm = scm;
    }

    public List<DeveloperMetadata> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<DeveloperMetadata> developers) {
        this.developers = developers;
    }

    public List<BuildPluginMetadata> getBuildPlugins() {
        return buildPlugins;
    }

    public void setBuildPlugins(List<BuildPluginMetadata> buildPlugins) {
        this.buildPlugins = buildPlugins;
    }
}