package com.healthdekho.repository;

import com.healthdekho.entity.PremiumPayment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, UUID> {
}
