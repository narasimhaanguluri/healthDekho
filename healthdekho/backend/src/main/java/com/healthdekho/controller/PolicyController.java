package com.healthdekho.controller;

import com.healthdekho.dto.PolicyDetailsResponse;
import com.healthdekho.dto.PolicyRegistrationRequest;
import com.healthdekho.dto.PolicyRegistrationResponse;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.service.PolicyRegistrationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    private final PolicyRegistrationService policyRegistrationService;

    public PolicyController(PolicyRegistrationService policyRegistrationService) {
        this.policyRegistrationService = policyRegistrationService;
    }

    @PostMapping
    public ResponseEntity<PolicyRegistrationResponse> registerPolicy(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody PolicyRegistrationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey) {
        PolicyRegistrationResponse response =
                policyRegistrationService.registerPolicy(principal.customerId(), request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{policyNumber}")
    public PolicyDetailsResponse getPolicyDetails(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable String policyNumber) {
        return policyRegistrationService.getPolicyDetails(principal.customerId(), policyNumber);
    }
}
