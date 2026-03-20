package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.WalletAccount;

import java.util.Optional;

/**
 * Puerto de salida para persistir y recuperar {@link WalletAccount}.
 * <p>
 * Se define como interfaz para desacoplar la aplicación del mecanismo de persistencia (JPA u otro).
 */
public interface WalletAccountRepositoryPort {

    /** Devuelve la cuenta del owner si existe (sin bloquear). */
    Optional<WalletAccount> findByOwner(String owner);

    /**
     * Devuelve la cuenta del owner aplicando un mecanismo de exclusión mutua (bloqueo) si el adaptador lo soporta.
     * <p>
     * Se usa cuando vamos a modificar balances para evitar condiciones de carrera.
     */
    Optional<WalletAccount> findByOwnerForUpdate(String owner);

    /** Guarda la cuenta (insert/update) y devuelve el estado persistido. */
    WalletAccount save(WalletAccount walletAccount);
}

