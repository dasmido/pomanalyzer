package dev.damido.pomanalyzer.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabProject {

    private Long id;
    private String name;

    @JsonProperty("name_with_namespace")
    private String nameWithNamespace;

    @JsonProperty("path_with_namespace")
    private String pathWithNamespace;

    private String description;

    @JsonProperty("web_url")
    private String webUrl;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("last_activity_at")
    private String lastActivityAt;

    private String visibility;
}
