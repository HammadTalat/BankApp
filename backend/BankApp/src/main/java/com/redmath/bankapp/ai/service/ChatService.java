package com.redmath.bankapp.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatClient chatClient;

  public String getResponse(String userMessage, Jwt jwt) {
    String conversationId = extractUserId(jwt);

    return chatClient
        .prompt()
        .advisors(advisorSpec -> advisorSpec.param("chat_memory_conversation_id", conversationId))
        .user(userMessage)
        .call()
        .content();
  }

  private String extractUserId(Jwt jwt) {
    Object userIdClaim = jwt.getClaims().get("userId");

    if (userIdClaim instanceof Number number) {
      return String.valueOf(number.longValue());
    }

    throw new IllegalStateException("JWT does not contain a valid userId claim");
  }
}
