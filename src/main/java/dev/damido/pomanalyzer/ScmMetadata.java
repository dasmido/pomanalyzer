package dev.damido.pomanalyzer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScmMetadata {
    private String connection;
    private String developerConnection;
    private String tag;
    private String url;
}
