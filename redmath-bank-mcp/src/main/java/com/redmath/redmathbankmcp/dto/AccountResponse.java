package com.redmath.redmathbankmcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountResponse(
        String accountNumber,
        String status
) {
}
