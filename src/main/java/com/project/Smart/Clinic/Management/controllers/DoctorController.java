package com.project.Smart.Clinic.Management.controllers;

import com.project.Smart.Clinic.Management.DTO.Login;
import com.project.Smart.Clinic.Management.models.Doctor;
import com.project.Smart.Clinic.Management.services.DoctorService;
import com.project.Smart.Clinic.Management.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> body = new HashMap<>();
        body.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@PathVariable String token,
                                                          @Valid @RequestBody Doctor doctor) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "admin");
        if (isDenied(auth)) {
            return auth;
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return message(HttpStatus.CREATED, "Doctor added successfully");
        }
        if (result == -1) {
            return message(HttpStatus.CONFLICT, "A doctor with this email already exists");
        }
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save the doctor");
    }

    @DeleteMapping("/delete/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id,
                                                            @PathVariable String token) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "admin");
        if (isDenied(auth)) {
            return auth;
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return message(HttpStatus.OK, "Doctor deleted successfully");
        }
        if (result == -1) {
            return message(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete the doctor");
    }

    @GetMapping("/filter/{name}/{time}/{specialty}")
    public ResponseEntity<Map<String, Object>> filterDoctors(@PathVariable String name,
                                                             @PathVariable String time,
                                                             @PathVariable String specialty) {
        return ResponseEntity.ok(service.filterDoctor(name, specialty, time));
    }

    private static boolean isDenied(ResponseEntity<Map<String, String>> auth) {
        return auth.getBody() != null && !auth.getBody().isEmpty();
    }

    private static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }
}