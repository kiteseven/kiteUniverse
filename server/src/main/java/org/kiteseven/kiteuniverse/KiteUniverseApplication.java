package org.kiteseven.kiteuniverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class KiteUniverseApplication {

    /**
     * Starts the backend application.
     *
     * @param args startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KiteUniverseApplication.class, args);
    }
}
