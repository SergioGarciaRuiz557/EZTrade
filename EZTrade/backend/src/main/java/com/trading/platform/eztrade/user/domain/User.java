/**
 * <h2>User</h2>
 *
 * <p><strong>Módulo:</strong> User</p>
 * <p><strong>Capa:</strong> Domain</p>
 *
 * <p><strong>Responsabilidad:</strong><br/>
 * Encapsula lógica de negocio pura del dominio.</p>
 *
 * <p><strong>Rol arquitectónico:</strong><br/>
 * Forma parte de la arquitectura hexagonal del módulo, manteniendo separación
 * entre dominio, aplicación e infraestructura mediante puertos y adaptadores.
 * Está gestionado por Spring Modulith.</p>
 */

package com.trading.platform.eztrade.user.domain;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @return resultado devuelto por la operación.
     */

    public User(){}
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param name dato de entrada requerido por la operación.
     * @param surname dato de entrada requerido por la operación.
     * @param email dato de entrada requerido por la operación.
     * @param password dato de entrada requerido por la operación.
     * @return resultado devuelto por la operación.
     */

    public User(String name, String surname, String username, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String name, String surname, String email, String password) {
        this(name, surname, null, email, password);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + role.name();
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */
    public String getUsername() {
        return email;
    }

    @Override
    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */
    public String getPassword() {
        return password;
    }
    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */

    public String getName() {
        return name;
    }
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param name dato de entrada requerido por la operación.
     */

    public void setName(String name) {
        this.name = name;
    }
    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */

    public String getSurname() {
        return surname;
    }
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param surname dato de entrada requerido por la operación.
     */

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsernameValue() {
        return username;
    }

    public void setUsernameValue(String username) {
        this.username = username;
    }

    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */

    public String getEmail() {
        return email;
    }
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param email dato de entrada requerido por la operación.
     */

    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param password dato de entrada requerido por la operación.
     */

    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * Obtiene información del estado interno del objeto.
     * @return resultado devuelto por la operación.
     */

    public Role getRole() {
        return role;
    }
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param role dato de entrada requerido por la operación.
     */

    public void setRole(Role role) {
        this.role = role;
    }
}
