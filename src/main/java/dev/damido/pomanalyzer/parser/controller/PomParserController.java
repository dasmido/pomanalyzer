package dev.damido.pomanalyzer.parser.controller;

import dev.damido.pomanalyzer.parser.domain.PomMetadata;
import dev.damido.pomanalyzer.parser.services.PomParserService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pom")
public class PomParserController {

    private static final Logger logger = LoggerFactory.getLogger(PomParserController.class);

    private final PomParserService pomParserService;

    public PomParserController(PomParserService pomParserService) {
        this.pomParserService = pomParserService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("GET /api/pom/health - Health check requested");
        return ResponseEntity.ok("ok");
    }

    @PostMapping(path = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> parsePomFile(@RequestParam("file") MultipartFile file) {
        logger.info("POST /api/pom/parse - Parse pom from file requested, filename: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            logger.warn("POST /api/pom/parse - Uploaded file is empty");
            return ResponseEntity.badRequest().body(new ApiError("Uploaded file is empty."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            logger.warn("POST /api/pom/parse - Invalid file type: {}", filename);
            return ResponseEntity.badRequest().body(new ApiError("Only .xml files are supported."));
        }

        try {
            PomMetadata metadata = pomParserService.parsePomFromInputStream(file.getInputStream());
            logger.info("POST /api/pom/parse - Successfully parsed pom file: {}", filename);
            return ResponseEntity.ok(metadata);
        } catch (Exception exception) {
            logger.error("POST /api/pom/parse - Failed to parse pom file: {}", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Unable to parse pom.xml: " + exception.getMessage()));
        }
    }

    @GetMapping("/parse-url")
    public ResponseEntity<?> parsePomFromUrl(@RequestParam("url") String url) {
        logger.info("GET /api/pom/parse-url - Parse pom from URL requested, url: {}", url);

        if (url == null || url.isBlank()) {
            logger.warn("GET /api/pom/parse-url - URL parameter is missing or blank");
            return ResponseEntity.badRequest().body(new ApiError("url is required."));
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                logger.warn("GET /api/pom/parse-url - Invalid URL scheme: {}", scheme);
                return ResponseEntity.badRequest().body(new ApiError("Only http/https URLs are allowed."));
            }

            PomMetadata metadata = pomParserService.parsePomFromUrl(url);
            logger.info("GET /api/pom/parse-url - Successfully parsed pom from URL: {}", url);
            return ResponseEntity.ok(metadata);
        } catch (MalformedURLException malformedURLException) {
            logger.warn("GET /api/pom/parse-url - Malformed URL: {}", url);
            return ResponseEntity.badRequest().body(new ApiError("Invalid URL."));
        } catch (URISyntaxException uriSyntaxException) {
            logger.warn("GET /api/pom/parse-url - Invalid URI syntax: {}", url);
            return ResponseEntity.badRequest().body(new ApiError("Invalid URL."));
        } catch (Exception exception) {
            logger.error("GET /api/pom/parse-url - Failed to parse pom from URL: {}", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Unable to parse pom.xml from URL: " + exception.getMessage()));
        }
    }

    @PostMapping(path = "/parse-raw", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> parsePomRawXml(@RequestBody String xml) {
        logger.info("POST /api/pom/parse-raw - Parse pom from raw XML requested");

        if (xml == null || xml.isBlank()) {
            logger.warn("POST /api/pom/parse-raw - Request body is empty or blank");
            return ResponseEntity.badRequest().body(new ApiError("Request body must contain XML content."));
        }

        try {
            PomMetadata metadata = pomParserService.parsePomFromByteArray(xml.getBytes(StandardCharsets.UTF_8));
            logger.info("POST /api/pom/parse-raw - Successfully parsed raw XML");
            return ResponseEntity.ok(metadata);
        } catch (Exception exception) {
            logger.error("POST /api/pom/parse-raw - Failed to parse raw XML: {}", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Unable to parse raw XML: " + exception.getMessage()));
        }
    }
}