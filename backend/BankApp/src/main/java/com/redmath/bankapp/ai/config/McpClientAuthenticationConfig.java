package com.redmath.bankapp.ai.config;

import java.net.http.HttpRequest;
import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.common.McpTransportContext;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

@Configuration
public class McpClientAuthenticationConfig {

  static final String AUTHORIZATION_CONTEXT_KEY = "authorization";

  @Bean
  McpClientCustomizer<McpClient.SyncSpec> authenticatedMcpTransportContext() {
    return (connectionName, client) -> client.transportContextProvider(() -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)
          || !StringUtils.hasText(jwt.getTokenValue())) {
        return McpTransportContext.EMPTY;
      }

      return McpTransportContext.create(Map.of(
          AUTHORIZATION_CONTEXT_KEY,
          "Bearer " + jwt.getTokenValue()));
    });
  }

  @Bean
  McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> authenticatedMcpHttpRequests() {
    return (connectionName, transport) -> transport.httpRequestCustomizer(
        (HttpRequest.Builder request, String method, java.net.URI endpoint, String body,
            McpTransportContext context) -> {
          Object authorization = context.get(AUTHORIZATION_CONTEXT_KEY);
          if (authorization instanceof String value && StringUtils.hasText(value)) {
            request.header(HttpHeaders.AUTHORIZATION, value);
          }
        });
  }
}
