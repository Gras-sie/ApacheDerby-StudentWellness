package com.wellness.dao;

import com.wellness.model.Feedback;

import java.util.List;

/**
 * Data Access Object interface for managing Feedback entities.
 * Extends the base CRUD operations with feedback-specific queries.
 */
public interface FeedbackDAO extends BaseDAO<Feedback, Integer> {
    
    /**
     * Finds all feedback for a specific student.
     *
     * @param studentId The ID of the student
     * @return A list of feedback entries for the student, ordered by creation date
     */
    List<Feedback> findByStudentId(Integer studentId);
    
    /**
     * Finds all feedback for a specific counselor.
     *
     * @param counselorId The ID of the counselor
     * @return A list of feedback entries for the counselor, ordered by creation date
     */
    List<Feedback> findByCounselorId(Integer counselorId);
    
    /**
     * Finds all feedback for a specific appointment.
     *
     * @param appointmentId The ID of the appointment
     * @return The feedback for the appointment, if it exists
     */
    List<Feedback> findByAppointmentId(Integer appointmentId);
    
    /**
     * Finds feedback within a specific rating range.
     *
     * @param minRating The minimum rating (inclusive)
     * @param maxRating The maximum rating (inclusive)
     * @return A list of feedback entries within the rating range
     */
    List<Feedback> findByRatingRange(int minRating, int maxRating);
    
    /**
     * Calculates the average rating for a counselor.
     *
     * @param counselorId The ID of the counselor
     * @return The average rating, or 0 if no feedback exists
     */
    double getAverageRatingForCounselor(Integer counselorId);
    
    /**
     * Counts the number of feedback entries for a counselor.
     *
     * @param counselorId The ID of the counselor
     * @return The count of feedback entries
     */
    long countByCounselorId(Integer counselorId);
    
    /**
     * Counts the number of feedback entries for a student.
     *
     * @param studentId The ID of the student
     * @return The count of feedback entries
     */
    long countByStudentId(Integer studentId);
    
    /**
     * Checks if feedback exists for a specific appointment.
     *
     * @param appointmentId The ID of the appointment
     * @return true if feedback exists for the appointment, false otherwise
     */
    boolean existsByAppointmentId(Integer appointmentId);
    
    /**
     * Gets the distribution of ratings for a counselor.
     *
     * @param counselorId The ID of the counselor
     * @return An array where index i-1 contains the count of i-star ratings
     */
    int[] getRatingDistribution(Integer counselorId);
    
    /**
     * Finds all anonymous feedback.
     *
     * @return A list of all anonymous feedback entries
     */
    List<Feedback> findAnonymousFeedback();
    
    /**
     * Finds all non-anonymous feedback.
     *
     * @return A list of all non-anonymous feedback entries
     */
    List<Feedback> findNonAnonymousFeedback();
    
    /**
     * Searches for feedback containing the given search term in the comments or student names.
     *
     * @param searchTerm The term to search for
     * @return A list of matching feedback entries
     */
    List<Feedback> searchFeedback(String searchTerm);
    
    /**
     * Gets the average rating for a specific counselor.
     *
     * @param counselorId The ID of the counselor
     * @return The average rating, or 0 if no feedback exists
     */
    double getAverageRatingByCounselorId(Integer counselorId);
    
    /**
     * Gets the count of feedback for a specific counselor.
     *
     * @param counselorId The ID of the counselor
     * @return The count of feedback entries
     */
    int getFeedbackCountByCounselorId(Integer counselorId);
    
    /**
     * Gets all feedback for a specific student.
     *
     * @param studentId The ID of the student
     * @return A list of feedback entries for the student
     */
    List<Feedback> getFeedbackByStudentId(Integer studentId);
    
    /**
     * Gets all feedback for a specific appointment.
     *
     * @param appointmentId The ID of the appointment
     * @return A list of feedback entries for the appointment
     */
    List<Feedback> getFeedbackByAppointmentId(Integer appointmentId);
    
    /**
     * Gets all feedback within a specific rating range.
     *
     * @param minRating The minimum rating (inclusive)
     * @param maxRating The maximum rating (inclusive)
     * @return A list of feedback entries within the rating range
     */
    List<Feedback> getFeedbackByRatingRange(int minRating, int maxRating);
}
