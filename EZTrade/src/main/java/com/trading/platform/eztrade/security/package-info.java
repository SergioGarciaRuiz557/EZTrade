/**
 * Módulo de seguridad de la aplicación.
 * <p>
 * Agrupa los componentes responsables de la autenticación, autorización
 * y gestión de tokens JWT, integrados con Spring Security.
 * <p>
 * Restricciones de dependencias (Spring Modulith):
 * <ul>
 *   <li>Solo puede depender del módulo {@code user :: api}.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"user :: api"}
)
package com.trading.platform.eztrade.security;

import org.springframework.modulith.ApplicationModule;

