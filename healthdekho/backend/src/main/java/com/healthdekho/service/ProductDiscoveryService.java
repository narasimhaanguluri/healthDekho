package com.healthdekho.service;

import com.healthdekho.dto.AvailableProductsResponse;
import com.healthdekho.dto.ProductDetailResponse;
import java.util.UUID;

public interface ProductDiscoveryService {
    AvailableProductsResponse getAvailableProducts(UUID customerId, int page, int size);

    ProductDetailResponse getProductDetails(String productCode);
}
