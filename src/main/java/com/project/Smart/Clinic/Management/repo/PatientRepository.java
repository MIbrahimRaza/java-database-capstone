package com.project.Smart.Clinic.Management.repo;

import com.project.Smart.Clinic.Management.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);

    boolean existsByEmailOrPhone(String email, String phone);
}