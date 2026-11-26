package com.healthdekho.service;

import com.healthdekho.dto.CustomerProfile;
import com.healthdekho.dto.DashboardResponse;
import com.healthdekho.dto.PolicySummaryResponse;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerProfile getProfile(UUID customerId);

    List<PolicySummaryResponse> getPolicies(UUID customerId);

    DashboardResponse getDashboard(UUID customerId);
}
