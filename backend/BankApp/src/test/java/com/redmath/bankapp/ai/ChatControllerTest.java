package com.redmath.bankapp.ai;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import tools.jackson.databind.ObjectMapper;
import com.redmath.bankapp.ai.controller.ChatController;
import com.redmath.bankapp.ai.dto.ChatRequest;
import com.redmath.bankapp.ai.service.ChatService;
import com.redmath.bankapp.auth.security.ApiSecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc
@Import(ChatControllerTest.SecurityConfig.class)
class ChatControllerTest {

    @EnableMethodSecurity
    static class SecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private ApiSecurityService apiSecurityService;

    @Test
    @DisplayName("Should process chat request and return response when authenticated with ROLE_ACCOUNT_HOLDER")
    void chat_Success() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest("What is my current balance?");
        String mockAiResponse = "Your current account balance is $1,250.00.";

        when(chatService.getResponse(eq("What is my current balance?"), any(Jwt.class)))
                .thenReturn(mockAiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ACCOUNT_HOLDER"))
                                .jwt(jwt -> jwt.claim("sub", "user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(mockAiResponse));

        verify(chatService).getResponse(eq("What is my current balance?"), any(Jwt.class));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user lacks ROLE_ACCOUNT_HOLDER")
    void chat_ForbiddenWhenWrongRole() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest("Hello");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 3xx Redirection when unauthenticated")
    void chat_UnauthorizedWhenNoJwt() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest("Hello");

        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection());
    }
}