package com.healthdekho.repository;

import com.healthdekho.entity.InsuranceProduct;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, UUID> {
    boolean existsByProductCode(String productCode);

    Optional<InsuranceProduct> findByProductCode(String productCode);

    Optional<InsuranceProduct> findByProductCodeIgnoreCaseAndActiveTrue(String productCode);

    @Query("""
            SELECT p FROM InsuranceProduct p
            WHERE p.active = true
              AND NOT EXISTS (
                  SELECT 1 FROM CustomerPolicy cp
                  WHERE cp.customer.id = :customerId
                    AND cp.product.id = p.id
              )
            ORDER BY p.productCode ASC
            """)
    Page<InsuranceProduct> findAvailableForCustomer(@Param("customerId") UUID customerId, Pageable pageable);
}
