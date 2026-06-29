package dev.damido.pomanalyzer.parser.domain;

public class ScmMetadata {
    private String connection;
    private String developerConnection;
    private String tag;
    private String url;

    public ScmMetadata() {
    }

    public ScmMetadata(String connection, String developerConnection, String tag, String url) {
        this.connection = connection;
        this.developerConnection = developerConnection;
        this.tag = tag;
        this.url = url;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getDeveloperConnection() {
        return developerConnection;
    }

    public void setDeveloperConnection(String developerConnection) {
        this.developerConnection = developerConnection;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
