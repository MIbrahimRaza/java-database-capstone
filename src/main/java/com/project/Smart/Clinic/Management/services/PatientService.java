package com.project.Smart.Clinic.Management.services;

import com.project.Smart.Clinic.Management.DTO.AppointmentDTO;
import com.project.Smart.Clinic.Management.models.Appointment;
import com.project.Smart.Clinic.Management.models.Doctor;
import com.project.Smart.Clinic.Management.models.Patient;
import com.project.Smart.Clinic.Management.repo.AppointmentRepository;
import com.project.Smart.Clinic.Management.repo.DoctorRepository;
import com.project.Smart.Clinic.Management.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@org.springframework.stereotype.Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Returns 1 when the patient has been stored. */
    @Transactional
    public int createPatient(Patient patient) {
        patient.setId(null);
        patientRepository.save(patient);
        return 1;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Patient patient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (patient == null) {
            return error(HttpStatus.NOT_FOUND, "Patient not found");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("patient", patient);
        return ResponseEntity.ok(body);
    }

    /**
     * user = "patient": the token owner may only read their own appointments.
     * user = "doctor": the doctor sees this patient's appointments with them.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token, String user) {
        String email = tokenService.extractEmail(token);
        List<Appointment> appointments;

        if ("patient".equalsIgnoreCase(user)) {
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return error(HttpStatus.NOT_FOUND, "Patient not found");
            }
            if (!patient.getId().equals(id)) {
                return error(HttpStatus.FORBIDDEN, "You are not allowed to view these appointments");
            }
            appointments = appointmentRepository.findByPatientId(id);
        } else if ("doctor".equalsIgnoreCase(user)) {
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                return error(HttpStatus.NOT_FOUND, "Doctor not found");
            }
            appointments = appointmentRepository.findByPatientIdAndDoctorId(id, doctor.getId());
        } else {
            return error(HttpStatus.BAD_REQUEST, "User must be 'patient' or 'doctor'");
        }

        return ok(appointments);
    }

    /** condition: "future" (scheduled), "past" (completed) or "null"; name: doctor name or "null". */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Patient patient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (patient == null) {
            return error(HttpStatus.NOT_FOUND, "Patient not found");
        }

        String conditionFilter = normalize(condition);
        String doctorName = normalize(name);

        Integer status = null;
        if (conditionFilter != null) {
            switch (conditionFilter.toLowerCase(Locale.ROOT)) {
                case "future" -> status = 0;
                case "past" -> status = 1;
                default -> {
                    return error(HttpStatus.BAD_REQUEST, "Condition must be 'future', 'past' or 'null'");
                }
            }
        }

        Long patientId = patient.getId();
        List<Appointment> appointments;
        if (status == null && doctorName == null) {
            appointments = appointmentRepository.findByPatientId(patientId);
        } else if (doctorName == null) {
            appointments = appointmentRepository.findByPatientIdAndStatus(patientId, status);
        } else if (status == null) {
            appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
        } else {
            appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(
                    doctorName, patientId, status);
        }

        return ok(appointments);
    }

    private static ResponseEntity<Map<String, Object>> ok(List<Appointment> appointments) {
        List<AppointmentDTO> dtos = appointments.stream().map(AppointmentDTO::from).toList();
        Map<String, Object> body = new HashMap<>();
        body.put("appointments", dtos);
        return ResponseEntity.ok(body);
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        return trimmed;
    }
}