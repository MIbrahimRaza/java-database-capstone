package com.project.Smart.Clinic.Management.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "name cannot be null")
    @Size(min = 3, max = 100, message = "name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "email cannot be null")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotNull(message = "password cannot be null")
    @Size(min = 6, message = "password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "phone cannot be null")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    @NotNull(message = "address cannot be null")
    @Size(max = 255, message = "address cannot exceed 255 characters")
    private String address;

    // Extra fields
    @Past(message = "dateOfBirth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "\\d{10}", message = "emergencyContact must be 10 digits")
    private String emergencyContact;

    @Size(max = 100, message = "insuranceProvider cannot exceed 100 characters")
    private String insuranceProvider;
}