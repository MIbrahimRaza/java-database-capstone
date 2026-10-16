package com.project.Smart.Clinic.Management.controllers;

import com.project.Smart.Clinic.Management.models.Prescription;
import com.project.Smart.Clinic.Management.services.PrescriptionService;
import com.project.Smart.Clinic.Management.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;

    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token,
                                                                @Valid @RequestBody Prescription prescription) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "doctor");
        if (isDenied(auth)) {
            return auth;
        }
        return prescriptionService.savePrescription(prescription, token);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId,
                                                               @PathVariable String token) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "doctor");
        if (isDenied(auth)) {
            return ResponseEntity.status(auth.getStatusCode())
                    .body(new HashMap<String, Object>(auth.getBody()));
        }
        return prescriptionService.getPrescription(appointmentId, token);
    }

    private static boolean isDenied(ResponseEntity<Map<String, String>> auth) {
        return auth.getBody() != null && !auth.getBody().isEmpty();
    }
}