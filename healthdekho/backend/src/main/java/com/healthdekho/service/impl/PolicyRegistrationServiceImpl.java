package com.healthdekho.service.impl;

import com.healthdekho.dto.InstallmentSummaryResponse;
import com.healthdekho.dto.PolicyDetailsResponse;
import com.healthdekho.dto.PolicyRegistrationRequest;
import com.healthdekho.dto.PolicyRegistrationResponse;
import com.healthdekho.entity.Customer;
import com.healthdekho.entity.CustomerPolicy;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.entity.PremiumInstallment;
import com.healthdekho.exception.AgeNotEligibleException;
import com.healthdekho.exception.AuthenticationFailedException;
import com.healthdekho.exception.DuplicatePolicyException;
import com.healthdekho.exception.PolicyNotFoundException;
import com.healthdekho.exception.ProductNotFoundException;
import com.healthdekho.exception.ProductUnavailableException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;
import com.healthdekho.service.PolicyRegistrationService;
import com.healthdekho.service.PremiumScheduleService;
import com.healthdekho.util.MoneyFormatter;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyRegistrationServiceImpl implements PolicyRegistrationService {
    private static final String POLICY_STATUS_ACTIVE = "ACTIVE";
    private static final long POLICY_NUMBER_LOCK_KEY = 7243001L;

    private final CustomerRepository customerRepository;
    private final InsuranceProductRepository productRepository;
    private final CustomerPolicyRepository policyRepository;
    private final PremiumInstallmentRepository installmentRepository;
    private final PremiumScheduleService premiumScheduleService;
    private final EntityManager entityManager;

    public PolicyRegistrationServiceImpl(
            CustomerRepository customerRepository,
            InsuranceProductRepository productRepository,
            CustomerPolicyRepository policyRepository,
            PremiumInstallmentRepository installmentRepository,
            PremiumScheduleService premiumScheduleService,
            EntityManager entityManager) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.installmentRepository = installmentRepository;
        this.premiumScheduleService = premiumScheduleService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public PolicyRegistrationResponse registerPolicy(
            UUID customerId, PolicyRegistrationRequest request, UUID idempotencyKey) {
        Customer customer = customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(AuthenticationFailedException::new);

        LocalDate today = LocalDate.now();
        if (customer.getDateOfBirth() == null || customer.getDateOfBirth().isAfter(today)) {
            throw new IllegalArgumentException("Customer date of birth is invalid.");
        }

        String normalizedCode = request.productCode().trim();
        InsuranceProduct product = productRepository.findByProductCodeIgnoreCase(normalizedCode)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.isActive()) {
            throw new ProductUnavailableException();
        }

        if (idempotencyKey != null) {
            Optional<CustomerPolicy> existingByKey =
                    policyRepository.findByCustomerIdAndRegistrationIdempotencyKey(customer.getId(), idempotencyKey);
            if (existingByKey.isPresent()) {
                CustomerPolicy existing = existingByKey.get();
                if (!existing.getProduct().getId().equals(product.getId())) {
                    throw new DuplicatePolicyException();
                }
                long existingInstallments = installmentRepository.countByPolicyId(existing.getId());
                return new PolicyRegistrationResponse(
                        existing.getPolicyNumber(),
                        product.getProductCode(),
                        existing.getProductName(),
                        existing.getStartDate(),
                        existing.getMaturityDate(),
                        existing.getTotalPremium(),
                        Math.toIntExact(existingInstallments),
                        "Policy registered successfully");
            }
        }

        if (policyRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new DuplicatePolicyException();
        }

        int age = Period.between(customer.getDateOfBirth(), today).getYears();
        if (age < product.getMinAge() || age > product.getMaxAge()) {
            throw new AgeNotEligibleException(
                    "Customer age " + age + " is outside the eligible range of "
                            + product.getMinAge() + " to " + product.getMaxAge() + " years.");
        }

        validateProductTerms(product);
        int intervalMonths = resolveIntervalMonths(product.getPremiumFrequency());
        if (product.getTermMonths() % intervalMonths != 0) {
            throw new IllegalArgumentException("Product term is not evenly divisible by premium frequency.");
        }
        int installmentCount = product.getTermMonths() / intervalMonths;

        LocalDate maturityDate = today.plusMonths(product.getTermMonths());
        BigDecimal totalPremium = product.getPremiumPerInstallment()
                .multiply(BigDecimal.valueOf(installmentCount))
                .setScale(2, RoundingMode.HALF_UP);

        String policyNumber = generateNextPolicyNumber(today.getYear());

        CustomerPolicy policy = new CustomerPolicy();
        policy.setCustomer(customer);
        policy.setProduct(product);
        policy.setPolicyNumber(policyNumber);
        policy.setRegistrationIdempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID());
        policy.setStartDate(today);
        policy.setMaturityDate(maturityDate);
        policy.setProductName(product.getName());
        policy.setProductType(product.getProductType());
        policy.setTermMonths(product.getTermMonths());
        policy.setPremiumFrequency(product.getPremiumFrequency());
        policy.setPremiumPerInstallment(product.getPremiumPerInstallment());
        policy.setSumInsured(product.getSumInsured());
        policy.setTotalPremium(totalPremium);
        policy.setStatus(POLICY_STATUS_ACTIVE);

        CustomerPolicy savedPolicy;
        try {
            savedPolicy = policyRepository.saveAndFlush(policy);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicatePolicyException();
        }

        List<PremiumInstallment> installments =
                premiumScheduleService.createInitialSchedule(savedPolicy, product, installmentCount, intervalMonths);

        return new PolicyRegistrationResponse(
                savedPolicy.getPolicyNumber(),
                product.getProductCode(),
                savedPolicy.getProductName(),
                savedPolicy.getStartDate(),
                savedPolicy.getMaturityDate(),
                savedPolicy.getTotalPremium(),
                installments.size(),
                "Policy registered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailsResponse getPolicyDetails(UUID customerId, String policyNumber) {
        if (policyNumber == null || policyNumber.isBlank()) {
            throw new PolicyNotFoundException();
        }
        CustomerPolicy policy = policyRepository
                .findByPolicyNumberAndCustomerId(policyNumber.trim(), customerId)
                .orElseThrow(PolicyNotFoundException::new);

        List<InstallmentSummaryResponse> installments = installmentRepository
                .findByPolicyIdOrderByInstallmentNumberAsc(policy.getId())
                .stream()
                .map(installment -> new InstallmentSummaryResponse(
                        installment.getInstallmentNumber(),
                        installment.getDueDate(),
                        installment.getGraceDate(),
                        MoneyFormatter.format(installment.getScheduledAmount()),
                        MoneyFormatter.format(installment.getPaidAmount()),
                        installment.getStatus()))
                .toList();

        return new PolicyDetailsResponse(
                policy.getPolicyNumber(),
                policy.getProduct().getProductCode(),
                policy.getProductName(),
                policy.getProductType(),
                policy.getTermMonths(),
                policy.getStartDate(),
                policy.getMaturityDate(),
                policy.getPremiumFrequency(),
                MoneyFormatter.format(policy.getPremiumPerInstallment()),
                MoneyFormatter.format(policy.getSumInsured()),
                MoneyFormatter.format(policy.getTotalPremium()),
                policy.getStatus(),
                policy.getRegisteredAt(),
                installments);
    }

    private void validateProductTerms(InsuranceProduct product) {
        if (product.getTermMonths() == null || product.getTermMonths() <= 0
                || product.getPremiumPerInstallment() == null
                || product.getPremiumPerInstallment().compareTo(BigDecimal.ZERO) <= 0
                || product.getSumInsured() == null
                || product.getSumInsured().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product terms and monetary amounts must be positive.");
        }
    }

    public static int resolveIntervalMonths(String premiumFrequency) {
        if (premiumFrequency == null) {
            throw new IllegalArgumentException("Premium frequency is required.");
        }
        return switch (premiumFrequency.trim().toUpperCase(Locale.ROOT)) {
            case "MONTHLY" -> 1;
            case "QUARTERLY" -> 3;
            case "HALF_YEARLY" -> 6;
            case "YEARLY" -> 12;
            default -> throw new IllegalArgumentException("Unsupported premium frequency: " + premiumFrequency);
        };
    }

    private String generateNextPolicyNumber(int year) {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:lockKey)")
                .setParameter("lockKey", POLICY_NUMBER_LOCK_KEY)
                .getSingleResult();
        String prefix = "HD-" + year + "-";
        int nextSequence = policyRepository
                .findTopByPolicyNumberStartingWithOrderByPolicyNumberDesc(prefix)
                .map(CustomerPolicy::getPolicyNumber)
                .map(latest -> Integer.parseInt(latest.substring(prefix.length())) + 1)
                .orElse(1);
        return prefix + String.format(Locale.ROOT, "%06d", nextSequence);
    }
}
