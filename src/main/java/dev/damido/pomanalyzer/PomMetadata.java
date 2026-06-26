package dev.damido.pomanalyzer;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
}