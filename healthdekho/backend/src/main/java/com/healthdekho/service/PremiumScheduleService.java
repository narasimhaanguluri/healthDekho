package com.healthdekho.service;

import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.entity.PremiumInstallment;
import java.util.List;

public interface PremiumScheduleService {
    List<PremiumInstallment> createInitialSchedule(
            CustomerPolicy policy, InsuranceProduct product, int installmentCount, int intervalMonths);
}
