package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * Retrieve appointments for a doctor within a given time range
     * @param doctorId the ID of the doctor
     * @param start start of the time range
     * @param end end of the time range
     * @return list of appointments within the specified range
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    /**
     * Filter appointments by doctor ID, partial patient name (case-insensitive), and time range
     * @param doctorId the ID of the doctor
     * @param patientName partial patient name to search for
     * @param start start of the time range
     * @param end end of the time range
     * @return filtered list of appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.patient p " +
           "LEFT JOIN FETCH a.doctor d " +
           "WHERE a.doctor.id = :doctorId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    /**
     * Delete all appointments related to a specific doctor
     * @param doctorId the ID of the doctor
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);
    
    /**
     * Find all appointments for a specific patient
     * @param patientId the ID of the patient
     * @return list of appointments for the patient
     */
    List<Appointment> findByPatientId(Long patientId);
    
    /**
     * Retrieve appointments for a patient by status, ordered by appointment time
     * @param patientId the ID of the patient
     * @param status the status of appointments to find
     * @return sorted list of appointments
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);
    
    /**
     * Search appointments by partial doctor name and patient ID
     * @param doctorName partial doctor name to search for
     * @param patientId the ID of the patient
     * @return filtered list of appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.doctor d " +
           "JOIN a.patient p " +
           "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND p.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);
    
    /**
     * Filter appointments by doctor name, patient ID, and status
     * @param doctorName partial doctor name to search for
     * @param patientId the ID of the patient
     * @param status the status to filter by
     * @return filtered list of appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.doctor d " +
           "JOIN a.patient p " +
           "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND p.id = :patientId " +
           "AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);
}