package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.util.Date;
import java.util.Map;

@Builder
public record ValidationErrorResponse(int statusCode,
                                      String error,
                                      Map<String, String> validationErrors,
                                      Date timestamp) {
}
