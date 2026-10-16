package com.project.Smart.Clinic.Management.services;

import com.project.Smart.Clinic.Management.DTO.Login;
import com.project.Smart.Clinic.Management.models.Admin;
import com.project.Smart.Clinic.Management.models.Appointment;
import com.project.Smart.Clinic.Management.models.Patient;
import com.project.Smart.Clinic.Management.repo.AdminRepository;
import com.project.Smart.Clinic.Management.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /** Empty map in the body means the token is valid; otherwise a message with HTTP 401. */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        if (!tokenService.validateToken(token, user)) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        return ResponseEntity.ok(new HashMap<>());
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> body = new HashMap<>();
        if (receivedAdmin == null
                || receivedAdmin.getUsername() == null
                || receivedAdmin.getPassword() == null) {
            body.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
        if (admin == null || !passwordsMatch(admin.getPassword(), receivedAdmin.getPassword())) {
            body.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        body.put("token", tokenService.generateToken(admin.getUsername()));
        return ResponseEntity.ok(body);
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        return doctorService.filterDoctorsByNameSpecialtyAndTime(name, specialty, time);
    }

    /** 1 = valid, 0 = time slot unavailable, -1 = doctor does not exist. */
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            return -1;
        }
        return validateAppointment(appointment.getDoctor().getId(), appointment.getAppointmentTime(), null);
    }

    /** Same as above for a doctor id and time; the appointment with excludeAppointmentId is ignored (updates). */
    public int validateAppointment(Long doctorId, LocalDateTime time, Long excludeAppointmentId) {
        if (doctorId == null || time == null || !doctorService.doctorExists(doctorId)) {
            return -1;
        }

        LocalTime requested = time.toLocalTime().withSecond(0).withNano(0);
        List<String> freeSlots = doctorService.getDoctorAvailability(doctorId, time.toLocalDate(), excludeAppointmentId);

        for (String slot : freeSlots) {
            try {
                if (DoctorService.slotStart(slot).equals(requested)) {
                    return 1;
                }
            } catch (DateTimeParseException ignored) {
                // skip malformed slots
            }
        }
        return 0;
    }

    /** True when neither the email nor the phone number is already registered. */
    public boolean validatePatient(Patient patient) {
        return !patientRepository.existsByEmailOrPhone(patient.getEmail(), patient.getPhone());
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> body = new HashMap<>();
        if (login == null || login.getEmail() == null || login.getPassword() == null) {
            body.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        Patient patient = patientRepository.findByEmail(login.getEmail());
        if (patient == null || !passwordsMatch(patient.getPassword(), login.getPassword())) {
            body.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        body.put("token", tokenService.generateToken(patient.getEmail()));
        return ResponseEntity.ok(body);
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        return patientService.filterPatient(condition, name, token);
    }

    private static boolean passwordsMatch(String stored, String provided) {
        if (stored == null || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}