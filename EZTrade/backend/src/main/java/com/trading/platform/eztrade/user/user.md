# Documentacion del modulo User

El modulo `user` sigue una arquitectura hexagonal: el dominio contiene el
agregado `User` y el enum `Role`, la capa de aplicacion orquesta los casos de
uso, y los adaptadores traducen HTTP, persistencia y seguridad.

## Dominio

`domain/User.java` es un POJO de dominio. No esta anotado con JPA ni implementa
interfaces de Spring Security.

```java
public class User {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Role role;
}
```

Las excepciones `UserExistsException` y `UserNotFoundException` expresan reglas
del dominio sin depender de infraestructura.

## Aplicacion

`UserService` implementa `RegisterUserUserCase` y `GetUserUserCase`, usando el
puerto de salida `UserRepository` para persistir y consultar usuarios.

## Adaptadores

`adapter/in/UserController.java` expone la API REST y usa `UserDTO` para no
publicar directamente el modelo interno.

`adapter/out/persistence/jpa/UserJpaEntity.java` representa la tabla `user` y
`UserJpaMapper.java` traduce entre la entidad JPA y el agregado de dominio.

`adapter/out/LoadUserForSecurityAdapter.java` adapta un `User` de dominio a
`UserDetails`, manteniendo el acoplamiento con Spring Security fuera del dominio.
