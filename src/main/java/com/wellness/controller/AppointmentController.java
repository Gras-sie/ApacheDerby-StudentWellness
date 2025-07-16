package com.wellness.controller;

import com.wellness.dao.AppointmentDAO;
import com.wellness.model.Appointment;
import com.wellness.model.Appointment.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller class for managing appointment-related operations.
 * Handles business logic and coordinates between the view and data access layers.
 */
public class AppointmentController {
    private final AppointmentDAO appointmentDAO;
    
    public AppointmentController(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
    }
    
    /**
     * Retrieves all appointments from the database.
     * @return List of all appointments
     */
    public List<Appointment> getAllAppointments() {
        try {
            return appointmentDAO.findAll();
        } catch (Exception e) {
            System.err.println("Error retrieving appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves appointments with optional filtering.
     * @param status Filter by status (optional)
     * @param counselorId Filter by counselor ID (optional)
     * @param startDate Start date range (inclusive)
     * @param endDate End date range (inclusive)
     * @return Filtered list of appointments
     */
    public List<Appointment> getFilteredAppointments(Status status, Integer counselorId, 
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Appointment> appointments = appointmentDAO.findAll();
            
            // Apply filters
            return appointments.stream()
                .filter(appt -> status == null || appt.getStatus() == status)
                .filter(appt -> counselorId == null || counselorId.equals(appt.getCounselorId()))
                .filter(appt -> startDate == null || !appt.getStartTime().isBefore(startDate))
                .filter(appt -> endDate == null || !appt.getStartTime().isAfter(endDate))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error filtering appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Saves or updates an appointment.
     * @param appointment The appointment to save
     * @return true if successful, false otherwise
     */
    public boolean saveAppointment(Appointment appointment) {
        try {
            // Check for scheduling conflicts
            if (hasSchedulingConflict(appointment)) {
                throw new IllegalStateException("Scheduling conflict detected");
            }
            
            Appointment savedAppointment = appointmentDAO.save(appointment);
            return savedAppointment != null;
        } catch (Exception e) {
            System.err.println("Error saving appointment: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Cancels an appointment.
     * @param appointmentId The ID of the appointment to cancel
     * @param reason Optional reason for cancellation
     * @return true if successful, false otherwise
     */
    public boolean cancelAppointment(int appointmentId, String reason) {
        try {
            Optional<Appointment> appointmentOpt = appointmentDAO.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                throw new IllegalArgumentException("Appointment not found");
            }
            
            Appointment appointment = appointmentOpt.get();
            appointment.cancel(reason);
            Appointment updatedAppointment = appointmentDAO.save(appointment);
            return updatedAppointment != null;
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Checks if an appointment has scheduling conflicts with existing appointments.
     * @param appointment The appointment to check
     * @return true if there is a conflict, false otherwise
     */
    public boolean hasSchedulingConflict(Appointment appointment) {
        if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
            return false;
        }
        
        try {
            // Find all appointments for the counselor on the same day
            LocalDateTime startOfDay = appointment.getStartTime().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // First get all appointments for the counselor
            List<Appointment> counselorAppointments = appointmentDAO.findByCounselorId(appointment.getCounselorId());
            
            // Then filter for the date range we care about
            List<Appointment> existingAppointments = counselorAppointments.stream()
                .filter(a -> !a.getStartTime().isBefore(startOfDay) && !a.getStartTime().isAfter(endOfDay))
                .collect(Collectors.toList());
            
            // Check for conflicts with other appointments (excluding the current one if it exists)
            return existingAppointments.stream()
                .filter(a -> !a.getId().equals(appointment.getId())) // Exclude self
                .anyMatch(a -> a.hasConflict(appointment));
                
        } catch (Exception e) {
            System.err.println("Error checking for scheduling conflicts: " + e.getMessage());
            return true; // Assume conflict on error to be safe
        }
    }
    
    /**
     * Retrieves available time slots for a counselor on a specific date.
     * @param counselorId The ID of the counselor
     * @param date The date to check
     * @return List of available time slots
     */
    public List<LocalDateTime> getAvailableTimeSlots(int counselorId, LocalDateTime date) {
        try {
            // Get all appointments for the counselor on the given date
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // First get all appointments for the counselor
            List<Appointment> counselorAppointments = appointmentDAO.findByCounselorId(counselorId);
            
            // Then filter for the date range we care about
            List<Appointment> existingAppointments = counselorAppointments.stream()
                .filter(a -> !a.getStartTime().isBefore(startOfDay) && !a.getStartTime().isAfter(endOfDay))
                .collect(Collectors.toList());
                
            // Generate all possible 30-minute slots for the day (9:00-17:00)
            List<LocalDateTime> allSlots = new ArrayList<>();
            LocalDateTime slot = startOfDay.plusHours(9); // Start at 9:00
            while (slot.isBefore(startOfDay.plusHours(17))) { // Until 17:00
                allSlots.add(slot);
                slot = slot.plusMinutes(30);
            }
            
            // Remove booked slots
            for (Appointment appt : existingAppointments) {
                allSlots.removeIf(slotTime -> 
                    !slotTime.isBefore(appt.getStartTime()) && 
                    slotTime.isBefore(appt.getEndTime()));
            }
            
            return allSlots;
            
        } catch (Exception e) {
            System.err.println("Error getting available time slots: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Exports appointments to CSV format.
     * @param appointments The appointments to export
     * @return CSV formatted string
     */
    public String exportToCsv(List<Appointment> appointments) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("ID,Student ID,Counselor ID,Start Time,End Time,Status,Notes\n");
        
        // Data rows
        for (Appointment appt : appointments) {
            csv.append(String.format("\"%d\",", appt.getId()));
            csv.append(String.format("\"%d\",", appt.getStudentId()));
            csv.append(String.format("\"%d\",", appt.getCounselorId()));
            csv.append(String.format("\"%s\",", appt.getStartTime()));
            csv.append(String.format("\"%s\",", appt.getEndTime()));
            csv.append(String.format("\"%s\",", appt.getStatus()));
            
            // Escape quotes in notes and add to CSV
            String notes = appt.getNotes() != null ? 
                appt.getNotes().replace("\"", "\"\"") : "";
            csv.append(String.format("\"%s\"", notes));
            
            csv.append("\n");
        }
        
        return csv.toString();
    }
}
