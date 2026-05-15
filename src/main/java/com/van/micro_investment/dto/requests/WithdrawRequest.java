package com.van.micro_investment.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawRequest(@NotNull(message = "The amount is required")
                              @DecimalMin(value = "0.01", message = "The amount must be greater than 0")
                              BigDecimal amount,
                              @NotNull(message = "The account ID is required")
                              UUID accountId) {
}
