package com.wellness.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of AppointmentRepository for testing purposes.
 * In a real application, this would be replaced with a database implementation.
 */
public class InMemoryAppointmentRepository implements AppointmentRepository {
    
    private final Map<Long, Boolean> counselorAppointments;
    
    public InMemoryAppointmentRepository() {
        this.counselorAppointments = new HashMap<>();
    }
    
    @Override
    public boolean hasActiveAppointments(Long counselorId) {
        if (counselorId == null) {
            return false;
        }
        // In a real implementation, this would check the database
        // For testing, we'll just check our in-memory map
        return counselorAppointments.getOrDefault(counselorId, false);
    }
    
    // For testing purposes
    public void setHasActiveAppointments(Long counselorId, boolean hasAppointments) {
        if (counselorId != null) {
            counselorAppointments.put(counselorId, hasAppointments);
        }
    }
}
