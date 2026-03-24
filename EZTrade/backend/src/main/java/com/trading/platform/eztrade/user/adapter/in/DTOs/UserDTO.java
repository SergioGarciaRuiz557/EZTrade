package com.trading.platform.eztrade.user.adapter.in.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * DTO que representa los datos de un usuario recibidos o enviados
 * a través de la capa de entrada de la aplicación.
 * <p>
 * Incluye anotaciones de validación para asegurar que los campos
 * mínimos requeridos estén informados y cumplan las restricciones
 * de formato y longitud.
 */
public class UserDTO {

    /**
     * Nombre del usuario.
     * <p>
     * Campo obligatorio, no puede ser nulo ni estar en blanco.
     */
    @NotNull(message = "The firstname is mandatory")
    @NotBlank(message = "The firstname is mandatory")
    private String firstname;

    /**
     * Apellidos del usuario.
     * <p>
     * Campo obligatorio, no puede ser nulo ni estar en blanco.
     */
    @NotNull(message = "The lastname is mandatory")
    @NotBlank(message = "The lastname is mandatory")
    private String lastname;

    /**
     * Nombre de usuario del usuario.
     * <p>
     * Campo obligatorio y único a nivel de persistencia.
     */
    @NotNull(message = "The username is mandatory")
    @NotBlank(message = "The username is mandatory")
    private String username;

    /**
     * Correo electrónico del usuario.
     * <p>
     * Campo obligatorio, debe tener un formato de email válido
     * y ser único en el sistema.
     */
    @NotNull(message = "The email is mandatory")
    @NotBlank(message = "The email is mandatory")
    @Email(message = "The given email does not match the pattern")
    @UniqueElements()
    private String email;

    /**
     * Contraseña del usuario.
     * <p>
     * Campo obligatorio, no puede ser nulo ni estar en blanco y
     * debe tener al menos 5 caracteres de longitud.
     */
    @NotNull(message = "The password is mandatory")
    @NotBlank(message = "The password is mandatory")
    @Length(min = 5, message = "The password should be at least of 5 characters of length")
    private String password;

    /**
     * Constructor sin argumentos requerido por algunos frameworks
     * de serialización y deserialización.
     */
    public UserDTO() {}

    /**
     * Constructor completo para inicializar todos los campos del DTO.
     *
     * @param firstname nombre del usuario
     * @param lastname apellidos del usuario
     * @param username nombre de usuario
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     */
    public UserDTO(String firstname, String lastname, String username, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Devuelve el nombre del usuario.
     *
     * @return nombre del usuario
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param firstname nombre del usuario
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Devuelve los apellidos del usuario.
     *
     * @return apellidos del usuario
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Establece los apellidos del usuario.
     *
     * @param lastname apellidos del usuario
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Devuelve el nombre de usuario.
     *
     * @return nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    /**
     * Establece el nombre de usuario.
     *
     * @param username nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Devuelve el correo electrónico del usuario.
     *
     * @return correo electrónico del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param email correo electrónico del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param password contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }
}



