package com.vdprog.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Integer Id;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotNull(message = "Birth date cannot be null")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    private String address;
    private String phoneNumber;

    public User(User other) {
        this.Id = other.Id;
        this.email = other.email;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.birthDate = other.birthDate;
        this.address = other.address;
        this.phoneNumber = other.phoneNumber;
    }
}