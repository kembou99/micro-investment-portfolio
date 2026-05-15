package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorResponse(int statusCode,
                            Date timestamp,
                            String message,
                            String description) {
}
