package com.project.Smart.Clinic.Management.services;

import com.project.Smart.Clinic.Management.models.Appointment;
import com.project.Smart.Clinic.Management.models.Doctor;
import com.project.Smart.Clinic.Management.models.Prescription;
import com.project.Smart.Clinic.Management.repo.AppointmentRepository;
import com.project.Smart.Clinic.Management.repo.DoctorRepository;
import com.project.Smart.Clinic.Management.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               DoctorRepository doctorRepository,
                               TokenService tokenService) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription, String token) {
        ResponseEntity<Map<String, String>> denied = checkAccess(prescription.getAppointmentId(), token);
        if (denied != null) {
            return denied;
        }

        List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
        if (!existing.isEmpty()) {
            return message(HttpStatus.CONFLICT, "A prescription already exists for this appointment");
        }

        prescription.setId(null);
        prescriptionRepository.save(prescription);
        return message(HttpStatus.CREATED, "Prescription saved");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId, String token) {
        ResponseEntity<Map<String, String>> denied = checkAccess(appointmentId, token);
        if (denied != null) {
            return ResponseEntity.status(denied.getStatusCode())
                    .body(new HashMap<String, Object>(denied.getBody()));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("prescription", prescriptionRepository.findByAppointmentId(appointmentId));
        return ResponseEntity.ok(body);
    }

    /** Returns null when the doctor behind the token may access the appointment, otherwise the error response. */
    private ResponseEntity<Map<String, String>> checkAccess(Long appointmentId, String token) {
        if (appointmentId == null) {
            return message(HttpStatus.BAD_REQUEST, "Appointment id is required");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return message(HttpStatus.NOT_FOUND, "Appointment not found");
        }

        Doctor doctor = doctorRepository.findByEmail(tokenService.extractEmail(token));
        if (doctor == null
                || appointment.getDoctor() == null
                || !doctor.getId().equals(appointment.getDoctor().getId())) {
            return message(HttpStatus.FORBIDDEN, "You are not allowed to access this appointment");
        }
        return null;
    }

    private static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }
}