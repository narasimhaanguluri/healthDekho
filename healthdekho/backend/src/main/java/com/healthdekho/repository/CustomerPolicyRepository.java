package com.healthdekho.repository;

import com.healthdekho.entity.CustomerPolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, UUID> {
    List<CustomerPolicy> findByCustomerIdOrderByStartDateDescPolicyNumberAsc(UUID customerId);

    boolean existsByCustomerIdAndProductId(UUID customerId, UUID productId);

    Optional<CustomerPolicy> findByCustomerIdAndRegistrationIdempotencyKey(
            UUID customerId, UUID registrationIdempotencyKey);

    Optional<CustomerPolicy> findByPolicyNumberAndCustomerId(String policyNumber, UUID customerId);

    Optional<CustomerPolicy> findTopByPolicyNumberStartingWithOrderByPolicyNumberDesc(String prefix);
}
