/**
 * Interfaz pública del módulo Wallet para <strong>eventos de dominio</strong>.
 * <p>
 * Spring Modulith usa {@link org.springframework.modulith.NamedInterface} para declarar qué paquete se considera parte
 * del API del módulo. Este paquete contiene <em>records</em> que representan hechos relevantes del dominio de wallet,
 * típicamente publicados tras una operación (reserva/liberación/liquidación) o ante un fallo de negocio
 * (fondos insuficientes).
 * <p>
 * Nota: los eventos están modelados como objetos simples (records) para facilitar su publicación con el bus de eventos
 * de Spring y su consumo por otros módulos.
 */
@NamedInterface("events")
package com.trading.platform.eztrade.wallet.domain.events;

import org.springframework.modulith.NamedInterface;

