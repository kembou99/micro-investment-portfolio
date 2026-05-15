package com.van.micro_investment.dto.responses;

import com.van.micro_investment.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TransactionResponse(UUID id,
                                  UUID accountId,
                                  TransactionType type,
                                  BigDecimal amount,
                                  BigDecimal roundUpAmount,
                                  BigDecimal taxWithheld,
                                  BigDecimal netAmount,
                                  String description,
                                  Instant createdAt,
                                  Instant updatedAt) {
}
