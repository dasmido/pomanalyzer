package dev.damido.pomanalyzer.gitlab;

import dev.damido.pomanalyzer.PomMetadata;
import dev.damido.pomanalyzer.api.ApiError;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    private static final Logger logger = LoggerFactory.getLogger(GitLabController.class);

    private final GitLabService gitLabService;

    public GitLabController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    /**
     * Search/list GitLab projects accessible by the token.
     * GET /api/gitlab/projects?gitlabUrl=https://gitlab.com&search=myproject
     */
    @GetMapping("/projects")
    public ResponseEntity<?> searchProjects(
        @RequestParam String gitlabUrl,
        @RequestHeader("X-GitLab-Token") String token,
        @RequestParam(required = false) String search
    ) {
        logger.info("GET /api/gitlab/projects - gitlabUrl={} search={}", gitlabUrl, search);
        try {
            List<GitLabProject> projects = gitLabService.searchProjects(gitlabUrl, token, search);
            return ResponseEntity.ok(projects);
        } catch (GitLabApiException e) {
            logger.warn("GET /api/gitlab/projects - {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            logger.error("GET /api/gitlab/projects - unexpected error", e);
            return ResponseEntity.internalServerError().body(new ApiError("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * List branches for a project.
     * GET /api/gitlab/projects/{projectId}/branches?gitlabUrl=https://gitlab.com
     */
    @GetMapping("/projects/{projectId}/branches")
    public ResponseEntity<?> getBranches(
        @PathVariable long projectId,
        @RequestParam String gitlabUrl,
        @RequestHeader("X-GitLab-Token") String token
    ) {
        logger.info("GET /api/gitlab/projects/{}/branches - gitlabUrl={}", projectId, gitlabUrl);
        try {
            List<GitLabBranch> branches = gitLabService.getBranches(gitlabUrl, token, projectId);
            return ResponseEntity.ok(branches);
        } catch (GitLabApiException e) {
            logger.warn("GET /api/gitlab/projects/{}/branches - {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            logger.error("GET /api/gitlab/projects/{}/branches - unexpected error", projectId, e);
            return ResponseEntity.internalServerError().body(new ApiError("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Find all pom.xml files in a project's repository tree.
     * GET /api/gitlab/projects/{projectId}/poms?gitlabUrl=https://gitlab.com&branch=main
     */
    @GetMapping("/projects/{projectId}/poms")
    public ResponseEntity<?> findPomFiles(
        @PathVariable long projectId,
        @RequestParam String gitlabUrl,
        @RequestParam String branch,
        @RequestHeader("X-GitLab-Token") String token
    ) {
        logger.info("GET /api/gitlab/projects/{}/poms - gitlabUrl={} branch={}", projectId, gitlabUrl, branch);
        try {
            List<GitLabTreeItem> poms = gitLabService.findPomFiles(gitlabUrl, token, projectId, branch);
            return ResponseEntity.ok(poms);
        } catch (GitLabApiException e) {
            logger.warn("GET /api/gitlab/projects/{}/poms - {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            logger.error("GET /api/gitlab/projects/{}/poms - unexpected error", projectId, e);
            return ResponseEntity.internalServerError().body(new ApiError("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Fetch and parse a specific pom.xml from the repository.
     * GET /api/gitlab/projects/{projectId}/pom/parse?gitlabUrl=...&branch=main&filePath=pom.xml
     */
    @GetMapping("/projects/{projectId}/pom/parse")
    public ResponseEntity<?> parsePomFile(
        @PathVariable long projectId,
        @RequestParam String gitlabUrl,
        @RequestParam String branch,
        @RequestParam String filePath,
        @RequestHeader("X-GitLab-Token") String token
    ) {
        logger.info("GET /api/gitlab/projects/{}/pom/parse - gitlabUrl={} branch={} filePath={}", projectId, gitlabUrl, branch, filePath);
        try {
            PomMetadata metadata = gitLabService.parsePomFile(gitlabUrl, token, projectId, branch, filePath);
            return ResponseEntity.ok(metadata);
        } catch (GitLabApiException e) {
            logger.warn("GET /api/gitlab/projects/{}/pom/parse - {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            logger.error("GET /api/gitlab/projects/{}/pom/parse - unexpected error", projectId, e);
            return ResponseEntity.internalServerError().body(new ApiError("Unexpected error: " + e.getMessage()));
        }
    }
}
