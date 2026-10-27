package com.trading.platform.eztrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the EzTrade application.
 * <p>
 * This class starts the Spring Boot context and loads all configured
 * application components.
 */
@SpringBootApplication
public class EzTradeApplication {

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EzTradeApplication.class, args);
    }

}
