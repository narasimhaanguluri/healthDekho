package com.healthdekho.controller;

import com.healthdekho.dto.DashboardResponse;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.service.CustomerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final CustomerService customerService;

    public DashboardController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal CustomerPrincipal principal) {
        return customerService.getDashboard(principal.customerId());
    }
}
