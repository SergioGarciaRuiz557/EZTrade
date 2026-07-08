/**
 * Application security module.
 * <p>
 * Groups the components responsible for authentication, authorization, and JWT
 * token management, integrated with Spring Security.
 * <p>
 * Dependency constraints (Spring Modulith):
 * <ul>
 *   <li>May only depend on the {@code user :: api} module.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"user :: api"}
)
package com.trading.platform.eztrade.security;

import org.springframework.modulith.ApplicationModule;

