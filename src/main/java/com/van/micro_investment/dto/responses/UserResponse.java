package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Instant createdAt,
        Instant updatedAt
) {
}
