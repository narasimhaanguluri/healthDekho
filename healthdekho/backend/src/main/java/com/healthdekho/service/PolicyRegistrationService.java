package com.healthdekho.service;

import com.healthdekho.dto.PolicyDetailsResponse;
import com.healthdekho.dto.PolicyRegistrationRequest;
import com.healthdekho.dto.PolicyRegistrationResponse;
import java.util.UUID;

public interface PolicyRegistrationService {
    PolicyRegistrationResponse registerPolicy(
            UUID customerId, PolicyRegistrationRequest request, UUID idempotencyKey);

    PolicyDetailsResponse getPolicyDetails(UUID customerId, String policyNumber);
}
