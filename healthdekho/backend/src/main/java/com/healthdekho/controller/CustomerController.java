package com.healthdekho.controller;

import com.healthdekho.dto.CustomerProfile;
import com.healthdekho.dto.PolicySummaryResponse;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.service.CustomerService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public CustomerProfile getCurrentCustomer(@AuthenticationPrincipal CustomerPrincipal principal) {
        return customerService.getProfile(principal.customerId());
    }

    @GetMapping("/me/policies")
    public List<PolicySummaryResponse> getMyPolicies(@AuthenticationPrincipal CustomerPrincipal principal) {
        return customerService.getPolicies(principal.customerId());
    }
}
