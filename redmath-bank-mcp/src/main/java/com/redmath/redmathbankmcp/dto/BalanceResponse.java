package com.redmath.redmathbankmcp.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BalanceResponse(
        BigDecimal amount
) {
}
