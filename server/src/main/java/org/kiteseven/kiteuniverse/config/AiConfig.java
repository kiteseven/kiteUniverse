package org.kiteseven.kiteuniverse.config;

import org.kiteseven.kiteuniverse.config.properties.AiProperties;
import org.kiteseven.kiteuniverse.support.ai.DeepSeekClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向 Spring 容器注册 DeepSeekClient，仅在配置了 API Key 时生效。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ConditionalOnProperty(prefix = "kite-universe.ai.openai", name = "api-key")
public class AiConfig {

    private final AiProperties aiProperties;

    public AiConfig(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Bean
    public DeepSeekClient deepSeekClient() {
        return new DeepSeekClient(
                aiProperties.getBaseUrl(),
                aiProperties.getApiKey(),
                aiProperties.getModel()
        );
    }
}
