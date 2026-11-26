package com.healthdekho.service.impl;

import com.healthdekho.dto.CustomerProfile;
import com.healthdekho.dto.DashboardCustomerSummary;
import com.healthdekho.dto.DashboardResponse;
import com.healthdekho.dto.PolicySummaryResponse;
import com.healthdekho.entity.Customer;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.exception.AuthenticationFailedException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.service.CustomerService;
import com.healthdekho.util.MoneyFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerPolicyRepository customerPolicyRepository;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerPolicyRepository customerPolicyRepository) {
        this.customerRepository = customerRepository;
        this.customerPolicyRepository = customerPolicyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfile getProfile(UUID customerId) {
        Customer customer = loadActiveCustomer(customerId);
        return new CustomerProfile(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getNormalizedEmail(),
                customer.getMobileNumber(),
                customer.getDateOfBirth());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicySummaryResponse> getPolicies(UUID customerId) {
        Customer customer = loadActiveCustomer(customerId);
        return loadOwnedPolicies(customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID customerId) {
        Customer customer = loadActiveCustomer(customerId);
        List<PolicySummaryResponse> policies = loadOwnedPolicies(customer.getId());
        return new DashboardResponse(
                new DashboardCustomerSummary(
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getNormalizedEmail()),
                policies.size(),
                policies);
    }

    private Customer loadActiveCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(AuthenticationFailedException::new);
    }

    private List<PolicySummaryResponse> loadOwnedPolicies(UUID customerId) {
        return customerPolicyRepository.findByCustomerIdOrderByStartDateDescPolicyNumberAsc(customerId).stream()
                .map(this::toPolicySummary)
                .toList();
    }

    private PolicySummaryResponse toPolicySummary(CustomerPolicy policy) {
        return new PolicySummaryResponse(
                policy.getPolicyNumber(),
                policy.getProductName(),
                policy.getProductType(),
                policy.getTermMonths(),
                policy.getStartDate(),
                policy.getMaturityDate(),
                policy.getPremiumFrequency(),
                MoneyFormatter.format(policy.getPremiumPerInstallment()),
                MoneyFormatter.format(policy.getSumInsured()),
                policy.getStatus());
    }
}
