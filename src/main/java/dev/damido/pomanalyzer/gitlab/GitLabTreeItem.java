package dev.damido.pomanalyzer.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabTreeItem {

    private String id;
    private String name;
    private String type; // "blob" or "tree"
    private String path;
    private String mode;
}
