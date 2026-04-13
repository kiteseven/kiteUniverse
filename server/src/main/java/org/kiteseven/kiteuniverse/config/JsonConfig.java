package org.kiteseven.kiteuniverse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides shared JSON infrastructure used by controller responses and Redis cache payloads.
 */
@Configuration
public class JsonConfig {

    /**
     * Creates the shared ObjectMapper bean and registers available Java time modules automatically.
     *
     * @return shared object mapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
