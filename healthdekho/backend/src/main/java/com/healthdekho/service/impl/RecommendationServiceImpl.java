package com.healthdekho.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthdekho.dto.RecommendationHistoryEntryResponse;
import com.healthdekho.dto.RecommendationHistoryResponse;
import com.healthdekho.dto.RecommendationItemResponse;
import com.healthdekho.dto.RecommendationRequest;
import com.healthdekho.dto.RecommendationResponse;
import com.healthdekho.entity.AIRecommendationItem;
import com.healthdekho.entity.AIRecommendationRequest;
import com.healthdekho.entity.InsuranceProduct;
import com.healthdekho.exception.AiRecommendationUnavailableException;
import com.healthdekho.exception.InvalidPaginationException;
import com.healthdekho.repository.AIRecommendationItemRepository;
import com.healthdekho.repository.AIRecommendationRequestRepository;
import com.healthdekho.service.RecommendationService;
import com.healthdekho.service.impl.RecommendationPersistenceService.CandidateProductSnapshot;
import com.healthdekho.service.impl.RecommendationPersistenceService.PreparedCandidates;
import com.healthdekho.service.impl.RecommendationPersistenceService.ValidatedRecommendation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_REASON_LENGTH = 2000;
    private static final int MAX_SUITABILITY_LENGTH = 100;

    private final ChatClient chatClient;
    private final String modelId;
    private final RecommendationPromptBuilder promptBuilder;
    private final RecommendationPersistenceService persistenceService;
    private final AIRecommendationRequestRepository requestRepository;
    private final AIRecommendationItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public RecommendationServiceImpl(
            ChatClient chatClient,
            @Value("${spring.ai.bedrock.converse.chat.options.model}") String modelId,
            RecommendationPromptBuilder promptBuilder,
            RecommendationPersistenceService persistenceService,
            AIRecommendationRequestRepository requestRepository,
            AIRecommendationItemRepository itemRepository,
            ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.modelId = modelId;
        this.promptBuilder = promptBuilder;
        this.persistenceService = persistenceService;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public RecommendationResponse generateRecommendations(UUID customerId, RecommendationRequest request) {
        PreparedCandidates prepared = persistenceService.prepareCandidates(customerId, request);

        UUID requestId = persistenceService.createPendingRequest(
                prepared.customerId(),
                request,
                prepared.calculatedAge(),
                prepared.candidates(),
                modelId,
                RecommendationPromptBuilder.PROMPT_VERSION);

        if (prepared.candidates().isEmpty()) {
            persistenceService.markNoResults(requestId);
            return new RecommendationResponse(
                    requestId,
                    RecommendationPersistenceService.STATUS_NO_RESULTS,
                    List.of());
        }

        Map<String, CandidateProductSnapshot> candidateByCode = prepared.candidates().stream()
                .collect(Collectors.toMap(
                        c -> c.productCode().trim().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (first, ignored) -> first));

        try {
            String rawModelOutput = chatClient.prompt()
                    .system(promptBuilder.systemPrompt())
                    .user(promptBuilder.userPrompt(prepared.calculatedAge(), request, prepared.candidates()))
                    .call()
                    .content();

            List<ValidatedRecommendation> validated = parseAndValidateResponse(rawModelOutput, candidateByCode);
            List<RecommendationItemResponse> savedItems =
                    persistenceService.saveSucceededItems(requestId, validated, candidateByCode);

            return new RecommendationResponse(
                    requestId,
                    RecommendationPersistenceService.STATUS_SUCCEEDED,
                    savedItems);
        } catch (RuntimeException exception) {
            persistenceService.markFailed(requestId);
            if (exception instanceof AiRecommendationUnavailableException aiException) {
                throw aiException;
            }
            throw new AiRecommendationUnavailableException("AI recommendation generation failed.", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationHistoryResponse getHistory(UUID customerId, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "Pagination requires page >= 0 and size between 1 and " + MAX_PAGE_SIZE + ".");
        }

        Page<AIRecommendationRequest> requestsPage =
                requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(page, size));

        List<UUID> requestIds = requestsPage.getContent().stream()
                .map(AIRecommendationRequest::getId)
                .toList();

        Map<UUID, List<AIRecommendationItem>> itemsByRequestId = new HashMap<>();
        if (!requestIds.isEmpty()) {
            for (AIRecommendationItem item : itemRepository.findWithProductByRequestIdInOrderByRankAsc(requestIds)) {
                itemsByRequestId
                        .computeIfAbsent(item.getRequest().getId(), ignored -> new ArrayList<>())
                        .add(item);
            }
        }

        List<RecommendationHistoryEntryResponse> entries = requestsPage.getContent().stream()
                .map(req -> toHistoryEntry(req, itemsByRequestId.getOrDefault(req.getId(), List.of())))
                .toList();

        return new RecommendationHistoryResponse(
                entries,
                requestsPage.getNumber(),
                requestsPage.getSize(),
                requestsPage.getTotalElements(),
                requestsPage.getTotalPages());
    }

    private List<ValidatedRecommendation> parseAndValidateResponse(
            String rawModelOutput, Map<String, CandidateProductSnapshot> candidateByCode) {
        if (rawModelOutput == null || rawModelOutput.isBlank()) {
            throw new AiRecommendationUnavailableException("AI model returned an empty response.");
        }

        String cleanedJson = stripCodeFences(rawModelOutput);
        JsonNode root;
        try {
            root = objectMapper.readTree(cleanedJson);
        } catch (Exception exception) {
            throw new AiRecommendationUnavailableException("AI model returned malformed JSON.", exception);
        }

        JsonNode recommendationsNode = root.get("recommendations");
        if (recommendationsNode == null || !recommendationsNode.isArray() || recommendationsNode.isEmpty()) {
            throw new AiRecommendationUnavailableException("AI model returned no recommendation items.");
        }

        List<ValidatedRecommendation> validated = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        int fallbackRank = 1;

        for (JsonNode itemNode : recommendationsNode) {
            if (!itemNode.isObject()) {
                throw new AiRecommendationUnavailableException("AI recommendation item is not an object.");
            }
            String rawCode = itemNode.path("productCode").asText("");
            String normalizedCode = rawCode.trim().toUpperCase(Locale.ROOT);
            if (normalizedCode.isEmpty() || !candidateByCode.containsKey(normalizedCode)) {
                throw new AiRecommendationUnavailableException("AI model referenced an unknown or ineligible product.");
            }
            if (!seenCodes.add(normalizedCode)) {
                throw new AiRecommendationUnavailableException("AI model returned duplicate product recommendations.");
            }

            String reason = sanitizeForStorage(itemNode.path("recommendationReason").asText(""));
            if (reason.isEmpty()) {
                throw new AiRecommendationUnavailableException("AI model omitted recommendation reason.");
            }
            if (reason.length() > MAX_REASON_LENGTH) {
                reason = reason.substring(0, MAX_REASON_LENGTH).trim();
            }

            String suitability = sanitizeForStorage(itemNode.path("suitability").asText("")).toUpperCase(Locale.ROOT);
            if (suitability.isEmpty()) {
                throw new AiRecommendationUnavailableException("AI model omitted suitability.");
            }
            if (suitability.length() > MAX_SUITABILITY_LENGTH) {
                suitability = suitability.substring(0, MAX_SUITABILITY_LENGTH);
            }

            JsonNode rankNode = itemNode.get("rank");
            int rank = fallbackRank;
            if (rankNode != null && !rankNode.isNull()) {
                if (!rankNode.isInt() || rankNode.asInt() <= 0) {
                    throw new AiRecommendationUnavailableException("AI model returned an invalid rank.");
                }
                rank = rankNode.asInt();
            }

            validated.add(new ValidatedRecommendation(normalizedCode, reason, suitability, rank));
            fallbackRank++;
        }

        return validated;
    }

    /**
     * Normalizes AI text to printable ASCII so PostgreSQL databases using single-byte
     * encodings such as WIN1252 do not fail with SQLState 22P05 (e.g. for the ₹ symbol).
     */
    static String sanitizeForStorage(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\u20B9' -> out.append("INR ");
                case '\u2013', '\u2014', '\u2212' -> out.append('-');
                case '\u2018', '\u2019' -> out.append('\'');
                case '\u201C', '\u201D' -> out.append('"');
                case '\u2022' -> out.append('*');
                case '\u2026' -> out.append("...");
                case '\u00A0' -> out.append(' ');
                default -> {
                    if ((ch >= 0x20 && ch <= 0x7E) || ch == '\n' || ch == '\r' || ch == '\t') {
                        out.append(ch);
                    }
                }
            }
        }
        return out.toString().replaceAll(" {2,}", " ").trim();
    }

    private String stripCodeFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline >= 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private RecommendationHistoryEntryResponse toHistoryEntry(
            AIRecommendationRequest req, List<AIRecommendationItem> items) {
        if (!RecommendationPersistenceService.STATUS_SUCCEEDED.equals(req.getStatus())) {
            return new RecommendationHistoryEntryResponse(
                    req.getId(),
                    req.getCreatedAt(),
                    req.getStatus(),
                    req.getGoals(),
                    req.getPreferredTermMonths(),
                    req.getPreferredProductType(),
                    req.getCalculatedAge(),
                    List.of());
        }

        Map<UUID, CandidateProductSnapshot> snapshotByProductId =
                persistenceService.deserializeSnapshot(req.getCandidateSnapshot()).stream()
                        .collect(Collectors.toMap(
                                CandidateProductSnapshot::productId,
                                Function.identity(),
                                (first, ignored) -> first));

        List<RecommendationItemResponse> itemResponses = items.stream()
                .map(item -> {
                    InsuranceProduct product = item.getProduct();
                    CandidateProductSnapshot snapshot = snapshotByProductId.get(product.getId());
                    if (snapshot != null) {
                        String frequency = snapshot.premiumFrequency() != null
                                ? snapshot.premiumFrequency()
                                : product.getPremiumFrequency();
                        return new RecommendationItemResponse(
                                snapshot.productCode(),
                                snapshot.policyName(),
                                snapshot.policyType(),
                                snapshot.policyTermMonths(),
                                frequency,
                                snapshot.premiumAmount(),
                                snapshot.sumInsured(),
                                item.getReason(),
                                item.getSuitability(),
                                item.getRank());
                    }
                    return new RecommendationItemResponse(
                            product.getProductCode(),
                            product.getName(),
                            product.getProductType(),
                            product.getTermMonths(),
                            product.getPremiumFrequency(),
                            product.getPremiumPerInstallment(),
                            product.getSumInsured(),
                            item.getReason(),
                            item.getSuitability(),
                            item.getRank());
                })
                .toList();

        return new RecommendationHistoryEntryResponse(
                req.getId(),
                req.getCreatedAt(),
                req.getStatus(),
                req.getGoals(),
                req.getPreferredTermMonths(),
                req.getPreferredProductType(),
                req.getCalculatedAge(),
                itemResponses);
    }
}
