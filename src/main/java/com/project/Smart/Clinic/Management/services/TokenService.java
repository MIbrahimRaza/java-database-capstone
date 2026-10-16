package com.project.Smart.Clinic.Management.services;

import com.project.Smart.Clinic.Management.repo.AdminRepository;
import com.project.Smart.Clinic.Management.repo.DoctorRepository;
import com.project.Smart.Clinic.Management.repo.PatientRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;

@Component
public class TokenService {

    private static final long EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SecretKey signingKey;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository,
                        @Value("${jwt.secret}") String secret) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.signingKey = buildKey(secret);
    }

    private static SecretKey buildKey(String secret) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }

    /** Subject is the admin username or the doctor/patient email. */
    public String generateToken(String subject) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(signingKey)
                .compact();
    }

    /** Returns the token subject (admin username or user email). Throws JwtException if the token is invalid. */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, String user) {
        if (token == null || token.isBlank() || user == null) {
            return false;
        }
        try {
            String subject = extractEmail(token);
            if (subject == null) {
                return false;
            }
            return switch (user.toLowerCase(Locale.ROOT)) {
                case "admin" -> adminRepository.findByUsername(subject) != null;
                case "doctor" -> doctorRepository.findByEmail(subject) != null;
                case "patient" -> patientRepository.findByEmail(subject) != null;
                default -> false;
            };
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}