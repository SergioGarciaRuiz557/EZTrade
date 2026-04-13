package com.trading.platform.eztrade.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuracion para registrar en logs las peticiones HTTP entrantes.
 */
@Configuration
public class HttpObservabilityConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(2048);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("HTTP REQUEST: ");
        return filter;
    }
}

