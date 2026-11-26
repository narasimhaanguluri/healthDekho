package com.healthdekho.config;

import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.repository.InsuranceProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class ProductCatalogSeedRunner implements ApplicationRunner {
    private final InsuranceProductRepository productRepository;

    public ProductCatalogSeedRunner(InsuranceProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
                product("HLT001", "Health Secure Plus", "HEALTH", 12, "MONTHLY", "1500.00", "500000.00", 18, 60),
                product("HLT002", "Family Health Shield", "HEALTH", 24, "QUARTERLY", "4200.00", "1000000.00", 21, 65),
                product("HLT003", "Senior Citizen Care", "HEALTH", 12, "HALF_YEARLY", "9500.00", "500000.00", 60, 80),
                product("HLT004", "Critical Illness Shield", "HEALTH", 36, "YEARLY", "8500.00", "1500000.00", 18, 65),
                product("LIF001", "Smart Life Protect", "LIFE", 120, "MONTHLY", "2500.00", "2500000.00", 18, 55),
                product("LIF002", "Family Life Cover", "LIFE", 240, "YEARLY", "18000.00", "5000000.00", 21, 60),
                product("RET001", "Retirement Future Plan", "RETIREMENT", 180, "HALF_YEARLY", "15000.00", "3000000.00", 25, 60),
                product("EDU001", "Child Education Secure", "EDUCATION", 120, "QUARTERLY", "6000.00", "1200000.00", 21, 50)));
    }

    private InsuranceProduct product(
            String code,
            String name,
            String type,
            int termMonths,
            String frequency,
            String premiumAmount,
            String sumInsured,
            int minAge,
            int maxAge) {
        InsuranceProduct product = new InsuranceProduct();
        product.setProductCode(code);
        product.setName(name);
        product.setProductType(type);
        product.setTermMonths(termMonths);
        product.setPremiumFrequency(frequency);
        product.setPremiumPerInstallment(new BigDecimal(premiumAmount));
        product.setSumInsured(new BigDecimal(sumInsured));
        product.setMinAge(minAge);
        product.setMaxAge(maxAge);
        product.setActive(true);
        return product;
    }
}
