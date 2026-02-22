package com.trading.platform.eztrade.security.configuration;

import org.springframework.http.HttpMethod;

/**
 * Contrato que representa un endpoint securizado de la aplicación.
 * <p>
 * Cada implementación define la ruta, el método HTTP y la política
 * de seguridad asociada al recurso expuesto.
 */
public interface SecuredEndpoint {

    /**
     * Devuelve la ruta del endpoint protegido.
     *
     * @return ruta del endpoint (por ejemplo, <strong>`/api/user`</strong>)
     */
    String path();

    /**
     * Devuelve el método HTTP asociado al endpoint.
     *
     * @return método HTTP requerido para acceder al recurso
     */
    HttpMethod method();

    /**
     * Devuelve la política de seguridad aplicada al endpoint.
     *
     * @return política de seguridad que debe cumplirse para acceder
     */
    SecurityPolicy policy();
}

