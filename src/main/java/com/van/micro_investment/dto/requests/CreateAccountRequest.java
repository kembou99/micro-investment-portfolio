package com.van.micro_investment.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAccountRequest(@NotBlank(message = "The account name is required")
                                   @Size(min = 2, max = 255)
                                   String name,
                                   boolean isDefault,
                                   @NotNull(message = "The user ID is required")
                                   UUID userId) {

}
