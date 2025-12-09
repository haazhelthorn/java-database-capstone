package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.dto.Login;
import com.project.back_end.repositories.AdminRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class Service {

    // 1. **@Service Annotation**
    // The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
    // and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

    @Autowired
    private final TokenService tokenService;

    @Autowired
    private final AdminRepository adminRepository;

    @Autowired
    private final DoctorRepository doctorRepository;

    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final DoctorService doctorService;

    @Autowired
    private final PatientService patientService;

    // 2. **Constructor Injection for Dependencies**
    // The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
    // and ensures that all required dependencies are provided at object creation time.
    @Autowired
    public Service(TokenService tokenService, AdminRepository adminRepository,
                   DoctorRepository doctorRepository, PatientRepository patientRepository,
                   DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // 3. **validateToken Method**
    // This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
    // If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
    // unauthorized access to protected resources.
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();

        if (!tokenService.validateToken(token, user)) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("message", "Token is valid");
        return ResponseEntity.ok(response);
    }

    // 4. **validateAdmin Method**
    // This method validates the login credentials for an admin user.
    // - It first searches the admin repository using the provided username.
    // - If an admin is found, it checks if the password matches.
    // - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
    // - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
    // - If no admin is found, it also returns a 401 Unauthorized.
    // - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
    // This method ensures that only valid admin users can access secured parts of the system.
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();

        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

            if (admin == null) {
                response.put("message", "Admin not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("role", "admin");
            response.put("adminId", admin.getId().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error during admin login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. **filterDoctor Method**
    // This method provides filtering functionality for doctors based on name, specialty, and available time slots.
    // - It supports various combinations of the three filters.
    // - If none of the filters are provided, it returns all available doctors.
    // This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors;

            if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                // Filter by name, specialty, and time
                doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
                doctors = filterDoctorsByTime(doctors, time);
            } else if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty()) {
                // Filter by name and specialty
                doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            } else if (name != null && !name.isEmpty() && time != null && !time.isEmpty()) {
                // Filter by name and time
                doctors = doctorRepository.findByNameLike(name);
                doctors = filterDoctorsByTime(doctors, time);
            } else if (specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                // Filter by specialty and time
                doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
                doctors = filterDoctorsByTime(doctors, time);
            } else if (name != null && !name.isEmpty()) {
                // Filter by name only
                doctors = doctorRepository.findByNameLike(name);
            } else if (specialty != null && !specialty.isEmpty()) {
                // Filter by specialty only
                doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            } else if (time != null && !time.isEmpty()) {
                // Filter by time only
                doctors = doctorRepository.findAll();
                doctors = filterDoctorsByTime(doctors, time);
            } else {
                // No filters, return all doctors
                doctors = doctorRepository.findAll();
            }

            response.put("doctors", doctors);
            response.put("count", doctors.size());
            response.put("filters", Map.of(
                    "name", name != null ? name : "",
                    "specialty", specialty != null ? specialty : "",
                    "time", time != null ? time : ""
            ));

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
        }

        return response;
    }

    // Helper method to filter doctors by time (AM/PM)
    private List<Doctor> filterDoctorsByTime(List<Doctor> doctors, String time) {
        List<Doctor> filteredDoctors = new ArrayList<>();
        boolean isAM = "AM".equalsIgnoreCase(time);
        boolean isPM = "PM".equalsIgnoreCase(time);

        for (Doctor doctor : doctors) {
            List<String> availableTimes = doctor.getAvailableTimes();
            if (availableTimes == null || availableTimes.isEmpty()) {
                continue;
            }

            boolean hasMatchingTime = false;
            for (String timeSlot : availableTimes) {
                String startTime = timeSlot.split(" - ")[0];
                int hour = Integer.parseInt(startTime.split(":")[0]);

                if (isAM && hour < 12) {
                    hasMatchingTime = true;
                    break;
                } else if (isPM && hour >= 12) {
                    hasMatchingTime = true;
                    break;
                }
            }

            if (hasMatchingTime) {
                filteredDoctors.add(doctor);
            }
        }

        return filteredDoctors;
    }

    // 6. **validateAppointment Method**
    // This method validates if the requested appointment time for a doctor is available.
    // - It first checks if the doctor exists in the repository.
    // - Then, it retrieves the list of available time slots for the doctor on the specified date.
    // - It compares the requested appointment time with the start times of these slots.
    // - If a match is found, it returns 1 (valid appointment time).
    // - If no matching time slot is found, it returns 0 (invalid).
    // - If the doctor doesn’t exist, it returns -1.
    // This logic prevents overlapping or invalid appointment bookings.
    public int validateAppointment(Appointment appointment) {
        try {
            Long doctorId = appointment.getDoctor().getId();
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);

            if (doctorOpt.isEmpty()) {
                return -1; // Doctor doesn't exist
            }

            // Get available time slots for the doctor on the appointment date
            LocalDateTime appointmentTime = appointment.getAppointmentTime();
            List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentTime.toLocalDate());

            // Check if the appointment time matches any available slot
            String appointmentTimeStr = appointmentTime.toLocalTime().toString().substring(0, 5); // HH:MM format
            for (String slot : availableSlots) {
                String slotStartTime = slot.split(" - ")[0];
                if (slotStartTime.equals(appointmentTimeStr)) {
                    return 1; // Valid appointment time
                }
            }

            return 0; // Time is unavailable

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 7. **validatePatient Method**
    // This method checks whether a patient with the same email or phone number already exists in the system.
    // - If a match is found, it returns false (indicating the patient is not valid for new registration).
    // - If no match is found, it returns true.
    // This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    public boolean validatePatient(Patient patient) {
        try {
            Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            return existingPatient == null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 8. **validatePatientLogin Method**
    // This method handles login validation for patient users.
    // - It looks up the patient by email.
    // - If found, it checks whether the provided password matches the stored one.
    // - On successful validation, it generates a JWT token and returns it with a 200 OK status.
    // - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
    // - If an exception occurs, it returns a 500 Internal Server Error.
    // This method ensures only legitimate patients can log in and access their data securely.
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());

            if (patient == null) {
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("role", "patient");
            response.put("patientId", patient.getId().toString());
            response.put("name", patient.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error during patient login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 9. **filterPatient Method**
    // This method filters a patient's appointment history based on condition and doctor name.
    // - It extracts the email from the JWT token to identify the patient.
    // - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
    // - If no filters are provided, it retrieves all appointments for the patient.
    // This flexible method supports patient-specific querying and enhances user experience on the client side.
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract email from token
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get patient by email
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Long patientId = patient.getId();

            // Apply filters based on provided parameters
            if (condition != null && !condition.isEmpty() && name != null && !name.isEmpty()) {
                // Filter by both condition and doctor name
                return patientService.filterByDoctorAndCondition(condition, name, patientId);
            } else if (condition != null && !condition.isEmpty()) {
                // Filter by condition only
                return patientService.filterByCondition(condition, patientId);
            } else if (name != null && !name.isEmpty()) {
                // Filter by doctor name only
                return patientService.filterByDoctor(name, patientId);
            } else {
                // No filters, return all appointments
                return patientService.getPatientAppointment(patientId, token);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error filtering patient appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}