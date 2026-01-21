package com.trading.platform.eztrade.user.adapter.in.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;


public class UserDTO {
    @NotNull(message = "The firstname is mandatory")
    @NotBlank(message = "The firstname is mandatory")
    private String firstname;
    @NotNull(message = "The lastname is mandatory")
    @NotBlank(message = "The lastname is mandatory")
    private String lastname;
    @NotNull(message = "The email is mandatory")
    @NotBlank(message = "The email is mandatory")
    @Email(message = "The given email does not match the pattern")
    @UniqueElements()
    private String email;
    @NotNull(message = "The password is mandatory")
    @NotBlank(message = "The password is mandatory")
    @Length(min = 5, message = "The password should be at least of 5 characters of length")
    private String password;

    public UserDTO() {}

    public UserDTO(String firstname, String lastname, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
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
}


