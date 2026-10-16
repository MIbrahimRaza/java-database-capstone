package com.project.Smart.Clinic.Management.repo;

import com.project.Smart.Clinic.Management.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Appointments of a doctor with start >= start and < end (end exclusive). */
    @Query("SELECT a FROM Appointment a "
            + "WHERE a.doctor.id = :doctorId "
            + "AND a.appointmentTime >= :start AND a.appointmentTime < :end "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDoctorIdInTimeRange(@Param("doctorId") Long doctorId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.doctor.id = :doctorId "
            + "AND LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%')) "
            + "AND a.appointmentTime >= :start AND a.appointmentTime < :end "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDoctorIdAndPatientNameInTimeRange(@Param("doctorId") Long doctorId,
                                                              @Param("patientName") String patientName,
                                                              @Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.patient.id = :patientId "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.patient.id = :patientId AND a.status = :status "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByPatientIdAndStatus(@Param("patientId") Long patientId,
                                               @Param("status") int status);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.patient.id = :patientId "
            + "AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName,
                                                     @Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.patient.id = :patientId AND a.status = :status "
            + "AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("doctorName") String doctorName,
                                                              @Param("patientId") Long patientId,
                                                              @Param("status") int status);

    @Query("SELECT a FROM Appointment a "
            + "WHERE a.patient.id = :patientId AND a.doctor.id = :doctorId "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByPatientIdAndDoctorId(@Param("patientId") Long patientId,
                                                 @Param("doctorId") Long doctorId);
}