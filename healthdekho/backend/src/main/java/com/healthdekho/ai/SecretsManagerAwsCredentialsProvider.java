package com.healthdekho.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicReference;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretsManagerAwsCredentialsProvider implements AwsCredentialsProvider {
    private final SecretsManagerClient secretsManagerClient;
    private final String secretName;
    private final ObjectMapper objectMapper;
    private final AtomicReference<AwsCredentials> cachedCredentials = new AtomicReference<>();

    public SecretsManagerAwsCredentialsProvider(
            SecretsManagerClient secretsManagerClient, String secretName, ObjectMapper objectMapper) {
        this.secretsManagerClient = secretsManagerClient;
        this.secretName = secretName;
        this.objectMapper = objectMapper;
    }

    @Override
    public AwsCredentials resolveCredentials() {
        AwsCredentials existing = cachedCredentials.get();
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            existing = cachedCredentials.get();
            if (existing != null) {
                return existing;
            }
            AwsCredentials loaded = loadFromSecretsManager();
            cachedCredentials.set(loaded);
            return loaded;
        }
    }

    private AwsCredentials loadFromSecretsManager() {
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(
                GetSecretValueRequest.builder().secretId(secretName).build());
        String secretString = response.secretString();
        if (secretString == null || secretString.isBlank()) {
            throw new IllegalStateException("Configured AWS secret contains no JSON payload.");
        }
        try {
            JsonNode json = objectMapper.readTree(secretString);
            String accessKeyId = json.path("AWS_ACCESS_KEY_ID").asText("");
            String secretAccessKey = json.path("AWS_SECRET_ACCESS_KEY").asText("");
            if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
                throw new IllegalStateException("AWS credentials keys are missing from the configured secret.");
            }
            return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse AWS credentials from Secrets Manager.", exception);
        }
    }
}
