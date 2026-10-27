package com.trading.platform.eztrade.user.adapter.in.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * DTO representing user data received or sent through the application's input
 * layer.
 * <p>
 * Includes validation annotations to ensure the required fields are present and
 * satisfy format and length constraints.
 */
public class UserDTO {

    /**
     * User first name.
     * <p>
     * Required field; it cannot be null or blank.
     */
    @NotNull(message = "The firstname is mandatory")
    @NotBlank(message = "The firstname is mandatory")
    private String firstname;

    /**
     * User last name.
     * <p>
     * Required field; it cannot be null or blank.
     */
    @NotNull(message = "The lastname is mandatory")
    @NotBlank(message = "The lastname is mandatory")
    private String lastname;

    /**
     * User username.
     * <p>
     * Required field and unique at the persistence level.
     */
    @NotNull(message = "The username is mandatory")
    @NotBlank(message = "The username is mandatory")
    private String username;

    /**
     * User email address.
     * <p>
     * Required field; it must have a valid email format and be unique in the
     * system.
     */
    @NotNull(message = "The email is mandatory")
    @NotBlank(message = "The email is mandatory")
    @Email(message = "The given email does not match the pattern")
    @UniqueElements()
    private String email;

    /**
     * User password.
     * <p>
     * Required field; it cannot be null or blank and must be at least 5
     * characters long.
     */
    @NotNull(message = "The password is mandatory")
    @NotBlank(message = "The password is mandatory")
    @Length(min = 5, message = "The password should be at least of 5 characters of length")
    private String password;

    /**
     * No-args constructor required by some serialization and deserialization
     * frameworks.
     */
    public UserDTO() {}

    /**
     * Full constructor for initializing every DTO field.
     *
     * @param firstname user first name
     * @param lastname user last name
     * @param username username
     * @param email user email address
     * @param password user password
     */
    public UserDTO(String firstname, String lastname, String username, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the user first name.
     *
     * @return user first name
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Sets the user first name.
     *
     * @param firstname user first name
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Returns the user last name.
     *
     * @return user last name
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Sets the user last name.
     *
     * @param lastname user last name
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Returns the username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the user email address.
     *
     * @return user email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user email address.
     *
     * @param email user email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user password.
     *
     * @return user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     *
     * @param password user password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}



