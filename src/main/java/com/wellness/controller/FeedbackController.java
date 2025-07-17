package com.wellness.controller;

import com.wellness.model.Feedback;
import com.wellness.service.FeedbackService;
import javax.swing.*;
import java.util.List;

/**
 * Controller for handling feedback-related UI interactions.
 * Acts as a bridge between the UI components and the FeedbackService.
 */
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }
    
    /**
     * Submits new feedback.
     * 
     * @param feedback The feedback to submit
     * @return true if successful, false otherwise
     */
    public boolean submitFeedback(Feedback feedback) {
        try {
            feedbackService.createFeedback(feedback);
            showMessage("Feedback submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            showError("Failed to submit feedback: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates existing feedback.
     * 
     * @param feedback The feedback to update
     * @return true if successful, false otherwise
     */
    public boolean updateFeedback(Feedback feedback) {
        try {
            feedbackService.updateFeedback(feedback);
            showMessage("Feedback updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            showError("Failed to update feedback: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes feedback by ID.
     * 
     * @param feedbackId The ID of the feedback to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteFeedback(int feedbackId) {
        try {
            feedbackService.deleteFeedback(feedbackId);
            showMessage("Feedback deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            showError("Failed to delete feedback: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all feedback entries.
     * 
     * @return List of all feedback entries, or null if an error occurs
     */
    public List<Feedback> getAllFeedback() {
        try {
            return feedbackService.getAllFeedback();
        } catch (Exception e) {
            showError("Failed to load feedback: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets feedback for a specific counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return List of feedback for the counselor, or null if an error occurs
     */
    public List<Feedback> getFeedbackForCounselor(int counselorId) {
        try {
            return feedbackService.getFeedbackByCounselorId(counselorId);
        } catch (Exception e) {
            showError("Failed to load feedback for counselor: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets feedback within a specific rating range.
     * 
     * @param minRating Minimum rating (inclusive)
     * @param maxRating Maximum rating (inclusive)
     * @return List of feedback within the rating range, or null if an error occurs
     */
    public List<Feedback> getFeedbackByRating(int minRating, int maxRating) {
        try {
            return feedbackService.getFeedbackByRatingRange(minRating, maxRating);
        } catch (Exception e) {
            showError("Failed to load feedback by rating: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Searches feedback by student name or comment text.
     * 
     * @param searchTerm The term to search for
     * @return List of matching feedback, or null if an error occurs
     */
    public List<Feedback> searchFeedback(String searchTerm) {
        try {
            return feedbackService.searchFeedback(searchTerm);
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculates the average rating for a counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return The average rating, or -1 if an error occurs
     */
    public double getAverageRating(int counselorId) {
        try {
            return feedbackService.getAverageRatingByCounselorId(counselorId);
        } catch (Exception e) {
            showError("Failed to calculate average rating: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Gets the feedback count for a counselor.
     * 
     * @param counselorId The ID of the counselor
     * @return The feedback count, or -1 if an error occurs
     */
    public int getFeedbackCount(int counselorId) {
        try {
            return feedbackService.getFeedbackCountByCounselorId(counselorId);
        } catch (Exception e) {
            showError("Failed to get feedback count: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Shows a confirmation dialog for deleting feedback.
     * 
     * @return true if the user confirms, false otherwise
     */
    public boolean confirmDelete() {
        int result = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete this feedback? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    // Helper methods for displaying messages
    
    private void showError(String message) {
        showMessage(message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            messageType
        );
    }
}
