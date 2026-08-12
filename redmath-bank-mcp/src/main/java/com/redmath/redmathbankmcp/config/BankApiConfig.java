package com.redmath.redmathbankmcp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BankApiConfig {

    @Bean
    public RestClient bankRestClient(
            RestClient.Builder builder,
            @Value("${bank.api.base-url}")
            String bankApiBaseUrl
    ) {

        return builder
                .baseUrl(bankApiBaseUrl)
                .build();
    }
}