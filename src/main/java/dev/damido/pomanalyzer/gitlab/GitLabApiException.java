package dev.damido.pomanalyzer.gitlab;

public class GitLabApiException extends RuntimeException {
    public GitLabApiException(String message) {
        super(message);
    }
}
