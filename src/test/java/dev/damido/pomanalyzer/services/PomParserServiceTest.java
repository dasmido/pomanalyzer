package dev.damido.pomanalyzer.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.damido.pomanalyzer.parser.domain.PomMetadata;
import dev.damido.pomanalyzer.parser.services.PomParserService;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PomParserServiceTest {

    private final PomParserService pomParserService = new PomParserService();

    @Test
    void parsePomFromByteArray_extractsMetadataAndDependencies() throws Exception {
        String pom = """
            <?xml version=\"1.0\" encoding=\"UTF-8\"?>
            <project xmlns=\"http://maven.apache.org/POM/4.0.0\"
                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
                     xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
                <modelVersion>4.0.0</modelVersion>
                <parent>
                    <groupId>org.example</groupId>
                    <artifactId>parent-app</artifactId>
                    <version>1.0.0</version>
                    <relativePath>../pom.xml</relativePath>
                </parent>
                <artifactId>sample-app</artifactId>
                <scm>
                    <connection>scm:git:git://example.com/repo.git</connection>
                    <developerConnection>scm:git:ssh://example.com/repo.git</developerConnection>
                    <tag>main</tag>
                    <url>https://example.com/repo</url>
                </scm>
                <developers>
                    <developer>
                        <id>mjamal</id>
                        <name>Mohammed Jamal</name>
                        <email>dev@example.com</email>
                        <url>https://example.com/dev</url>
                        <organization>Damido</organization>
                    </developer>
                </developers>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                        <scope>compile</scope>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                            <version>4.1.0</version>
                        </plugin>
                    </plugins>
                </build>
                <properties>
                    <java.version>21</java.version>
                </properties>
            </project>
            """;

        PomMetadata metadata = pomParserService.parsePomFromByteArray(pom.getBytes(StandardCharsets.UTF_8));

        assertNotNull(metadata);
        assertEquals("org.example", metadata.getGroupId());
        assertEquals("sample-app", metadata.getArtifactId());
        assertEquals("1.0.0", metadata.getVersion());
        assertFalse(metadata.getDependencies().isEmpty());
        assertEquals("org.springframework.boot", metadata.getDependencies().get(0).getGroupId());
        assertEquals("21", metadata.getProperties().get("java.version"));
        assertNotNull(metadata.getParent());
        assertEquals("../pom.xml", metadata.getParent().getRelativePath());
        assertNotNull(metadata.getScm());
        assertEquals("main", metadata.getScm().getTag());
        assertEquals(1, metadata.getDevelopers().size());
        assertEquals("mjamal", metadata.getDevelopers().get(0).getId());
        assertEquals(1, metadata.getBuildPlugins().size());
        assertEquals("spring-boot-maven-plugin", metadata.getBuildPlugins().get(0).getArtifactId());
    }

    @Test
    void parsePomFromByteArray_allowsDoctypeAndParsesProject() throws Exception {
        String pom = """
            <!DOCTYPE project SYSTEM \"https://maven.apache.org/xsd/maven-4.0.0.xsd\">
            <project xmlns=\"http://maven.apache.org/POM/4.0.0\"
                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
                     xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>doctype-ok</artifactId>
                <version>1.2.3</version>
            </project>
            """;

        PomMetadata metadata = pomParserService.parsePomFromByteArray(pom.getBytes(StandardCharsets.UTF_8));

        assertEquals("com.example", metadata.getGroupId());
        assertEquals("doctype-ok", metadata.getArtifactId());
        assertEquals("1.2.3", metadata.getVersion());
    }
}