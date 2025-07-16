package com.wellness.dao;

import com.wellness.model.Counselor;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for managing Counselor entities.
 * Extends the base CRUD operations with counselor-specific queries.
 */
public interface CounselorDAO extends BaseDAO<Counselor, Integer> {
    
    /**
     * Finds a counselor by their email address.
     *
     * @param email The email address to search for
     * @return An Optional containing the counselor if found, or empty if not found
     */
    Optional<Counselor> findByEmail(String email);
    
    /**
     * Finds counselors by their specialization.
     *
     * @param specialization The specialization to search for
     * @return A list of counselors with the specified specialization
     */
    List<Counselor> findBySpecialization(String specialization);
    
    /**
     * Finds all active counselors.
     *
     * @return A list of all active counselors
     */
    List<Counselor> findActiveCounselors();
    
    /**
     * Finds all inactive counselors.
     *
     * @return A list of all inactive counselors
     */
    List<Counselor> findInactiveCounselors();
    
    /**
     * Searches for counselors by name (partial match on first or last name).
     *
     * @param name The name or part of the name to search for
     * @return A list of matching counselors
     */
    List<Counselor> searchByName(String name);
    
    /**
     * Checks if a counselor with the given email exists.
     *
     * @param email The email to check
     * @return true if a counselor with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Counts the number of active counselors.
     *
     * @return The count of active counselors
     */
    long countActive();
    
    /**
     * Counts the number of inactive counselors.
     *
     * @return The count of inactive counselors
     */
    long countInactive();
    
    /**
     * Deactivates a counselor by ID.
     *
     * @param id The ID of the counselor to deactivate
     * @return true if the counselor was deactivated, false if not found
     */
    boolean deactivate(Integer id);
    
    /**
     * Reactivates a counselor by ID.
     *
     * @param id The ID of the counselor to reactivate
     * @return true if the counselor was reactivated, false if not found
     */
    boolean reactivate(Integer id);
}
