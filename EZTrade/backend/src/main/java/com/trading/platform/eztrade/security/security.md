# Documentación del Módulo de Seguridad (Security)

Este documento detalla el diseño, flujo de trabajo (workflow) y las clases principales que componen el módulo de seguridad de la aplicación EZTrade. El módulo está diseñado utilizando **Spring Security** y **JWT (JSON Web Tokens)** para ofrecer una autenticación *stateless* (sin estado).

---

## 🚀 Workflow de Seguridad

El workflow de seguridad se divide principalmente en dos flujos: **Inicio de sesión (Autenticación)** y **Acceso a recursos (Autorización)**.

### 1. Flujo de Inicio de Sesión (Login)
1. **Petición del cliente**: El usuario envía sus credenciales (email y contraseña) a través del endpoint `POST /auth/login`.
2. **Controlador**: `AuthController` recibe el `LoginRequest` y delega la operación al `AuthService`.
3. **Validación de Credenciales**: El `AuthService` utiliza el `AuthenticationManager` de Spring Security para validar la coincidencia del email y contraseña.
4. **Carga de Usuario**: Durante la validación, el `AuthenticationProvider` se apoya en `JwtAuthenticationProvider` (que implementa la comunicación con el módulo de usuarios) para cargar los datos en un `UserDetails`.
5. **Generación del Token**: Una vez autenticado, `AuthService` pide a `JwtService` que genere un token JWT con la información del usuario.
6. **Respuesta**: Se devuelve el token encapsulado en un `JwtResponse` al cliente.

### 2. Flujo de Acceso a Recursos Protegidos
1. **Petición con Token**: El cliente realiza una petición a un endpoint protegido (ej. `/api/v1/market`) incluyendo el token JWT en la cabecera `Authorization: Bearer <token>`.
2. **Filtro JWT (`JwtAuthFilter`)**: Este filtro intercepta la petición, extrae el token y valida su integridad y expiración a través de `JwtService`. 
3. **Contexto de Seguridad**: Si el token es válido, se extrae el email y se genera un `UsernamePasswordAuthenticationToken`, que se almacena en el `SecurityContextHolder` para la petición actual.
4. **Filtro de Acceso Personalizado (`UserAccessFilter`)**: Se ejecuta después del filtro JWT. Si el usuario intenta acceder a `/api/user`, este filtro verifica que el usuario autenticado es un `ADMIN` o es el propio dueño del recurso solicitado.
5. **Control de Acceso Final**: Spring Security (configurado en `AuthenticationConfig`) revisa las reglas a nivel de URL antes de delegar la petición al controlador correspondiente.

---

## 📂 Clases del Módulo y Responsabilidades

### 1. Controller & DTOs (`controller/`, `dto/`)

#### `AuthController`
Controlador que expone el endpoint público de autenticación.
```java
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
    String token = authService.login(request.getEmail(), request.getPassword());
    return ResponseEntity.ok(new JwtResponse(token));
}
```

#### `LoginRequest` & `JwtResponse`
Objetos de transferencia de datos utilizados para recibir credenciales y devolver el token JWT generado, respectivamente.

### 2. Servicios (`service/`)

#### `AuthService`
Servicio que orquesta la lógica de autenticación invocando al `AuthenticationManager` y generando el JWT en caso de éxito.
```java
public String login(String email, String password) {
    // 1. Validar credenciales
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
    );
    // 2. Generar JWT
    return jwtService.generateToken((UserDetails) authentication.getPrincipal());
}
```

### 3. JWT (`jwt/`)

#### `JwtService`
Encapsula toda la lógica criptográfica de JWT: extracción de *claims*, generación de tokens y validación comprobando su caducidad y firma secreta.
```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

public String generateToken(UserDetails userDetails) {
    // Genera el token con una expiración (Ej: una ventana de 24 horas)
    return generateToken(new HashMap<>(), userDetails);
}
```

#### `JwtAuthenticationProvider`
Actúa como puente para inyectar un usuario del dominio (a través del puerto `LoadUserForSecurityPort`) en la infraestructura de Spring Security devolviendo un `UserDetails`.
```java
public UserDetails loadByUsername(String username) {
    return userPort.loadByUsername(username); // Delega la carga al módulo User
}
```

### 4. Filtros (`filter/`)

#### `JwtAuthFilter`
Hereda de `OncePerRequestFilter`. Se encarga de inspeccionar cada solicitud HTTP, extraer la cabecera y validar el token usando el `JwtService`.
```java
protected void doFilterInternal(...) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response); return;
    }
    String jwt = authHeader.substring(7);
    String userEmail = jwtService.extractUsername(jwt);
    // ... Si es válido, lo establece en el SecurityContextHolder
}
```

#### `UserAccessFilter`
Filtro personalizado para aplicar autorización a nivel de dominio en particular para operaciones sobre usuarios. Evalúa si el solicitante puede ver la información validando si es `ROLE_ADMIN` o si el parámetro `email` coincide con el suyo propio.
```java
if ("/api/user".equals(path) && "GET".equalsIgnoreCase(method)) {
    String email = request.getParameter("email");
    if (!permissionEvaluator.isAdminOrSameUser(authentication, email)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); return;
    }
}
```

### 5. Configuración (`configuration/`)

#### `AuthenticationConfig`
Configuración central e indispensable de Web Security. Es aquí donde se define que la gestión de sesiones sea `STATELESS` (vital para aplicaciones basadas en JWT) y se indican las rutas públicas frente a las privadas.
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/user/register", "/auth/login").permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(userAccessFilter, JwtAuthFilter.class)
            .build();
}
```

#### `BeansConfig`
Concentra la declaración de beans requeridos para inyección de dependencias, como el codificador de contraseñas (`BCryptPasswordEncoder`), el `AuthenticationManager`, el proveedor principal de autenticación acoplándolo a la lógica de cargar usuarios (`DaoAuthenticationProvider`), y el `SecurityPermissionEvaluator`.
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### `SecuredEndpoint` & `SecurityPolicy`
Elementos base para definir contratos y políticas base de seguridad (público, autenticado, administrador) que podrían usarse en futuras mejoras de enrutamiento basado en políticas u roles.

