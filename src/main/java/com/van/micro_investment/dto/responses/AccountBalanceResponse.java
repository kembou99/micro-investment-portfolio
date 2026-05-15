package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record AccountBalanceResponse(BigDecimal balance) {
}
