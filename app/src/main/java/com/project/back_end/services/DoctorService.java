package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import com.project.back_end.dto.Login;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * Fetches the available slots for a specific doctor on a given date
     */
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Doctor doctor = doctorOpt.get();
        List<String> allAvailableTimes = doctor.getAvailableTimes();
        if (allAvailableTimes == null || allAvailableTimes.isEmpty()) {
            return Collections.emptyList();
        }

        // Get appointments for the doctor on the given date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Extract booked times
        Set<String> bookedTimes = appointments.stream()
                .map(app -> app.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toSet());

        // Filter available times by removing booked times
        return allAvailableTimes.stream()
                .filter(time -> !bookedTimes.contains(time.split(" - ")[0]))
                .collect(Collectors.toList());
    }

    /**
     * Saves a new doctor to the database
     */
    public int saveDoctor(Doctor doctor) {
        try {
            // Check if doctor already exists by email
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if (existingDoctor != null) {
                return -1; // Doctor already exists
            }

            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Updates the details of an existing doctor
     */
    public int updateDoctor(Doctor doctor) {
        try {
            // Check if doctor exists by ID
            Optional<Doctor> existingDoctorOpt = doctorRepository.findById(doctor.getId());
            if (existingDoctorOpt.isEmpty()) {
                return -1; // Doctor not found
            }

            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Retrieves a list of all doctors
     */
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor by ID
     */
    public int deleteDoctor(long id) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(id);
            if (doctorOpt.isEmpty()) {
                return -1; // Doctor not found
            }

            // Delete all associated appointments
            appointmentRepository.deleteAllByDoctorId(id);

            // Delete the doctor
            doctorRepository.deleteById(id);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Validates a doctor's login credentials
     */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            if (doctor == null) {
                response.put("message", "Doctor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Verify password (in real app, use password encoder)
            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate token
            String token = tokenService.generateTokenForDoctor(doctor.getId(), doctor.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("doctorId", doctor.getId().toString());
            response.put("name", doctor.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Finds doctors by their name
     */
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository.findByNameLike(name);
            response.put("doctors", doctors);
            response.put("count", doctors.size());
            response.put("searchTerm", name);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error searching for doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by name, specialty, and availability during AM/PM
     */
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by name and specialty
            List<Doctor> doctors = doctorRepository
                    .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);

            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());
            response.put("filters", Map.of(
                    "name", name,
                    "specialty", specialty,
                    "time", amOrPm
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by name and their availability during AM/PM
     */
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by name
            List<Doctor> doctors = doctorRepository.findByNameLike(name);

            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());
            response.put("filters", Map.of(
                    "name", name,
                    "time", amOrPm
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by name and specialty
     */
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository
                    .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);

            response.put("doctors", doctors);
            response.put("count", doctors.size());
            response.put("filters", Map.of(
                    "name", name,
                    "specialty", specialty
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by specialty and their availability during AM/PM
     */
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by specialty
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);

            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());
            response.put("filters", Map.of(
                    "specialty", specialty,
                    "time", amOrPm
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by specialty
     */
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);

            response.put("doctors", doctors);
            response.put("count", doctors.size());
            response.put("filters", Map.of(
                    "specialty", specialty
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Filters doctors by their availability during AM/PM
     */
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> allDoctors = doctorRepository.findAll();
            List<Doctor> filteredDoctors = filterDoctorByTime(allDoctors, amOrPm);

            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());
            response.put("filters", Map.of(
                    "time", amOrPm
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    /**
     * Private method to filter a list of doctors by their available times (AM/PM)
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(doctor -> {
                    List<String> availableTimes = doctor.getAvailableTimes();
                    if (availableTimes == null || availableTimes.isEmpty()) {
                        return false;
                    }

                    // Check if any available time slot matches AM/PM
                    return availableTimes.stream().anyMatch(timeSlot -> {
                        String startTime = timeSlot.split(" - ")[0];
                        LocalTime time = LocalTime.parse(startTime);

                        if ("AM".equalsIgnoreCase(amOrPm)) {
                            return time.isBefore(LocalTime.NOON);
                        } else if ("PM".equalsIgnoreCase(amOrPm)) {
                            return time.isAfter(LocalTime.NOON.minusMinutes(1));
                        }
                        return false;
                    });
                })
                .collect(Collectors.toList());
    }
}