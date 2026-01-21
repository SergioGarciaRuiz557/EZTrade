package com.trading.platform.eztrade.security.configuration;

import org.springframework.http.HttpMethod;

public interface SecuredEndpoint {

    String path();
    HttpMethod method();
    SecurityPolicy policy();
}
