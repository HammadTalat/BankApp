package com.redmath.redmathbankmcp.config;

import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;

import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.ServerRequest;

import tools.jackson.databind.json.JsonMapper;


@Configuration
public class McpTransportConfig {

    public static final String AUTHORIZATION_HEADER = "authorization";
    public static final String COOKIE_HEADER = "cookie";

    @Bean
    public WebMvcStreamableServerTransportProvider webMvcStreamableServerTransportProvider(
            @Qualifier("mcpServerJsonMapper") JsonMapper jsonMapper,
            McpServerStreamableHttpProperties serverProperties
    ) {
        return WebMvcStreamableServerTransportProvider.builder()
                .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
                .mcpEndpoint(serverProperties.getMcpEndpoint())
                .keepAliveInterval(serverProperties.getKeepAliveInterval())
                .disallowDelete(serverProperties.isDisallowDelete())
                .contextExtractor(this::extractAuthenticationHeaders)
                .build();
    }

    private McpTransportContext extractAuthenticationHeaders(ServerRequest request) {
        Map<String, Object> headers = new HashMap<>();
        addHeader(headers, AUTHORIZATION_HEADER, request.headers().firstHeader(HttpHeaders.AUTHORIZATION));
        addHeader(headers, COOKIE_HEADER, request.headers().firstHeader(HttpHeaders.COOKIE));
        return McpTransportContext.create(headers);
    }

    private void addHeader(Map<String, Object> headers, String name, String value) {
        if (StringUtils.hasText(value)) {
            headers.put(name, value);
        }
    }
}
