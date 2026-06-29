package dev.damido.pomanalyzer.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.damido.pomanalyzer.parser.controller.PomParserController;
import dev.damido.pomanalyzer.parser.domain.Dependency;
import dev.damido.pomanalyzer.parser.domain.PomMetadata;
import dev.damido.pomanalyzer.parser.services.PomParserService;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PomParserControllerTest {

    @Mock
    private PomParserService pomParserService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PomParserController controller = new PomParserController(pomParserService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void parsePomFile_returnsJsonMetadata() throws Exception {
        PomMetadata metadata = new PomMetadata(
            "dev.example",
            "demo",
            "1.0.0",
            "Demo",
            "Demo app",
            List.of(new Dependency("org.slf4j", "slf4j-api", "2.0.17", "compile")),
            Map.of("java.version", "21")
        );

        when(pomParserService.parsePomFromInputStream(any())).thenReturn(metadata);

        MockMultipartFile pomFile = new MockMultipartFile(
            "file",
            "pom.xml",
            "application/xml",
            "<project/>".getBytes()
        );

        mockMvc.perform(multipart("/api/pom/parse").file(pomFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groupId").value("dev.example"))
            .andExpect(jsonPath("$.artifactId").value("demo"))
            .andExpect(jsonPath("$.dependencies[0].artifactId").value("slf4j-api"));
    }

    @Test
    void parsePomFromUrl_returnsBadRequestForInvalidUrl() throws Exception {
        mockMvc.perform(get("/api/pom/parse-url").param("url", "ht!tp://bad"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid URL."));
    }
}