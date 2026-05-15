package com.van.micro_investment.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank(message = "An email address is required")
                                @Email(message = "Invalid email format")
                                String email,

                                @NotBlank(message = "Your full name is required")
                                @Size(min = 2, max = 255)
                                String fullName) {
}
