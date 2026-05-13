# Modulo User

## Proposito

`user` gestiona los usuarios de la plataforma, sus datos basicos y su rol. Tambien expone una API publica para que security pueda cargar usuarios sin depender de la persistencia interna.

## Dominio

- `User`: modelo de dominio con id, nombre, apellidos, username, email, password y rol.
- `Role`: enum de roles funcionales.
- `UserExistsException`: se lanza al registrar email o username duplicado.
- `UserNotFoundException`: se lanza cuando no se encuentra un usuario por email o username.

El dominio no usa JPA ni Spring Security.

## Aplicacion

`UserService` implementa:

- `RegisterUserUserCase`: registra usuarios, codifica password y asigna `Role.USER`.
- `GetUserUserCase`: busca por email o username.

Puerto de salida:

- `UserRepository`: contrato que necesita la aplicacion para persistir y consultar usuarios.

## Adaptadores de entrada

`UserController` expone:

| Metodo | Ruta | Uso |
|---|---|---|
| `POST` | `/api/user/register` | Registra un usuario nuevo. |
| `GET` | `/api/user?email=...` | Consulta un usuario por email o username. |

`UserDTO` es el contrato HTTP y `UserMapper` traduce entre DTO y dominio.

## Adaptadores de salida

- `UserRepository`: implementacion del puerto de aplicacion usando Spring Data.
- `JpaUserRepository`: repositorio Spring Data.
- `UserJpaEntity`: entidad de la tabla `user`.
- `UserJpaMapper`: traduce dominio <-> entidad JPA.
- `LoadUserForSecurityAdapter`: transforma un usuario de dominio en `UserDetails`.
- `UserOwnerLookupAdapter`: permite comprobar existencia/propiedad desde otros modulos.

## Flujo de registro

1. `UserController` recibe `UserDTO`.
2. `UserMapper` lo convierte en `User`.
3. `UserService` valida duplicados por email y username.
4. Codifica password con `PasswordEncoder`.
5. Asigna `Role.USER`.
6. Persiste mediante `UserRepository`.
7. Devuelve DTO al cliente.

## Flujo de login desde security

1. Security solicita usuario por email o username.
2. `LoadUserForSecurityAdapter` delega en el puerto de user.
3. El usuario se adapta a `UserDetails`.
4. Spring Security valida password y roles.
