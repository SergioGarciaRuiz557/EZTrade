package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for the user module.
 * <p>
 * Represents the {@code user} table. The {@code User} domain object remains a
 * separate POJO so user rules are not coupled to persistence annotations.
 */
@Entity
@Table(name = "user")
public class UserJpaEntity {

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

    /** Constructor required by JPA. */
    protected UserJpaEntity() {
    }

    public UserJpaEntity(Long id,
                         String name,
                         String surname,
                         String username,
                         String email,
                         String password,
                         Role role) {
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

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
