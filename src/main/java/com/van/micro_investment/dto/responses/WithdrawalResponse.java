package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record WithdrawalResponse(
        UUID transactionId,
        BigDecimal grossAmount,
        BigDecimal taxWithheld,
        BigDecimal netAmount
) {
}
