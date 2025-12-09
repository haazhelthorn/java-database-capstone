package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    /**
     * Find a doctor by their email address
     * @param email the email address to search for
     * @return the Doctor entity if found
     */
    Doctor findByEmail(String email);
    
    /**
     * Find doctors by partial name match
     * @param name partial name to search for
     * @return list of doctors matching the name pattern
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> findByNameLike(@Param("name") String name);
    
    /**
     * Filter doctors by partial name and exact specialty (case-insensitive)
     * @param name partial name to search for
     * @param specialty specialty to filter by
     * @return list of filtered doctors
     */
    @Query("SELECT d FROM Doctor d " +
           "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            @Param("name") String name,
            @Param("specialty") String specialty);
    
    /**
     * Find doctors by specialty, ignoring case
     * @param specialty specialty to search for
     * @return list of doctors with the given specialty
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}