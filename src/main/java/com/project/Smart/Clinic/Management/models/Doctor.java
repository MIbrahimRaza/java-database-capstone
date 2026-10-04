package com.project.Smart.Clinic.Management.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "name cannot be null")
    @Size(min = 3, max = 100, message = "name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "specialty cannot be null")
    @Size(min = 3, max = 50, message = "specialty must be between 3 and 50 characters")
    private String specialty;

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

    @ElementCollection
    private List<String> availableTimes;

    // Extra fields
    @Min(value = 0, message = "yearsOfExperience cannot be negative")
    @Max(value = 60, message = "yearsOfExperience cannot exceed 60")
    private Integer yearsOfExperience;

    @Size(max = 255, message = "clinicAddress cannot exceed 255 characters")
    private String clinicAddress;

    @Min(value = 0, message = "rating cannot be less than 0")
    @Max(value = 5, message = "rating cannot be more than 5")
    private Double rating;
}