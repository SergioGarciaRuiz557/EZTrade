# Security Module

## Purpose

`security` protects the backend with Spring Security and JWT. It is responsible for authenticating users, generating tokens, validating HTTP requests, and preparing security for WebSocket/STOMP.

## Main Components

### Controller and DTOs

- `AuthController`: exposes `POST /auth/login`.
- `LoginRequest`: input credentials. Accepts email or username together with password.
- `JwtResponse`: token and `Bearer` type.

### Service

`AuthService` orchestrates login:

1. Builds a `UsernamePasswordAuthenticationToken`.
2. Delegates validation to `AuthenticationManager`.
3. Generates a JWT with `JwtService`.
4. Returns the token to the controller.

### JWT

- `JwtService`: generates tokens, extracts claims, validates signature and expiration.
- `JwtAuthenticationProvider`: bridge to the user module for loading `UserDetails`.

### Filters

- `JwtAuthFilter`: reads `Authorization: Bearer <token>`, validates the JWT, and fills `SecurityContextHolder`.
- `UserAccessFilter`: applies additional rules for access to user resources.
- `StompAuthChannelInterceptor`: validates authentication in STOMP messages.

### Configuration

- `AuthenticationConfig`: configures stateless sessions, public routes, and filters.
- `BeansConfig`: declares password encoder, authentication manager, provider, and evaluator.
- `WebSocketConfig`: configures the STOMP broker and WebSocket endpoints.
- `SecuredEndpoint` and `SecurityPolicy`: model semantic access policies.
- `HttpObservabilityConfig`: customizes HTTP observability.

## Endpoints

Public:

- `POST /api/user/register`
- `POST /auth/login`

Protected:

- all other REST endpoints unless explicitly configured otherwise.

## Login Flow

1. The client sends credentials to `/auth/login`.
2. `AuthController` delegates to `AuthService`.
3. Spring Security loads the user through user.
4. The password is validated.
5. `JwtService` generates the token.
6. The client receives `JwtResponse`.

## Protected Request Flow

1. The client sends `Authorization: Bearer <jwt>`.
2. `JwtAuthFilter` extracts and validates the token.
3. Authentication is created in `SecurityContextHolder`.
4. Spring Security evaluates URL rules.
5. The controller receives `Authentication` with the user name.
