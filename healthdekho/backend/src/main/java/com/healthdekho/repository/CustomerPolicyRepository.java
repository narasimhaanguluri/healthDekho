package com.healthdekho.repository;

import com.healthdekho.entity.CustomerPolicy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, UUID> {
    List<CustomerPolicy> findByCustomerIdOrderByStartDateDescPolicyNumberAsc(UUID customerId);
}
