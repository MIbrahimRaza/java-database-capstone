package com.project.Smart.Clinic.Management.controllers;

import com.project.Smart.Clinic.Management.DTO.Login;
import com.project.Smart.Clinic.Management.models.Patient;
import com.project.Smart.Clinic.Management.services.PatientService;
import com.project.Smart.Clinic.Management.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "patient");
        if (isDenied(auth)) {
            return toObjectResponse(auth);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        if (!service.validatePatient(patient)) {
            return message(HttpStatus.CONFLICT, "A patient with this email or phone number already exists");
        }
        if (patientService.createPatient(patient) == 1) {
            return message(HttpStatus.CREATED, "Signup successful");
        }
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to complete signup");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{user}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long id,
                                                                     @PathVariable String user,
                                                                     @PathVariable String token) {
        String role = user == null ? "" : user.toLowerCase(Locale.ROOT);
        if (!role.equals("patient") && !role.equals("doctor")) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "User must be 'patient' or 'doctor'");
            return ResponseEntity.badRequest().body(body);
        }

        ResponseEntity<Map<String, String>> auth = service.validateToken(token, role);
        if (isDenied(auth)) {
            return toObjectResponse(auth);
        }
        return patientService.getPatientAppointment(id, token, role);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(@PathVariable String condition,
                                                                         @PathVariable String name,
                                                                         @PathVariable String token) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "patient");
        if (isDenied(auth)) {
            return toObjectResponse(auth);
        }
        return service.filterPatient(condition, name, token);
    }

    private static boolean isDenied(ResponseEntity<Map<String, String>> auth) {
        return auth.getBody() != null && !auth.getBody().isEmpty();
    }

    private static ResponseEntity<Map<String, Object>> toObjectResponse(ResponseEntity<Map<String, String>> auth) {
        return ResponseEntity.status(auth.getStatusCode())
                .body(new HashMap<String, Object>(auth.getBody()));
    }

    private static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }
}