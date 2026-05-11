package com.trading.platform.eztrade.user.api;

import org.springframework.modulith.NamedInterface;

import java.util.Optional;

/**
 * API publica para resolver el identificador canonico de un usuario.
 */
@NamedInterface
public interface UserOwnerLookupPort {

    /**
     * Resuelve email o username a un owner canonico para otros modulos.
     */
    Optional<String> findOwner(String identifier);
}
