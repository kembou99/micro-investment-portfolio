package com.van.micro_investment.dto.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginationResponse<T> (int page,
                                     int size,
                                 long totalElements,
                                 int totalPages,
                                 List<T> content) {
}
