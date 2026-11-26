package com.healthdekho.dto;

import java.util.List;

public record DashboardResponse(
        DashboardCustomerSummary customer,
        int totalPolicies,
        List<PolicySummaryResponse> policies) {
}
