package com.redmath.bankapp.ai;

import com.redmath.bankapp.ai.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.AdvisorSpec advisorSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private Jwt jwt;

    @Captor
    private ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> advisorConsumerCaptor;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatClient);
    }

    @Test
    @DisplayName("Should successfully return response and configure chat memory parameter when userId is a Long")
    void getResponse_Success_WithNumberClaim() {
        // Arrange
        String userMessage = "What is my balance?";
        String expectedResponse = "Your balance is $1,000.";
        Long userId = 101L;

        when(jwt.getClaims()).thenReturn(Map.of("userId", userId));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec); // Fix: match Consumer overload
        when(requestSpec.user(userMessage)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResponse);
        when(advisorSpec.param(anyString(), any())).thenReturn(advisorSpec);

        // Act
        String result = chatService.getResponse(userMessage, jwt);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);

        // Verify advisor Consumer parameter execution (Crucial for PITest mutation coverage)
        verify(requestSpec).advisors(advisorConsumerCaptor.capture());
        Consumer<ChatClient.AdvisorSpec> capturedConsumer = advisorConsumerCaptor.getValue();
        capturedConsumer.accept(advisorSpec);
        verify(advisorSpec).param("chat_memory_conversation_id", "101");
    }

    @Test
    @DisplayName("Should successfully extract conversation ID when userId is an Integer")
    void getResponse_Success_WithIntegerClaim() {
        // Arrange
        String userMessage = "Hello";
        String expectedResponse = "Hi there!";
        Integer userId = 42;

        when(jwt.getClaims()).thenReturn(Map.of("userId", userId));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.user(userMessage)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // Act
        String result = chatService.getResponse(userMessage, jwt);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);

        verify(requestSpec).advisors(advisorConsumerCaptor.capture());
        advisorConsumerCaptor.getValue().accept(advisorSpec);
        verify(advisorSpec).param("chat_memory_conversation_id", "42");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when userId claim is missing")
    void getResponse_ThrowsException_WhenUserIdClaimMissing() {
        // Arrange
        when(jwt.getClaims()).thenReturn(Collections.emptyMap());

        // Act & Assert
        assertThatThrownBy(() -> chatService.getResponse("Hello", jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT does not contain a valid userId claim");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when userId claim is a String instead of a Number")
    void getResponse_ThrowsException_WhenUserIdNotANumber() {
        // Arrange
        when(jwt.getClaims()).thenReturn(Map.of("userId", "101"));

        // Act & Assert
        assertThatThrownBy(() -> chatService.getResponse("Hello", jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT does not contain a valid userId claim");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when userId claim is null")
    void getResponse_ThrowsException_WhenUserIdIsNull() {
        // Arrange
        Map<String, Object> claims = Collections.singletonMap("userId", null);
        when(jwt.getClaims()).thenReturn(claims);

        // Act & Assert
        assertThatThrownBy(() -> chatService.getResponse("Hello", jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT does not contain a valid userId claim");
    }
}