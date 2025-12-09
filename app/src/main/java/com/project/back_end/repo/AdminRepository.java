package com.project.back_end.repo;

import com.project.back_end.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * Find an admin by their username
     * @param username the username to search for
     * @return the Admin entity if found, null otherwise
     */
    Admin findByUsername(String username);
}