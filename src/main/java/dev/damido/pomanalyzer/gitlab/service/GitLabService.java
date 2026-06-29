package dev.damido.pomanalyzer.gitlab.service;

import dev.damido.pomanalyzer.gitlab.domain.GitLabBranch;
import dev.damido.pomanalyzer.gitlab.domain.GitLabProject;
import dev.damido.pomanalyzer.gitlab.domain.GitLabTreeItem;
import dev.damido.pomanalyzer.gitlab.exception.GitLabApiException;
import dev.damido.pomanalyzer.parser.domain.PomMetadata;
import dev.damido.pomanalyzer.parser.services.PomParserService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GitLabService {

    private static final Logger logger = LoggerFactory.getLogger(GitLabService.class);

    private static final String GITLAB_API_V4 = "/api/v4";
    private static final int DEFAULT_PER_PAGE = 100;

    private final RestClient.Builder restClientBuilder;
    private final PomParserService pomParserService;

    public GitLabService(RestClient.Builder restClientBuilder, PomParserService pomParserService) {
        this.restClientBuilder = restClientBuilder;
        this.pomParserService = pomParserService;
    }

    /**
     * Search GitLab projects accessible to the authenticated user.
     *
     * @param gitlabUrl base URL, e.g. https://gitlab.com
     * @param token     personal access token
     * @param search    optional search keyword
     */
    public List<GitLabProject> searchProjects(String gitlabUrl, String token, String search) {
        logger.info("Searching GitLab projects on {} with query: {}", gitlabUrl, search);

        UriComponentsBuilder uri = UriComponentsBuilder
            .fromUriString(gitlabUrl + GITLAB_API_V4 + "/projects")
            .queryParam("membership", true)
            .queryParam("per_page", DEFAULT_PER_PAGE)
            .queryParam("order_by", "last_activity_at")
            .queryParam("sort", "desc");

        if (search != null && !search.isBlank()) {
            uri.queryParam("search", search);
        }

        return client(gitlabUrl, token)
            .get()
            .uri(uri.build().toUri())
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, resp) -> {
                throw new GitLabApiException("GitLab API error: " + resp.getStatusCode());
            })
            .body(new ParameterizedTypeReference<List<GitLabProject>>() {});
    }

    /**
     * List all branches for a project.
     */
    public List<GitLabBranch> getBranches(String gitlabUrl, String token, long projectId) {
        logger.info("Fetching branches for project {} on {}", projectId, gitlabUrl);

        URI uri = UriComponentsBuilder
            .fromUriString(gitlabUrl + GITLAB_API_V4 + "/projects/{id}/repository/branches")
            .queryParam("per_page", DEFAULT_PER_PAGE)
            .queryParam("order_by", "updated_at")
            .buildAndExpand(projectId)
            .toUri();

        return client(gitlabUrl, token)
            .get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, resp) -> {
                throw new GitLabApiException("GitLab API error: " + resp.getStatusCode());
            })
            .body(new ParameterizedTypeReference<List<GitLabBranch>>() {});
    }

    /**
     * Find all pom.xml files in the repository tree (recursive, up to 100 results per page,
     * first page only – covers the vast majority of real-world projects).
     */
    public List<GitLabTreeItem> findPomFiles(String gitlabUrl, String token, long projectId, String branch) {
        logger.info("Finding pom.xml files in project {} branch {} on {}", projectId, branch, gitlabUrl);

        List<GitLabTreeItem> allItems = new ArrayList<>();
        int page = 1;

        while (true) {
            URI uri = UriComponentsBuilder
                .fromUriString(gitlabUrl + GITLAB_API_V4 + "/projects/{id}/repository/tree")
                .queryParam("recursive", true)
                .queryParam("ref", branch)
                .queryParam("per_page", DEFAULT_PER_PAGE)
                .queryParam("page", page)
                .buildAndExpand(projectId)
                .toUri();

            List<GitLabTreeItem> page_items = client(gitlabUrl, token)
                .get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new GitLabApiException("GitLab API error fetching tree: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<List<GitLabTreeItem>>() {});

            if (page_items == null || page_items.isEmpty()) {
                break;
            }

            page_items.stream()
                .filter(item -> "blob".equals(item.getType()) && "pom.xml".equals(item.getName()))
                .forEach(allItems::add);

            if (page_items.size() < DEFAULT_PER_PAGE) {
                break; // last page
            }
            page++;
        }

        logger.info("Found {} pom.xml file(s) in project {}", allItems.size(), projectId);
        return allItems;
    }

    /**
     * Fetch raw pom.xml content and parse it into PomMetadata.
     */
    public PomMetadata parsePomFile(String gitlabUrl, String token, long projectId, String branch, String filePath) {
        logger.info("Parsing pom.xml from project {} branch {} path {} on {}", projectId, branch, filePath, gitlabUrl);

        // URL-encode the file path (slashes must be encoded as %2F)
        String encodedPath = filePath.replace("/", "%2F");

        URI uri = UriComponentsBuilder
            .fromUriString(gitlabUrl + GITLAB_API_V4 + "/projects/{id}/repository/files/{filePath}/raw")
            .queryParam("ref", branch)
            .buildAndExpand(projectId, encodedPath)
            .toUri();

        byte[] rawContent = client(gitlabUrl, token)
            .get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, resp) -> {
                throw new GitLabApiException("Could not fetch file from GitLab: " + resp.getStatusCode());
            })
            .body(byte[].class);

        if (rawContent == null || rawContent.length == 0) {
            throw new GitLabApiException("Empty file returned by GitLab for path: " + filePath);
        }

        try {
            return pomParserService.parsePomFromByteArray(rawContent);
        } catch (Exception e) {
            throw new GitLabApiException("Failed to parse pom.xml: " + e.getMessage());
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private RestClient client(String gitlabUrl, String token) {
        return restClientBuilder
            .clone()
            .baseUrl(gitlabUrl)
            .defaultHeader("PRIVATE-TOKEN", token)
            .defaultHeader("Accept", "application/json")
            .build();
    }
}
