package com.redmath.transactionverification;

import com.redmath.transactionverification.dto.LlmRiskDecision;
import com.redmath.transactionverification.dto.TransactionEvaluationRequest;
import com.redmath.transactionverification.dto.TransactionEvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);
    private final ChatClient chatClient;

    public AnomalyDetectionService(ChatClient chatClient) {
        this.chatClient = chatClient;

    }

    public TransactionEvaluationResponse evaluateTransaction(TransactionEvaluationRequest request) {

        if (request.previousHistory() == null || request.previousHistory().isEmpty()) {
            return new TransactionEvaluationResponse(
                    true,
                    "ALLOWED",
                    "Transaction allowed: No prior historical records found for baseline comparison."
            );
        }

        String userPrompt = String.format("""
                        Compare the current transaction against the user's previous transaction history.
                        
                        CURRENT TRANSACTION ATTEMPT:
                        %s
                        
                        PREVIOUS TRANSACTION HISTORY:
                        %s
                        
                        Determine if the current transaction aligns with their historical behavior or indicates anomalous activity.
                        """,
                request.currentTransaction(),
                request.previousHistory()
        );

        try {
            // Spring AI automatically maps the JSON output directly to our Record
            LlmRiskDecision decision = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .entity(LlmRiskDecision.class);

            if (decision != null && decision.isBehaviorConsistent()) {
                return new TransactionEvaluationResponse(
                        true,
                        "ALLOWED",
                        "Transaction allowed: Behavior matches historical user patterns. " + decision.explanation()
                );
            } else {
                String explanation = (decision != null) ? decision.explanation() : "Pattern mismatch detected.";
                return new TransactionEvaluationResponse(
                        false,
                        "FLAGGED_MISMATCH",
                        "Transaction blocked: Current transaction behavior does not match previous activity history. Reason: " + explanation
                );
            }

        } catch (Exception e) {
            log.info(e.toString());
            // Fallback for LLM timeouts or structural parsing errors
            return new TransactionEvaluationResponse(
                    false,
                    "SYSTEM_CHALLENGE",
                    "Transaction flagged for verification due to evaluation timeout. Please complete step-up authentication."
            );
        }
    }


}
