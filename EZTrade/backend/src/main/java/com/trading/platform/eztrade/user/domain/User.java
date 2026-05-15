package com.trading.platform.eztrade.user.domain;

/**
 * Agregado de dominio del modulo User.
 * <p>
 * Mantiene los datos e invariantes propios del usuario sin depender de JPA ni
 * de Spring Security. La persistencia y la adaptacion a UserDetails viven en
 * adaptadores externos.
 */
public class User {

    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Role role;

    public User() {
    }

    public User(String name, String surname, String username, String email, String password) {
        this(null, name, surname, username, email, password, null);
    }

    public User(String name, String surname, String email, String password) {
        this(null, name, surname, null, email, password, null);
    }

    public User(Long id, String name, String surname, String username, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsernameValue() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsernameValue(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
