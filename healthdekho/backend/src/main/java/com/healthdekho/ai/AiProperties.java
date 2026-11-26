package com.healthdekho.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "healthdekho.ai")
public record AiProperties(Aws aws, Bedrock bedrock, SecretsManager secretsManager) {
    public record Aws(String region) {
    }

    public record Bedrock(String modelId) {
    }

    public record SecretsManager(boolean enabled, String secretName) {
    }
}
