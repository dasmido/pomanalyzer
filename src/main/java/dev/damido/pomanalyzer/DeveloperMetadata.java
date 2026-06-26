package dev.damido.pomanalyzer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperMetadata {
    private String id;
    private String name;
    private String email;
    private String url;
    private String organization;
}
