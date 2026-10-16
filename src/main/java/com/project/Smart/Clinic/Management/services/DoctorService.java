package com.project.Smart.Clinic.Management.services;

import com.project.Smart.Clinic.Management.DTO.Login;
import com.project.Smart.Clinic.Management.models.Appointment;
import com.project.Smart.Clinic.Management.models.Doctor;
import com.project.Smart.Clinic.Management.repo.AppointmentRepository;
import com.project.Smart.Clinic.Management.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@org.springframework.stereotype.Service
public class DoctorService {

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern SLOT_PATTERN = Pattern.compile("^\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}$");

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Parses the start of a slot such as "09:00-10:00". */
    public static LocalTime slotStart(String slot) {
        String start = slot.split("-")[0].trim();
        return LocalTime.parse(start, SLOT_FORMAT);
    }

    public boolean doctorExists(Long doctorId) {
        return doctorId != null && doctorRepository.existsById(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        initialize(doctors);
        return doctors;
    }

    /** 1 = saved, -1 = a doctor with this email already exists. */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -1;
        }
        if (doctor.getAvailableTimes() != null) {
            for (String slot : doctor.getAvailableTimes()) {
                if (slot == null || !SLOT_PATTERN.matcher(slot).matches()) {
                    throw new IllegalArgumentException("Invalid time slot format, expected HH:mm-HH:mm");
                }
            }
        }
        doctor.setId(null);
        doctorRepository.save(doctor);
        return 1;
    }

    /** 1 = deleted, -1 = doctor not found. */
    @Transactional
    public int deleteDoctor(Long id) {
        if (id == null || !doctorRepository.existsById(id)) {
            return -1;
        }
        appointmentRepository.deleteAllByDoctorId(id);
        doctorRepository.deleteById(id);
        return 1;
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> body = new HashMap<>();
        if (login == null || login.getEmail() == null || login.getPassword() == null) {
            body.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor == null || doctor.getPassword() == null
                || !MessageDigest.isEqual(
                doctor.getPassword().getBytes(StandardCharsets.UTF_8),
                login.getPassword().getBytes(StandardCharsets.UTF_8))) {
            body.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        body.put("token", tokenService.generateToken(doctor.getEmail()));
        return ResponseEntity.ok(body);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecialtyAndTime(String name, String specialty, String time) {
        String nameFilter = normalize(name);
        String specialtyFilter = normalize(specialty);
        String period = normalize(time);

        if (period != null) {
            period = period.toUpperCase(Locale.ROOT);
            if (!period.equals("AM") && !period.equals("PM")) {
                throw new IllegalArgumentException("Time must be AM, PM or null");
            }
        }

        List<Doctor> doctors;
        if (nameFilter != null && specialtyFilter != null) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(
                    nameFilter, specialtyFilter);
        } else if (nameFilter != null) {
            doctors = doctorRepository.findByNameContainingIgnoreCase(nameFilter);
        } else if (specialtyFilter != null) {
            doctors = doctorRepository.findBySpecialtyContainingIgnoreCase(specialtyFilter);
        } else {
            doctors = doctorRepository.findAll();
        }

        if (period != null) {
            final String selected = period;
            doctors = doctors.stream().filter(d -> matchesPeriod(d, selected)).toList();
        }

        List<Doctor> result = new ArrayList<>(doctors);
        initialize(result);

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", result);
        return response;
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        return getDoctorAvailability(doctorId, date, null);
    }

    /** Free slots of a doctor on a date; the appointment with excludeAppointmentId is not counted as booked. */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date, Long excludeAppointmentId) {
        List<String> free = new ArrayList<>();
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailableTimes() == null) {
            return free;
        }

        List<Appointment> booked = appointmentRepository.findByDoctorIdInTimeRange(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        Set<LocalTime> bookedStarts = new HashSet<>();
        for (Appointment appointment : booked) {
            if (excludeAppointmentId != null && excludeAppointmentId.equals(appointment.getId())) {
                continue;
            }
            bookedStarts.add(appointment.getAppointmentTime().toLocalTime().withSecond(0).withNano(0));
        }

        for (String slot : doctor.getAvailableTimes()) {
            if (slot == null) {
                continue;
            }
            try {
                if (!bookedStarts.contains(slotStart(slot))) {
                    free.add(slot);
                }
            } catch (DateTimeParseException ignored) {
                // skip malformed slots
            }
        }
        return free;
    }

    private static boolean matchesPeriod(Doctor doctor, String period) {
        if (doctor.getAvailableTimes() == null) {
            return false;
        }
        for (String slot : doctor.getAvailableTimes()) {
            if (slot == null) {
                continue;
            }
            try {
                boolean morning = slotStart(slot).isBefore(LocalTime.NOON);
                if (morning == "AM".equals(period)) {
                    return true;
                }
            } catch (DateTimeParseException ignored) {
                // skip malformed slots
            }
        }
        return false;
    }

    private static void initialize(List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        }
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