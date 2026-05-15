# Modulo Security

## Proposito

`security` protege el backend con Spring Security y JWT. Es responsable de autenticar usuarios, generar tokens, validar peticiones HTTP y preparar seguridad para WebSocket/STOMP.

## Componentes principales

### Controller y DTOs

- `AuthController`: expone `POST /auth/login`.
- `LoginRequest`: credenciales de entrada. Acepta email o username junto a password.
- `JwtResponse`: token y tipo `Bearer`.

### Servicio

`AuthService` orquesta login:

1. Construye un `UsernamePasswordAuthenticationToken`.
2. Delega validacion en `AuthenticationManager`.
3. Genera JWT con `JwtService`.
4. Devuelve el token al controlador.

### JWT

- `JwtService`: genera tokens, extrae claims, valida firma y expiracion.
- `JwtAuthenticationProvider`: puente hacia el modulo user para cargar `UserDetails`.

### Filtros

- `JwtAuthFilter`: lee `Authorization: Bearer <token>`, valida JWT y rellena `SecurityContextHolder`.
- `UserAccessFilter`: aplica reglas adicionales para acceso a recursos de usuario.
- `StompAuthChannelInterceptor`: valida autenticacion en mensajes STOMP.

### Configuracion

- `AuthenticationConfig`: configura stateless sessions, rutas publicas y filtros.
- `BeansConfig`: declara password encoder, authentication manager, provider y evaluator.
- `WebSocketConfig`: configura broker STOMP y endpoints WebSocket.
- `SecuredEndpoint` y `SecurityPolicy`: modelan politicas semanticas de acceso.
- `HttpObservabilityConfig`: personaliza observabilidad HTTP.

## Endpoints

Publicos:

- `POST /api/user/register`
- `POST /auth/login`

Protegidos:

- resto de endpoints REST salvo configuracion explicita.

## Flujo de login

1. Cliente envia credenciales a `/auth/login`.
2. `AuthController` delega en `AuthService`.
3. Spring Security carga usuario mediante user.
4. Se valida password.
5. `JwtService` genera token.
6. Cliente recibe `JwtResponse`.

## Flujo de peticion protegida

1. Cliente envia `Authorization: Bearer <jwt>`.
2. `JwtAuthFilter` extrae y valida el token.
3. Se crea autenticacion en `SecurityContextHolder`.
4. Spring Security evalua reglas de URL.
5. El controlador recibe `Authentication` con el nombre del usuario.
