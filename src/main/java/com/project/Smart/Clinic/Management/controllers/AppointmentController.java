package com.project.Smart.Clinic.Management.controllers;

import com.project.Smart.Clinic.Management.models.Appointment;
import com.project.Smart.Clinic.Management.services.AppointmentService;
import com.project.Smart.Clinic.Management.services.Service;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "doctor");
        if (isDenied(auth)) {
            return ResponseEntity.status(auth.getStatusCode())
                    .body(new HashMap<String, Object>(auth.getBody()));
        }
        return appointmentService.getAppointments(date, patientName, token);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token,
                                                               @RequestBody Appointment appointment) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "patient");
        if (isDenied(auth)) {
            return auth;
        }
        return appointmentService.bookAppointment(appointment, token);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@PathVariable String token,
                                                                 @RequestBody Appointment appointment) {
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "patient");
        if (isDenied(auth)) {
            return auth;
        }
        return appointmentService.updateAppointment(appointment, token);
    }

    private static boolean isDenied(ResponseEntity<Map<String, String>> auth) {
        return auth.getBody() != null && !auth.getBody().isEmpty();
    }
}