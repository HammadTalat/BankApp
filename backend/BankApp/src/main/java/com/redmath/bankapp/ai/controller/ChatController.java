package com.redmath.bankapp.ai.controller;

import com.redmath.bankapp.ai.dto.ChatRequest;
import com.redmath.bankapp.ai.dto.ChatResponse;
import com.redmath.bankapp.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/chat")
@PreAuthorize("hasRole('ACCOUNT_HOLDER')")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping
  public ResponseEntity<ChatResponse> chat(
      @Valid  @RequestBody ChatRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    String response = chatService.getResponse(request.message(), jwt);
    return ResponseEntity.ok(new ChatResponse(response));
  }
}
