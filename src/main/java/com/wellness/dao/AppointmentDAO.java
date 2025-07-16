package com.wellness.dao;

import com.wellness.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object interface for managing Appointment entities.
 * Extends the base CRUD operations with appointment-specific queries.
 */
public interface AppointmentDAO extends BaseDAO<Appointment, Integer> {
    
    /**
     * Finds all appointments for a specific student.
     *
     * @param studentId The ID of the student
     * @return A list of the student's appointments, ordered by start time
     */
    List<Appointment> findByStudentId(Integer studentId);
    
    /**
     * Finds all appointments for a specific counselor.
     *
     * @param counselorId The ID of the counselor
     * @return A list of the counselor's appointments, ordered by start time
     */
    List<Appointment> findByCounselorId(Integer counselorId);
    
    /**
     * Finds appointments within a specific date range.
     *
     * @param start The start of the date range (inclusive)
     * @param end The end of the date range (inclusive)
     * @return A list of appointments within the specified date range
     */
    List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds appointments by status.
     *
     * @param status The status to search for
     * @return A list of appointments with the specified status
     */
    List<Appointment> findByStatus(Appointment.Status status);
    
    /**
     * Finds conflicting appointments for a given time range and counselor.
     *
     * @param counselorId The ID of the counselor
     * @param startTime The start time to check
     * @param endTime The end time to check
     * @param excludeAppointmentId Optional appointment ID to exclude from the check (for updates)
     * @return A list of appointments that conflict with the specified time range
     */
    List<Appointment> findConflictingAppointments(
        Integer counselorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Integer excludeAppointmentId
    );
    
    /**
     * Checks if a time slot is available for a counselor.
     *
     * @param counselorId The ID of the counselor
     * @param startTime The start time to check
     * @param endTime The end time to check
     * @param excludeAppointmentId Optional appointment ID to exclude from the check (for updates)
     * @return true if the time slot is available, false otherwise
     */
    boolean isTimeSlotAvailable(
        Integer counselorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Integer excludeAppointmentId
    );
    
    /**
     * Counts the number of appointments for a student within a date range.
     *
     * @param studentId The ID of the student
     * @param start The start of the date range
     * @param end The end of the date range
     * @return The count of appointments
     */
    long countByStudentAndDateRange(Integer studentId, LocalDateTime start, LocalDateTime end);
}
