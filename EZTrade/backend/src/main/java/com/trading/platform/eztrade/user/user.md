# Documentación del Módulo User

El módulo `user` está diseñado siguiendo los principios de la **Arquitectura Hexagonal (Ports and Adapters)**. Su objetivo principal es gestionar la lógica de negocio y las operaciones relacionadas con los usuarios del sistema (registro, consulta y gestión de roles).

## Workflow (Flujo de Trabajo)
El flujo típico dentro del módulo se divide en tres capas principales:

1. **Adaptadores de Entrada (Inbound Adapters):** Las solicitudes llegan a través del controlador REST (`UserController`).
2. **Puertos y Servicios de Aplicación (Application Layer):** El controlador invoca los puertos de entrada (`GetUserUserCase`, `RegisterUserUserCase`) que son implementados por los servicios (`UserService`).
3. **Dominio (Domain Layer):** El servicio aplica las reglas de negocio utilizando las entidades del dominio (`User`, `Role`) y lanza excepciones específicas si no se cumplen ciertas condiciones (`UserExistsException`, `UserNotFoundException`).
4. **API Pública:** Otros módulos pueden acceder a funcionalidades expuestas mediante puertos de API (p. ej., `LoadUserForSecurityPort`).

---

## Estructura de Clases y Explicación

### Capa de Dominio (`domain/`)
Encapsula la lógica de negocio pura del dominio, sin depender de frameworks externos.

* **`User.java`**: Entidad principal que representa a un usuario en el sistema. Contiene propiedades como el ID, nombre, email, contraseña y rol.
  ```java
  @Entity
  @Table(name = "user")
  public class User implements UserDetails {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      private String name;
      private String email;
      private String password;
      
      @Enumerated(EnumType.STRING)
      private Role role;
      
      // ... getters, setters y lógica adicional ...
  }
  ```

* **`Role.java`**: Enumera los tipos de roles que un usuario puede tener en la plataforma (por ejemplo, `ADMIN`, `USER`).

* **Excepciones (`exceptions/`)**: Representan errores puros del dominio:
  * `UserNotFoundException.java`: Lanzada cuando no se encuentra un usuario consultado.
  * `UserExistsException.java`: Lanzada cuando se intenta registrar un usuario con un email que ya existe en el sistema.

---

### Capa de Aplicación (`application/`)
Coordina los casos de uso, actuando como intermediario entre la capa de dominio y la infraestructura/adaptadores.

* **Puertos de Entrada (`ports/in/`)**: Interfaces que definen los casos de uso disponibles.
  * **`GetUserUserCase.java`**:
    ```java
    public interface GetUserUserCase {
        User getUserById(Long id);
    }
    ```
  * **`RegisterUserUserCase.java`**: Define el caso de uso para registrar nuevos usuarios.

* **Servicios (`services/`)**: Implementan los puertos definidos.
  * **`UserService.java`**: Implementa `GetUserUserCase` y `RegisterUserUserCase`. Orquesta las llamadas al repositorio o adaptadores de salida y gestiona la lógica de la transacción.
    ```java
    @Service
    public class UserService implements GetUserUserCase, RegisterUserUserCase {
        // Implementación de los casos de uso utilizando las entidades del dominio
    }
    ```

---

### Capa de Adaptadores (`adapter/`)
Traducen los datos entre el exterior y el interior del hexágono.

* **`in/UserController.java`**: Controlador REST que expone los endpoints HTTP para realizar operaciones sobre los usuarios.
  ```java
  @RestController
  @RequestMapping("/users")
  public class UserController {
      private final GetUserUserCase getUserUseCase;
      private final RegisterUserUserCase registerUserUseCase;

      // ... endpoints HTTP inyectando los casos de uso ...
  }
  ```

* **`in/DTOs/UserDTO.java`**: Objeto de Transferencia de Datos utilizado para enviar o recibir información desde el cliente, ocultando el modelo real de la base de datos (`User`).

* **`mapper/UserMapper.java`**: Lógica de mapeo para convertir entre `User` (Entidad de dominio) y `UserDTO`.
  ```java
  public class UserMapper {
      public static UserDTO toDTO(User user) {
          // Lógica de mapeo
      }
  }
  ```

* **`in/ExceptionHandlingAdvice.java`**: Intercepta excepciones de la capa de dominio o aplicación (como `UserNotFoundException`) y las convierte en respuestas HTTP estandarizadas con sus respectivos códigos de estado (ej: 404, 400).

---

### API Pública (`api/`)
Expone la funcionalidad que será consumida internamente por otros módulos (por ejemplo, el módulo de `security`).

* **`LoadUserForSecurityPort.java`**: Puerto expuesto para que los mecanismos de seguridad (como los filtros JWT) puedan cargar adecuadamente los detalles del usuario a partir de una sesión o token.
  ```java
  public interface LoadUserForSecurityPort {
      UserDetails loadUserByUsername(String username);
  }
  ```

