package com.project.Smart.Clinic.Management.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
public class Prescription {

    @Id
    private String id;

    @NotNull(message = "patientName cannot be null")
    @Size(min = 3, max = 100, message = "patientName must be between 3 and 100 characters")
    private String patientName;

    @NotNull(message = "appointmentId cannot be null")
    private Long appointmentId;

    @NotNull(message = "medication cannot be null")
    @Size(min = 3, max = 100, message = "medication must be between 3 and 100 characters")
    private String medication;

    @NotNull(message = "dosage cannot be null")
    @Size(min = 3, max = 20, message = "dosage must be between 3 and 20 characters")
    private String dosage;

    @Size(max = 200, message = "doctorNotes cannot exceed 200 characters")
    private String doctorNotes;

    // Extra fields
    @Min(value = 0, message = "refillCount cannot be negative")
    private Integer refillCount;

    @Size(max = 100, message = "pharmacyName cannot exceed 100 characters")
    private String pharmacyName;

    public Prescription(String patientName, Long appointmentId, String medication,
                        String dosage, String doctorNotes) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
    }
}