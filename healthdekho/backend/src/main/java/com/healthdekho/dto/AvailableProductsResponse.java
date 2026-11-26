package com.healthdekho.dto;

import java.util.List;

public record AvailableProductsResponse(
        List<ProductSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
