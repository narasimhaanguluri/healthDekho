package com.healthdekho.controller;

import com.healthdekho.dto.AvailableProductsResponse;
import com.healthdekho.dto.ProductDetailResponse;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.service.ProductDiscoveryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductDiscoveryService productDiscoveryService;

    public ProductController(ProductDiscoveryService productDiscoveryService) {
        this.productDiscoveryService = productDiscoveryService;
    }

    @GetMapping("/available")
    public AvailableProductsResponse getAvailableProducts(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productDiscoveryService.getAvailableProducts(principal.customerId(), page, size);
    }

    @GetMapping("/{productCode}")
    public ProductDetailResponse getProductDetails(@PathVariable String productCode) {
        return productDiscoveryService.getProductDetails(productCode);
    }
}
