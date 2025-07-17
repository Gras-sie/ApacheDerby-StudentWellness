package com.wellness.service;

import com.wellness.dao.FeedbackDAO;
import com.wellness.exception.ServiceException;
import com.wellness.model.Feedback;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer for handling feedback-related business logic.
 * Provides validation, security checks, and coordinates between controllers and DAO.
 */
public class FeedbackService {
    
    private final FeedbackDAO feedbackDAO;
    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
        "(?i)(bad|awful|terrible|horrible|worst|hate|stupid|idiot|dumb|suck)"
    );
    
    public FeedbackService(FeedbackDAO feedbackDAO) {
        this.feedbackDAO = feedbackDAO;
    }
    
    /**
     * Creates new feedback after validation.
     * 
     * @param feedback The feedback to create
     * @return The created feedback with generated ID
     * @throws ServiceException if validation fails or database error occurs
     */
    public Feedback createFeedback(Feedback feedback) throws ServiceException {
        validateFeedback(feedback);
        checkForProfanity(feedback);
        checkForDuplicateFeedback(feedback);
        
        try {
            return feedbackDAO.save(feedback);
        } catch (Exception e) {
            throw new ServiceException("Failed to save feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Updates existing feedback after validation.
     * 
     * @param feedback The feedback to update
     * @return The updated feedback
     * @throws ServiceException if validation fails or database error occurs
     */
    public Feedback updateFeedback(Feedback feedback) throws ServiceException {
        if (feedback.getId() == null) {
            throw new ServiceException("Cannot update feedback without ID", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        validateFeedback(feedback);
        checkForProfanity(feedback);
        
        try {
            return feedbackDAO.save(feedback);
        } catch (Exception e) {
            throw new ServiceException("Failed to update feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Deletes feedback by ID.
     * 
     * @param id The ID of the feedback to delete
     * @throws ServiceException if feedback not found or database error occurs
     */
    public void deleteFeedback(Integer id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("Feedback ID cannot be null", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            Feedback feedback = feedbackDAO.findById(id).orElse(null);
            if (feedback == null) {
                throw new ServiceException("Feedback not found with id: " + id, ServiceException.ErrorCode.NOT_FOUND);
            }
            feedbackDAO.delete(feedback);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Retrieves feedback by ID.
     * 
     * @param id The ID of the feedback to retrieve
     * @return The feedback, or null if not found
     * @throws ServiceException if database error occurs
     */
    public Feedback getFeedbackById(Integer id) throws ServiceException {
        try {
            return feedbackDAO.findById(id).orElse(null);
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Retrieves all feedback, ordered by creation date (newest first).
     * 
     * @return List of all feedback
     * @throws ServiceException if database error occurs
     */
    public List<Feedback> getAllFeedback() throws ServiceException {
        try {
            return feedbackDAO.findAll();
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Searches feedback by student name or comment text.
     * 
     * @param searchTerm The term to search for
     * @return List of matching feedback
     * @throws ServiceException if search term is invalid or database error occurs
     */
    public List<Feedback> searchFeedback(String searchTerm) throws ServiceException {
        if (searchTerm == null || searchTerm.trim().length() < 2) {
            throw new ServiceException("Search term must be at least 2 characters long", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            return feedbackDAO.searchFeedback(searchTerm.trim());
        } catch (Exception e) {
            throw new ServiceException("Search failed: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Gets feedback for a specific counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return List of feedback for the counselor
     * @throws ServiceException if database error occurs
     */
    public List<Feedback> getFeedbackByCounselorId(Integer counselorId) throws ServiceException {
        if (counselorId == null) {
            throw new ServiceException("Counselor ID cannot be null", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            return feedbackDAO.findByCounselorId(counselorId);
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Calculates the average rating for a counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return The average rating, or 0 if no feedback exists
     * @throws ServiceException if database error occurs
     */
    public double getAverageRatingByCounselorId(Integer counselorId) throws ServiceException {
        if (counselorId == null) {
            throw new ServiceException("Counselor ID cannot be null", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            return feedbackDAO.getAverageRatingByCounselorId(counselorId);
        } catch (Exception e) {
            throw new ServiceException("Failed to calculate average rating: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Gets feedback count for a specific counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return The number of feedback entries for the counselor
     * @throws ServiceException if database error occurs
     */
    public int getFeedbackCountByCounselorId(Integer counselorId) throws ServiceException {
        if (counselorId == null) {
            throw new ServiceException("Counselor ID cannot be null", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            return feedbackDAO.getFeedbackCountByCounselorId(counselorId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get feedback by student ID: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    /**
     * Gets feedback within a specific rating range.
     * 
     * @param minRating Minimum rating (inclusive)
     * @param maxRating Maximum rating (inclusive)
     * @return List of feedback within the rating range
     * @throws ServiceException if rating range is invalid or database error occurs
     */
    public List<Feedback> getFeedbackByRatingRange(int minRating, int maxRating) throws ServiceException {
        if (minRating < 1 || maxRating > 5 || minRating > maxRating) {
            throw new ServiceException("Invalid rating range. Must be between 1 and 5, with min <= max", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        try {
            return feedbackDAO.findByRatingRange(minRating, maxRating);
        } catch (Exception e) {
            throw new ServiceException("Failed to get feedback by rating range: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
    
    // Private helper methods
    
    private void validateFeedback(Feedback feedback) throws ServiceException {
        if (feedback == null) {
            throw new ServiceException("Feedback cannot be null", ServiceException.ErrorCode.INVALID_INPUT);
        }
        
        if (feedback.getAppointmentId() == null) {
            throw new ServiceException("Appointment ID is required", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        
        if (feedback.getStudentId() == null) {
            throw new ServiceException("Student ID is required", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        
        if (feedback.getCounselorId() == null) {
            throw new ServiceException("Counselor ID is required", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new ServiceException("Rating must be between 1 and 5", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        
        if (feedback.getComments() == null || feedback.getComments().trim().isEmpty()) {
            throw new ServiceException("Comments are required", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        
        if (feedback.getComments().length() > 2000) {
            throw new ServiceException("Comments cannot exceed 2000 characters", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }
    
    private void checkForProfanity(Feedback feedback) throws ServiceException {
        if (feedback.getComments() != null && PROFANITY_PATTERN.matcher(feedback.getComments()).find()) {
            throw new ServiceException("Feedback contains inappropriate language", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }
    
    private void checkForDuplicateFeedback(Feedback feedback) throws ServiceException {
        try {
            List<Feedback> existingFeedback = feedbackDAO.findByAppointmentId(feedback.getAppointmentId());
            if (!existingFeedback.isEmpty() && (feedback.getId() == null || 
                existingFeedback.stream().noneMatch(f -> f.getId().equals(feedback.getId())))) {
                throw new ServiceException("Feedback already exists for this appointment", ServiceException.ErrorCode.DUPLICATE_ENTRY);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to check for duplicate feedback: " + e.getMessage(), e, ServiceException.ErrorCode.DATABASE_ERROR);
        }
    }
}
