/**
 * <h2>Role</h2>
 *
 * <p><strong>Módulo:</strong> User</p>
 * <p><strong>Capa:</strong> Domain</p>
 *
 * <p><strong>Responsabilidad:</strong><br/>
 * Encapsula lógica de negocio pura del dominio.</p>
 *
 * <p><strong>Rol arquitectónico:</strong><br/>
 * Forma parte de la arquitectura hexagonal del módulo, manteniendo separación
 * entre dominio, aplicación e infraestructura mediante puertos y adaptadores.
 * Está gestionado por Spring Modulith.</p>
 */

package com.trading.platform.eztrade.user.domain;

public enum Role {
    ADMIN, USER;
}
