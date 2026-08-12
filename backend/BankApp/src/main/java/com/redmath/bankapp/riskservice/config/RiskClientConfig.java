package com.redmath.bankapp.riskservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RiskClientConfig {

    @Value("${services.anomaly-detection.url}")
    private String riskServiceBaseUrl;

    @Bean
    public RestClient riskEvaluatorRestClient() {
        return RestClient.builder()
                .baseUrl(riskServiceBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
