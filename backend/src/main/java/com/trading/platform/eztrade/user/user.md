# User Module

## Purpose

`user` manages platform users, their basic data, and their role. It also exposes a public API so security can load users without depending on internal persistence.

## Domain

- `User`: domain model with id, first name, last name, username, email, password, and role.
- `Role`: enum of functional roles.
- `UserExistsException`: thrown when registering a duplicated email or username.
- `UserNotFoundException`: thrown when no user is found by email or username.

The domain does not use JPA or Spring Security.

## Application

`UserService` implements:

- `RegisterUserUserCase`: registers users, encodes the password, and assigns `Role.USER`.
- `GetUserUserCase`: searches by email or username.

Output port:

- `UserRepository`: contract required by the application to persist and query users.

## Input Adapters

`UserController` exposes:

| Method | Route | Use |
|---|---|---|
| `POST` | `/api/user/register` | Registers a new user. |
| `GET` | `/api/user?email=...` | Queries a user by email or username. |

`UserDTO` is the HTTP contract, and `UserMapper` translates between DTO and domain.

## Output Adapters

- `UserRepository`: implementation of the application port using Spring Data.
- `JpaUserRepository`: Spring Data repository.
- `UserJpaEntity`: entity for the `user` table.
- `UserJpaMapper`: translates domain <-> JPA entity.
- `LoadUserForSecurityAdapter`: transforms a domain user into `UserDetails`.
- `UserOwnerLookupAdapter`: allows other modules to check existence/ownership.

## Registration Flow

1. `UserController` receives `UserDTO`.
2. `UserMapper` converts it into `User`.
3. `UserService` validates duplicates by email and username.
4. It encodes the password with `PasswordEncoder`.
5. It assigns `Role.USER`.
6. It persists through `UserRepository`.
7. It returns the DTO to the client.

## Login Flow from Security

1. Security requests the user by email or username.
2. `LoadUserForSecurityAdapter` delegates to the user port.
3. The user is adapted to `UserDetails`.
4. Spring Security validates password and roles.
