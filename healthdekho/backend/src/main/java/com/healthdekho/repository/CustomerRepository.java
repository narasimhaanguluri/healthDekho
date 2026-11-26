package com.healthdekho.repository;

import com.healthdekho.entity.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByNormalizedEmail(String normalizedEmail);

    boolean existsByNormalizedEmail(String normalizedEmail);

    boolean existsByMobileNumber(String mobileNumber);

    long countByNormalizedEmail(String normalizedEmail);
}
