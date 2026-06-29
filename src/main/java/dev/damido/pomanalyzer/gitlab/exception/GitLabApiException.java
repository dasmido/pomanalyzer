package dev.damido.pomanalyzer.gitlab.exception;

public class GitLabApiException extends RuntimeException {
    public GitLabApiException(String message) {
        super(message);
    }
}
