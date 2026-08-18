package com.redmath.bankapp.ai;

import com.redmath.bankapp.ai.controller.DocumentIngestionController;
import com.redmath.bankapp.ai.rag.PolicyDocumentIngester;
import com.redmath.bankapp.auth.security.ApiSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentIngestionController.class)
@AutoConfigureMockMvc
@Import({
        DocumentIngestionControllerTest.TestSecurityConfig.class,
        DocumentIngestionControllerTest.TestGlobalExceptionHandler.class
})
class DocumentIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiSecurityService apiSecurityService;

    @MockitoBean
    private PolicyDocumentIngester policyDocumentIngester;

    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
        public ResponseEntity<String> handleAccessDenied(Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
    }

    @Test
    @DisplayName("Should return 200 OK and message when user has ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void ingestPolicies_Success_WhenAdmin() throws Exception {
        // Arrange
        String expectedMessage = "Successfully ingested 5 policy documents";
        when(policyDocumentIngester.ingestDocuments()).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/documents/ingest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        verify(policyDocumentIngester).ingestDocuments();
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user has non-ADMIN role")
    @WithMockUser(roles = "USER")
    void ingestPolicies_Forbidden_WhenNotAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/documents/ingest")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyDocumentIngester);
    }

    @Test
    @DisplayName("Should return 3xx Redirection when unauthenticated")
    void ingestPolicies_Forbidden_WhenUnauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/documents/ingest")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(policyDocumentIngester);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when CSRF token is missing")
    @WithMockUser(roles = "ADMIN")
    void ingestPolicies_Forbidden_WhenCsrfMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/documents/ingest"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyDocumentIngester);
    }
}