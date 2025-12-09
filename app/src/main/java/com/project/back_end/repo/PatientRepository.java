package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Find a patient by their email address
     * @param email the email address to search for
     * @return the Patient entity if found
     */
    Patient findByEmail(String email);
    
    /**
     * Find a patient using either email or phone number
     * @param email the email address to search for
     * @param phone the phone number to search for
     * @return the Patient entity if found
     */
    Patient findByEmailOrPhone(String email, String phone);
}