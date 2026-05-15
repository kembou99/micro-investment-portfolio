package com.van.micro_investment.dto.responses;

import com.van.micro_investment.enums.AccountStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record AccountResponse(
                UUID id,
                UUID userId,
                String name,
                BigDecimal balance,
                boolean isDefault,
                AccountStatus status,
                Instant createdAt,
                Instant updatedAt) {
}
