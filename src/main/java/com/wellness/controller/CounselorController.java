package com.wellness.controller;

import com.wellness.model.Counselor;
import com.wellness.repository.AppointmentRepository;
import com.wellness.repository.InMemoryAppointmentRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for managing counselor-related operations.
 * Handles business logic for counselor management including CRUD operations.
 */
public class CounselorController {
    private static CounselorController instance;
    
    // In-memory storage (replace with database repository in production)
    private final Map<Integer, Counselor> counselors = new HashMap<>();
    private int nextId = 1;
    
    // Dependencies that would be injected in a real application
    private final AppointmentRepository appointmentRepository;
    
    // For testing purposes only - remove in production
    private CounselorController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Get the singleton instance of the controller with a default in-memory repository.
     * @return The singleton instance
     */
    public static synchronized CounselorController getInstance() {
        if (instance == null) {
            instance = new CounselorController(new InMemoryAppointmentRepository());
        }
        return instance;
    }
    
    /**
     * Get the singleton instance of the controller with a custom repository.
     * @param appointmentRepository The appointment repository to use
     * @return The singleton instance
     */
    public static synchronized CounselorController getInstance(AppointmentRepository appointmentRepository) {
        if (instance == null) {
            instance = new CounselorController(appointmentRepository);
        } else if (appointmentRepository != null && instance.appointmentRepository != appointmentRepository) {
            throw new IllegalStateException("CounselorController already initialized with a different repository");
        }
        return instance;
    }
    
    // CRUD Operations
    
    /**
     * Create a new counselor.
     * @param counselor The counselor to create
     * @return The created counselor with generated ID
     * @throws IllegalArgumentException if counselor data is invalid
     */
    public Counselor createCounselor(Counselor counselor) {
        validateCounselor(counselor);
        counselor.setId(nextId++);
        counselors.put(counselor.getId(), counselor);
        return counselor;
    }
    
    /**
     * Get a counselor by ID.
     * @param id The ID of the counselor to retrieve
     * @return Optional containing the counselor if found, empty otherwise
     */
    public Optional<Counselor> getCounselorById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(counselors.get(id));
    }
    
    /**
     * Get all counselors.
     * @return List of all counselors
     */
    public List<Counselor> getAllCounselors() {
        return new ArrayList<>(counselors.values().stream()
            .filter(Counselor::isActive)
            .collect(Collectors.toList()));
    }
    
    /**
     * Update an existing counselor.
     * @param counselor The counselor with updated information
     * @return The updated counselor
     * @throws IllegalArgumentException if counselor data is invalid or not found
     */
    public Counselor updateCounselor(Counselor counselor) {
        if (counselor == null || counselor.getId() == null) {
            throw new IllegalArgumentException("Invalid counselor data");
        }
        validateCounselor(counselor);
        if (!counselors.containsKey(counselor.getId())) {
            throw new IllegalArgumentException("Counselor not found");
        }
        counselors.put(counselor.getId(), counselor);
        return counselor;
    }
    
    /**
     * Delete a counselor by ID.
     * @param id The ID of the counselor to delete
     * @return true if deletion was successful, false otherwise
     * @throws IllegalStateException if counselor has active appointments
     */
    public boolean deleteCounselor(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid counselor ID");
        }
        if (appointmentRepository.hasActiveAppointments(id.longValue())) {
            throw new IllegalStateException("Cannot delete counselor with active appointments");
        }
        return counselors.remove(id) != null;
    }
    
    // Business Logic Methods
    
    /**
     * Search counselors by name or specialization.
     * @param query The search query
     * @return List of matching counselors
     */
    public List<Counselor> searchCounselors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCounselors();
        }
        
        String searchTerm = query.toLowerCase().trim();
        return counselors.values().stream()
            .filter(counselor -> counselor.isActive() && (
                (counselor.getFirstName() != null && counselor.getFirstName().toLowerCase().contains(searchTerm)) ||
                (counselor.getLastName() != null && counselor.getLastName().toLowerCase().contains(searchTerm)) ||
                (counselor.getEmail() != null && counselor.getEmail().toLowerCase().contains(searchTerm)) ||
                (counselor.getSpecialization() != null && counselor.getSpecialization().toLowerCase().contains(searchTerm)) ||
                (counselor.getBiography() != null && counselor.getBiography().toLowerCase().contains(searchTerm))
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a counselor has active appointments.
     * @param counselorId The ID of the counselor
     * @return true if the counselor has active appointments, false otherwise
     */
    public boolean hasActiveAppointments(Integer counselorId) {
        return appointmentRepository.hasActiveAppointments(counselorId.longValue());
    }
    
    // Validation
    
    /**
     * Validate counselor data.
     * @param counselor The counselor to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCounselor(Counselor counselor) {
        if (counselor == null) {
            throw new IllegalArgumentException("Counselor cannot be null");
        }
        
        if (counselor.getFirstName() == null || counselor.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        
        if (counselor.getLastName() == null || counselor.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        
        if (counselor.getEmail() == null || !counselor.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        // Additional validation rules can be added here
    }
}
