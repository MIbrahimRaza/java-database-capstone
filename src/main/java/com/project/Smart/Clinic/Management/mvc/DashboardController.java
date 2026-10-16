package com.project.Smart.Clinic.Management.mvc;

import com.project.Smart.Clinic.Management.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        if (isValid(token, "admin")) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        if (isValid(token, "doctor")) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }

    private boolean isValid(String token, String role) {
        Map<String, String> result = service.validateToken(token, role).getBody();
        return result != null && result.isEmpty();
    }
}