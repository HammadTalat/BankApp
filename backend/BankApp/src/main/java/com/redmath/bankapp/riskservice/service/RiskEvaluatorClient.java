package com.redmath.bankapp.riskservice.service;


import com.redmath.bankapp.riskservice.dto.EvaluateRiskRequest;
import com.redmath.bankapp.riskservice.dto.EvaluateRiskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RiskEvaluatorClient {

    private static final Logger log = LoggerFactory.getLogger(RiskEvaluatorClient.class);
    private final RestClient restClient;

    public RiskEvaluatorClient(RestClient riskEvaluatorRestClient) {
        this.restClient = riskEvaluatorRestClient;
    }

    public EvaluateRiskResponse evaluateTransactionRisk(EvaluateRiskRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/risk/evaluate")
                    .body(request)
                    .retrieve()
                    .body(EvaluateRiskResponse.class);

        } catch (Exception e) {
            log.error("Failed to reach risk evaluator microservice: {}", e.getMessage());

            return new EvaluateRiskResponse(
                    false,
                    "SERVICE_UNAVAILABLE",
                    "Risk evaluation service is currently unavailable. Transaction flagged for manual review."
            );
        }
    }

}
