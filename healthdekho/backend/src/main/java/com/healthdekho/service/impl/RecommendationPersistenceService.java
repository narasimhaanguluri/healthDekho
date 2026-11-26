package com.healthdekho.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthdekho.dto.RecommendationItemResponse;
import com.healthdekho.dto.RecommendationRequest;
import com.healthdekho.entity.AIRecommendationItem;
import com.healthdekho.entity.AIRecommendationRequest;
import com.healthdekho.entity.Customer;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.exception.AiRecommendationUnavailableException;
import com.healthdekho.exception.AuthenticationFailedException;
import com.healthdekho.repository.AIRecommendationItemRepository;
import com.healthdekho.repository.AIRecommendationRequestRepository;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationPersistenceService {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_NO_RESULTS = "NO_RESULTS";
    public static final String STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String STATUS_FAILED = "FAILED";

    private final CustomerRepository customerRepository;
    private final InsuranceProductRepository productRepository;
    private final CustomerPolicyRepository policyRepository;
    private final AIRecommendationRequestRepository requestRepository;
    private final AIRecommendationItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public RecommendationPersistenceService(
            CustomerRepository customerRepository,
            InsuranceProductRepository productRepository,
            CustomerPolicyRepository policyRepository,
            AIRecommendationRequestRepository requestRepository,
            AIRecommendationItemRepository itemRepository,
            ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PreparedCandidates prepareCandidates(UUID customerId, RecommendationRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(AuthenticationFailedException::new);

        LocalDate today = LocalDate.now();
        if (customer.getDateOfBirth() == null || customer.getDateOfBirth().isAfter(today)) {
            throw new IllegalArgumentException("Customer date of birth is invalid.");
        }

        int customerAge = Period.between(customer.getDateOfBirth(), today).getYears();

        Set<UUID> ownedProductIds = policyRepository
                .findByCustomerIdOrderByStartDateDescPolicyNumberAsc(customer.getId())
                .stream()
                .map(policy -> policy.getProduct().getId())
                .collect(Collectors.toSet());

        List<CandidateProductSnapshot> candidates = productRepository.findByActiveTrueOrderByProductCodeAsc()
                .stream()
                .filter(product -> !ownedProductIds.contains(product.getId()))
                .filter(product -> customerAge >= product.getMinAge() && customerAge <= product.getMaxAge())
                .map(product -> new CandidateProductSnapshot(
                        product.getId(),
                        product.getProductCode(),
                        product.getName(),
                        product.getProductType(),
                        product.getTermMonths(),
                        product.getPremiumPerInstallment(),
                        product.getSumInsured()))
                .toList();

        return new PreparedCandidates(customer.getId(), customerAge, candidates);
    }

    @Transactional
    public UUID createPendingRequest(
            UUID customerId,
            RecommendationRequest request,
            int calculatedAge,
            List<CandidateProductSnapshot> candidates,
            String modelId,
            String promptVersion) {
        Customer customer = customerRepository.getReferenceById(customerId);
        AIRecommendationRequest entity = new AIRecommendationRequest();
        entity.setCustomer(customer);
        entity.setGoals(trimToNull(request.insuranceGoals()));
        entity.setPreferredTermMonths(request.preferredPolicyTermMonths());
        entity.setPreferredProductType(normalizeUpperOrNull(request.insuranceType()));
        entity.setCalculatedAge(calculatedAge);
        entity.setStatus(STATUS_PENDING);
        entity.setModelId(modelId);
        entity.setPromptVersion(promptVersion);
        entity.setCandidateSnapshot(serializeSnapshot(candidates));
        return requestRepository.saveAndFlush(entity).getId();
    }

    @Transactional
    public void markNoResults(UUID requestId) {
        AIRecommendationRequest entity = requestRepository.findById(requestId).orElseThrow();
        entity.setStatus(STATUS_NO_RESULTS);
        requestRepository.save(entity);
    }

    @Transactional
    public void markFailed(UUID requestId) {
        AIRecommendationRequest entity = requestRepository.findById(requestId).orElseThrow();
        entity.setStatus(STATUS_FAILED);
        requestRepository.save(entity);
    }

    @Transactional
    public List<RecommendationItemResponse> saveSucceededItems(
            UUID requestId,
            List<ValidatedRecommendation> validatedItems,
            Map<String, CandidateProductSnapshot> candidateByCode) {
        AIRecommendationRequest requestEntity = requestRepository.findById(requestId).orElseThrow();
        List<AIRecommendationItem> entitiesToSave = new ArrayList<>(validatedItems.size());
        List<RecommendationItemResponse> responseItems = new ArrayList<>(validatedItems.size());

        for (ValidatedRecommendation validated : validatedItems) {
            CandidateProductSnapshot snapshot = candidateByCode.get(validated.productCode());
            InsuranceProduct activeProduct = productRepository.findById(snapshot.productId())
                    .filter(InsuranceProduct::isActive)
                    .orElseThrow(() -> new AiRecommendationUnavailableException(
                            "Recommended product is no longer active."));

            AIRecommendationItem itemEntity = new AIRecommendationItem();
            itemEntity.setRequest(requestEntity);
            itemEntity.setProduct(activeProduct);
            itemEntity.setReason(validated.recommendationReason());
            itemEntity.setSuitability(validated.suitability());
            itemEntity.setRank(validated.rank());
            itemEntity.setSelected(false);
            entitiesToSave.add(itemEntity);

            responseItems.add(new RecommendationItemResponse(
                    snapshot.productCode(),
                    snapshot.policyName(),
                    snapshot.policyType(),
                    snapshot.policyTermMonths(),
                    snapshot.premiumAmount(),
                    snapshot.sumInsured(),
                    validated.recommendationReason(),
                    validated.suitability(),
                    validated.rank()));
        }

        itemRepository.saveAll(entitiesToSave);
        requestEntity.setStatus(STATUS_SUCCEEDED);
        requestRepository.save(requestEntity);
        return responseItems;
    }

    public List<CandidateProductSnapshot> deserializeSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(snapshotJson, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private String serializeSnapshot(List<CandidateProductSnapshot> candidates) {
        try {
            return objectMapper.writeValueAsString(candidates);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize recommendation candidate snapshot.", exception);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeUpperOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public record CandidateProductSnapshot(
            UUID productId,
            String productCode,
            String policyName,
            String policyType,
            Integer policyTermMonths,
            BigDecimal premiumAmount,
            BigDecimal sumInsured) {
    }

    public record PreparedCandidates(
            UUID customerId,
            int calculatedAge,
            List<CandidateProductSnapshot> candidates) {
    }

    public record ValidatedRecommendation(
            String productCode,
            String recommendationReason,
            String suitability,
            int rank) {
    }
}
