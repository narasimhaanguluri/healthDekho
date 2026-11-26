package com.healthdekho.service.impl;

import com.healthdekho.dto.AvailableProductsResponse;
import com.healthdekho.dto.ProductDetailResponse;
import com.healthdekho.dto.ProductSummaryResponse;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.exception.InvalidPaginationException;
import com.healthdekho.exception.ProductNotFoundException;
import com.healthdekho.repository.InsuranceProductRepository;
import com.healthdekho.service.ProductDiscoveryService;
import com.healthdekho.util.MoneyFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDiscoveryServiceImpl implements ProductDiscoveryService {
    private static final int MAX_PAGE_SIZE = 100;

    private final InsuranceProductRepository productRepository;

    public ProductDiscoveryServiceImpl(InsuranceProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableProductsResponse getAvailableProducts(UUID customerId, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "Pagination requires page >= 0 and size between 1 and " + MAX_PAGE_SIZE + ".");
        }
        Page<InsuranceProduct> result = productRepository.findAvailableForCustomer(
                customerId, PageRequest.of(page, size));
        List<ProductSummaryResponse> items = result.getContent().stream()
                .map(this::toProductSummary)
                .toList();
        return new AvailableProductsResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetails(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new ProductNotFoundException();
        }
        InsuranceProduct product = productRepository.findByProductCodeIgnoreCaseAndActiveTrue(productCode.trim())
                .orElseThrow(ProductNotFoundException::new);
        return new ProductDetailResponse(
                product.getProductCode(),
                product.getName(),
                product.getProductType(),
                product.getPremiumFrequency(),
                MoneyFormatter.format(product.getPremiumPerInstallment()),
                MoneyFormatter.format(product.getSumInsured()),
                product.getTermMonths(),
                product.getMinAge(),
                product.getMaxAge());
    }

    private ProductSummaryResponse toProductSummary(InsuranceProduct product) {
        return new ProductSummaryResponse(
                product.getProductCode(),
                product.getName(),
                product.getProductType(),
                product.getTermMonths(),
                product.getPremiumFrequency(),
                MoneyFormatter.format(product.getPremiumPerInstallment()),
                MoneyFormatter.format(product.getSumInsured()),
                product.getMinAge(),
                product.getMaxAge());
    }
}
