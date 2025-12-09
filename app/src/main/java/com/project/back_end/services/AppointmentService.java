package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repositories.AppointmentRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private TokenService tokenService;
    
    /**
     * Books a new appointment
     * @param appointment The appointment object to book
     * @return 1 if successful, 0 if there's an error
     */
    public int bookAppointment(Appointment appointment) {
        try {
            // Validate that doctor exists
            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());
            if (doctor.isEmpty()) {
                return 0; // Doctor not found
            }
            
            // Validate that patient exists
            Optional<Patient> patient = patientRepository.findById(appointment.getPatient().getId());
            if (patient.isEmpty()) {
                return 0; // Patient not found
            }
            
            // Save the appointment
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Updates an existing appointment
     * @param appointment The appointment object with updated information
     * @return ResponseEntity with success or failure message
     */
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if appointment exists
            Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
            if (existingAppointment.isEmpty()) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Validate the appointment
            if (!validateAppointment(appointment)) {
                response.put("message", "Invalid appointment data");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if doctor exists
            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());
            if (doctor.isEmpty()) {
                response.put("message", "Doctor not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Save the updated appointment
            appointmentRepository.save(appointment);
            
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error updating appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cancels an existing appointment
     * @param id The ID of the appointment to cancel
     * @param token The authorization token
     * @return ResponseEntity with success or failure message
     */
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find the appointment
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            if (appointmentOpt.isEmpty()) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Extract patient ID from token
            Long patientIdFromToken = tokenService.extractPatientId(token);
            
            // Verify that the patient attempting to cancel is the one who booked it
            if (patientIdFromToken == null || 
                !appointment.getPatient().getId().equals(patientIdFromToken)) {
                response.put("message", "Unauthorized to cancel this appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Delete the appointment
            appointmentRepository.delete(appointment);
            
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error cancelling appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Retrieves a list of appointments for a specific doctor on a specific date
     * @param pname Patient name to filter by (can be null or empty)
     * @param date The date for appointments
     * @param token The authorization token
     * @return Map containing the list of appointments
     */
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract doctor ID from token
            Long doctorId = tokenService.extractDoctorId(token);
            if (doctorId == null) {
                response.put("error", "Invalid token or not a doctor");
                return response;
            }
            
            // Calculate start and end of the day
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            List<Appointment> appointments;
            
            if (pname != null && !pname.trim().isEmpty()) {
                // Filter by patient name
                appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                        doctorId, pname.trim(), startOfDay, endOfDay);
            } else {
                // Get all appointments for the day
                appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
            }
            
            response.put("appointments", appointments);
            response.put("count", appointments.size());
            response.put("date", date.toString());
            response.put("doctorId", doctorId);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error retrieving appointments: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Validates appointment data
     * @param appointment The appointment to validate
     * @return true if valid, false otherwise
     */
    private boolean validateAppointment(Appointment appointment) {
        // Check if appointment time is in the future
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Check if doctor and patient are not null
        if (appointment.getDoctor() == null || appointment.getPatient() == null) {
            return false;
        }
        
        // Check if status is valid (0 = Scheduled, 1 = Completed)
        int status = appointment.getStatus();
        if (status != 0 && status != 1) {
            return false;
        }
        
        return true;
    }
}