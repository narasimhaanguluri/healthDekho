package com.healthdekho.repository;

import com.healthdekho.entity.PremiumInstallment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumInstallmentRepository extends JpaRepository<PremiumInstallment, UUID> {
}
