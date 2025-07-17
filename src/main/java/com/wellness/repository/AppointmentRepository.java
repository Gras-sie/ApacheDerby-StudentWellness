package com.wellness.repository;

/**
 * Repository interface for managing appointment data access.
 */
public interface AppointmentRepository {
    
    /**
     * Checks if a counselor has any active appointments.
     * 
     * @param counselorId The ID of the counselor to check
     * @return true if the counselor has active appointments, false otherwise
     */
    boolean hasActiveAppointments(Long counselorId);
    
    // Add other appointment-related repository methods as needed
    // For example:
    // List<Appointment> findUpcomingByCounselor(Long counselorId);
    // boolean isTimeSlotAvailable(Long counselorId, LocalDateTime startTime, LocalDateTime endTime);
    // etc.
}
