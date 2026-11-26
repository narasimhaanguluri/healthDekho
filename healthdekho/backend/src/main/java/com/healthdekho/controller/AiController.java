package com.healthdekho.controller;

import com.healthdekho.ai.AiProperties;
import com.healthdekho.dto.AiHealthResponse;
import com.healthdekho.dto.AiTestRequest;
import com.healthdekho.dto.AiTestResponse;
import com.healthdekho.exception.AiServiceUnavailableException;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private static final String PROVIDER_NAME = "Amazon Bedrock";
    private static final String MODEL_DISPLAY_NAME = "Claude Haiku 4.5";

    private final ChatClient chatClient;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final AwsRegionProvider awsRegionProvider;
    private final AiProperties aiProperties;

    public AiController(
            ChatClient chatClient,
            AwsCredentialsProvider awsCredentialsProvider,
            AwsRegionProvider awsRegionProvider,
            AiProperties aiProperties) {
        this.chatClient = chatClient;
        this.awsCredentialsProvider = awsCredentialsProvider;
        this.awsRegionProvider = awsRegionProvider;
        this.aiProperties = aiProperties;
    }

    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> health() {
        try {
            boolean regionConfigured = awsRegionProvider.getRegion() != null
                    && aiProperties.bedrock() != null
                    && aiProperties.bedrock().modelId() != null
                    && !aiProperties.bedrock().modelId().isBlank();
            boolean credentialsResolved = awsCredentialsProvider.resolveCredentials() != null;
            if (regionConfigured && credentialsResolved) {
                return ResponseEntity.ok(new AiHealthResponse(PROVIDER_NAME, MODEL_DISPLAY_NAME, "UP"));
            }
        } catch (RuntimeException ignored) {
            // Fall through to DOWN status without exposing credential details.
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AiHealthResponse(PROVIDER_NAME, MODEL_DISPLAY_NAME, "DOWN"));
    }

    @PostMapping("/test")
    public AiTestResponse testPrompt(@Valid @RequestBody AiTestRequest request) {
        try {
            String reply = chatClient.prompt()
                    .user(request.message().trim())
                    .call()
                    .content();
            if (reply == null || reply.isBlank()) {
                throw new AiServiceUnavailableException("Empty response from AI provider.");
            }
            return new AiTestResponse(reply);
        } catch (AiServiceUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AiServiceUnavailableException("Bedrock invocation failed.", exception);
        }
    }
}
