package dev.damido.pomanalyzer.gitlab.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabBranch {

    private String name;
    private boolean merged;
    private boolean protected_;

    @JsonProperty("default")
    private boolean defaultBranch;

    @JsonProperty("can_push")
    private boolean canPush;
}
