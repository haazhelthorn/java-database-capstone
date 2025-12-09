package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private Service service;

    /**
     * Get appointments for a specific doctor on a specific date
     * @param date The date for appointments (yyyy-MM-dd)
     * @param patientName Optional patient name to filter by
     * @param token Authentication token
     * @return List of appointments
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        // Validate token for doctor
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode().isError()) {
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("message")));
        }

        LocalDate localDate = LocalDate.parse(date);
        Map<String, Object> appointments = appointmentService.getAppointment(patientName, localDate, token);
        
        return ResponseEntity.ok(appointments);
    }

    /**
     * Book a new appointment
     * @param appointment Appointment details
     * @param token Patient authentication token
     * @return Success or error response
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // Validate token for patient
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode().isError()) {
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("message")));
        }

        // Validate appointment time
        int validationResult = service.validateAppointment(appointment);
        if (validationResult == -1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Doctor does not exist"));
        } else if (validationResult == 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Appointment time is not available"));
        }

        // Book the appointment
        int bookingResult = appointmentService.bookAppointment(appointment);
        if (bookingResult == 1) {
            return ResponseEntity.status(201)
                    .body(Map.of("message", "Appointment booked successfully"));
        } else {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to book appointment"));
        }
    }

    /**
     * Update an existing appointment
     * @param appointment Updated appointment details
     * @param token Patient authentication token
     * @return Success or error response
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // Validate token for patient
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode().isError()) {
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("message")));
        }

        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancel an appointment
     * @param id Appointment ID
     * @param token Patient authentication token
     * @return Success or error response
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        // Validate token for patient
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode().isError()) {
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("message")));
        }

        return appointmentService.cancelAppointment(id, token);
    }
}