package dev.damido.pomanalyzer.parser.domain;

public class DeveloperMetadata {
    private String id;
    private String name;
    private String email;
    private String url;
    private String organization;

    public DeveloperMetadata() {
    }

    public DeveloperMetadata(String id, String name, String email, String url, String organization) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.url = url;
        this.organization = organization;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
