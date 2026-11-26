package com.healthdekho.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class BedrockAiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "healthdekho.ai.secrets-manager", name = "enabled", havingValue = "true")
    public SecretsManagerClient secretsManagerClient(AiProperties properties) {
        return SecretsManagerClient.builder()
                .region(Region.of(properties.aws().region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public AwsRegionProvider awsRegionProvider(AiProperties properties) {
        Region region = Region.of(properties.aws().region());
        return () -> region;
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
            AiProperties properties,
            ObjectProvider<SecretsManagerClient> secretsManagerClientProvider,
            ObjectMapper objectMapper) {
        if (properties.secretsManager() != null && properties.secretsManager().enabled()) {
            String secretName = properties.secretsManager().secretName();
            if (secretName == null || secretName.isBlank()) {
                throw new IllegalStateException("AWS_SECRET_NAME must be configured when Secrets Manager is enabled.");
            }
            SecretsManagerClient client = secretsManagerClientProvider.getIfAvailable();
            if (client == null) {
                throw new IllegalStateException("SecretsManagerClient must be configured when Secrets Manager is enabled.");
            }
            return new SecretsManagerAwsCredentialsProvider(client, secretName, objectMapper);
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
