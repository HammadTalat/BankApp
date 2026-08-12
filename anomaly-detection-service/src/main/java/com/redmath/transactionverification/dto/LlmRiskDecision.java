package com.redmath.transactionverification.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LlmRiskDecision(
        @JsonPropertyDescription("True if transaction matches normal behavioral patterns, False if it deviates")
        boolean isBehaviorConsistent,

        @JsonPropertyDescription("Brief single-sentence explanation of why the transaction matches or deviates")
        String explanation
) {}