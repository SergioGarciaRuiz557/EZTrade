package com.trading.platform.eztrade.user.domain;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad de dominio que representa a un usuario de la plataforma.
 * <p>
 * Implementa {@link UserDetails} para integrarse con el módulo de
 * seguridad de Spring y actuar como sujeto autenticado.
 */
@Entity
@Table(name = "user")
public class User implements UserDetails {

    /**
     * Identificador único del usuario en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de pila del usuario.
     */
    private String name;

    /**
     * Apellidos del usuario.
     */
    private String surname;

    /**
     * Correo electrónico del usuario.
     * <p>
     * Debe ser único en el sistema y se utiliza como nombre de usuario
     * para el proceso de autenticación.
     */
    @Column(unique = true)
    private String email;

    /**
     * Contraseña del usuario codificada.
     */
    private String password;

    /**
     * Rol asignado al usuario dentro de la plataforma.
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    public User() {}

    /**
     * Crea un nuevo usuario con los datos básicos.
     *
     * @param name nombre del usuario
     * @param surname apellidos del usuario
     * @param email correo electrónico único del usuario
     * @param password contraseña (normalmente codificada antes de persistir)
     */
    public User(String name, String surname, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }

    /**
     * Devuelve las autoridades concedidas al usuario a partir de su rol.
     *
     * @return colección con la autoridad correspondiente al rol del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Devuelve el identificador de autenticación del usuario.
     *
     * @return el correo electrónico del usuario
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return contraseña codificada
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Devuelve el nombre del usuario.
     *
     * @return nombre
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param name nombre a asignar
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Devuelve los apellidos del usuario.
     *
     * @return apellidos
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Establece los apellidos del usuario.
     *
     * @param surname apellidos a asignar
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Devuelve el correo electrónico del usuario.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param email email a asignar
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param password contraseña (preferiblemente ya codificada)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Devuelve el rol del usuario.
     *
     * @return rol asignado
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario.
     *
     * @param role rol a asignar
     */
    public void setRole(Role role) {
        this.role = role;
    }
}

