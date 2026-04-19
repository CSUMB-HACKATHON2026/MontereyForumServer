package com.MCF.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the MCF API.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@SpringBootApplication
public class MCFApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments supplied at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(MCFApplication.class, args);
    }
}