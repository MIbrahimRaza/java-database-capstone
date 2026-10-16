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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service service;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              Service service) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    @Transactional
    public ResponseEntity<Map<String, String>> bookAppointment(Appointment appointment, String token) {
        if (appointment == null
                || appointment.getDoctor() == null || appointment.getDoctor().getId() == null
                || appointment.getPatient() == null || appointment.getPatient().getId() == null
                || appointment.getAppointmentTime() == null) {
            return message(HttpStatus.BAD_REQUEST, "Doctor, patient and appointment time are required");
        }

        Patient tokenPatient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (tokenPatient == null) {
            return message(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        if (!tokenPatient.getId().equals(appointment.getPatient().getId())) {
            return message(HttpStatus.FORBIDDEN, "You can only book appointments for yourself");
        }

        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
        if (doctor == null) {
            return message(HttpStatus.NOT_FOUND, "Doctor not found");
        }

        int validity = service.validateAppointment(doctor.getId(), appointment.getAppointmentTime(), null);
        if (validity == -1) {
            return message(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        if (validity == 0) {
            return message(HttpStatus.CONFLICT, "The selected time slot is not available");
        }

        appointment.setId(null);
        appointment.setDoctor(doctor);
        appointment.setPatient(tokenPatient);
        appointment.setStatus(0);
        appointmentRepository.save(appointment);

        return message(HttpStatus.CREATED, "Appointment booked successfully");
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment, String token) {
        if (appointment == null || appointment.getId() == null) {
            return message(HttpStatus.BAD_REQUEST, "Appointment id is required");
        }

        Appointment existing = appointmentRepository.findById(appointment.getId()).orElse(null);
        if (existing == null) {
            return message(HttpStatus.NOT_FOUND, "Appointment not found");
        }

        Patient tokenPatient = patientRepository.findByEmail(tokenService.extractEmail(token));
        if (tokenPatient == null || !tokenPatient.getId().equals(existing.getPatient().getId())) {
            return message(HttpStatus.FORBIDDEN, "You can only update your own appointments");
        }

        Doctor doctor = existing.getDoctor();
        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
            doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
            if (doctor == null) {
                return message(HttpStatus.NOT_FOUND, "Doctor not found");
            }
        }

        LocalDateTime time = appointment.getAppointmentTime() != null
                ? appointment.getAppointmentTime()
                : existing.getAppointmentTime();

        int validity = service.validateAppointment(doctor.getId(), time, existing.getId());
        if (validity == -1) {
            return message(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        if (validity == 0) {
            return message(HttpStatus.CONFLICT, "The selected time slot is not available");
        }

        existing.setDoctor(doctor);
        existing.setAppointmentTime(time);
        if (appointment.getReasonForVisit() != null) {
            existing.setReasonForVisit(appointment.getReasonForVisit());
        }
        if (appointment.getNotes() != null) {
            existing.setNotes(appointment.getNotes());
        }
        appointmentRepository.save(existing);

        return message(HttpStatus.OK, "Appointment updated successfully");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAppointments(LocalDate date, String patientName, String token) {
        Doctor doctor = doctorRepository.findByEmail(tokenService.extractEmail(token));
        if (doctor == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        String name = normalize(patientName);

        List<Appointment> appointments = (name == null)
                ? appointmentRepository.findByDoctorIdInTimeRange(doctor.getId(), start, end)
                : appointmentRepository.findByDoctorIdAndPatientNameInTimeRange(doctor.getId(), name, start, end);

        List<AppointmentDTO> dtos = appointments.stream().map(AppointmentDTO::from).toList();
        Map<String, Object> body = new HashMap<>();
        body.put("appointments", dtos);
        return ResponseEntity.ok(body);
    }

    private static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("message", text);
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