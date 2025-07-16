package com.wellness.service;

import com.wellness.exception.ServiceException;
import com.wellness.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing appointments.
 * Handles business logic for appointment operations.
 */
public interface AppointmentService extends BaseService<Appointment, Integer> {
    
    /**
     * Finds all appointments for a specific student.
     *
     * @param studentId The ID of the student
     * @return A list of the student's appointments, ordered by start time
     * @throws ServiceException If an error occurs
     */
    List<Appointment> findByStudentId(Integer studentId) throws ServiceException;
    
    /**
     * Finds all appointments for a specific counselor.
     *
     * @param counselorId The ID of the counselor
     * @return A list of the counselor's appointments, ordered by start time
     * @throws ServiceException If an error occurs
     */
    List<Appointment> findByCounselorId(Integer counselorId) throws ServiceException;
    
    /**
     * Finds appointments within a specific date range.
     *
     * @param start The start of the date range (inclusive)
     * @param end The end of the date range (inclusive)
     * @return A list of appointments within the specified date range
     * @throws ServiceException If an error occurs
     */
    List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException;
    
    /**
     * Finds appointments by status.
     *
     * @param status The status to search for
     * @return A list of appointments with the specified status
     * @throws ServiceException If an error occurs
     */
    List<Appointment> findByStatus(Appointment.Status status) throws ServiceException;
    
    /**
     * Checks if a time slot is available for a counselor.
     *
     * @param counselorId The ID of the counselor
     * @param startTime The start time to check
     * @param endTime The end time to check
     * @param excludeAppointmentId Optional appointment ID to exclude from the check (for updates)
     * @return true if the time slot is available, false otherwise
     * @throws ServiceException If an error occurs
     */
    boolean isTimeSlotAvailable(
        Integer counselorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Integer excludeAppointmentId
    ) throws ServiceException;
    
    /**
     * Cancels an appointment.
     *
     * @param appointmentId The ID of the appointment to cancel
     * @param reason The reason for cancellation
     * @return The cancelled appointment
     * @throws ServiceException If the appointment doesn't exist or is already completed/cancelled
     */
    Appointment cancelAppointment(Integer appointmentId, String reason) throws ServiceException;
    
    /**
     * Completes an appointment.
     *
     * @param appointmentId The ID of the appointment to complete
     * @return The completed appointment
     * @throws ServiceException If the appointment doesn't exist or is already completed/cancelled
     */
    Appointment completeAppointment(Integer appointmentId) throws ServiceException;
    
    /**
     * Counts the number of appointments for a student within a date range.
     *
     * @param studentId The ID of the student
     * @param start The start of the date range
     * @param end The end of the date range
     * @return The count of appointments
     * @throws ServiceException If an error occurs
     */
    long countByStudentAndDateRange(Integer studentId, LocalDateTime start, LocalDateTime end) throws ServiceException;
}
