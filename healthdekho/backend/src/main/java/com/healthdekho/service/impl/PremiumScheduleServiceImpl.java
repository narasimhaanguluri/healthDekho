package com.healthdekho.service.impl;

import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.entity.PremiumInstallment;
import com.healthdekho.repository.PremiumInstallmentRepository;
import com.healthdekho.service.PremiumScheduleService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PremiumScheduleServiceImpl implements PremiumScheduleService {
    private static final String INITIAL_STATUS = "PENDING";

    private final PremiumInstallmentRepository installmentRepository;

    public PremiumScheduleServiceImpl(PremiumInstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<PremiumInstallment> createInitialSchedule(
            CustomerPolicy policy, InsuranceProduct product, int installmentCount, int intervalMonths) {
        if (installmentCount <= 0 || intervalMonths <= 0) {
            throw new IllegalArgumentException("Installment count and interval must be positive.");
        }
        LocalDate startDate = policy.getStartDate();
        BigDecimal zeroPaid = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        List<PremiumInstallment> schedule = new ArrayList<>(installmentCount);

        for (int number = 1; number <= installmentCount; number++) {
            LocalDate dueDate = startDate.plusMonths((long) (number - 1) * intervalMonths);
            PremiumInstallment installment = new PremiumInstallment();
            installment.setPolicy(policy);
            installment.setInstallmentNumber(number);
            installment.setDueDate(dueDate);
            installment.setGraceDate(dueDate.plusMonths(1));
            installment.setScheduledAmount(product.getPremiumPerInstallment());
            installment.setPaidAmount(zeroPaid);
            installment.setStatus(INITIAL_STATUS);
            schedule.add(installment);
        }

        return installmentRepository.saveAll(schedule);
    }
}
