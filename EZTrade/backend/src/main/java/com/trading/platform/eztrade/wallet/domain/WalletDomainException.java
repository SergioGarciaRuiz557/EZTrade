package com.trading.platform.eztrade.wallet.domain;

/**
 * Excepción de dominio para el módulo Wallet.
 * <p>
 * Se lanza cuando se viola una regla de negocio o un invariante del modelo (p. ej. intentar retirar más saldo del
 * disponible, reservar fondos sin balance suficiente, importes nulos/negativos, owners vacíos, etc.).
 * <p>
 * Es una {@link RuntimeException} porque representa un error de uso del dominio dentro del mismo proceso y se espera
 * que la capa de aplicación lo traduzca si es necesario (p. ej. a un error de API).
 */
public class WalletDomainException extends RuntimeException {

    public WalletDomainException(String message) {
        super(message);
    }
}

