package com.healthdekho.repository;

import com.healthdekho.entity.PremiumInstallment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumInstallmentRepository extends JpaRepository<PremiumInstallment, UUID> {
    List<PremiumInstallment> findByPolicyIdOrderByInstallmentNumberAsc(UUID policyId);

    long countByPolicyId(UUID policyId);
}
