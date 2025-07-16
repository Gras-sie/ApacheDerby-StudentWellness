package com.wellness.model;

import java.util.Objects;

/**
 * Represents feedback provided by a student for a counseling session.
 * Includes rating, comments, and timestamps for tracking feedback history.
 */
public class Feedback extends Entity {
    private Integer appointmentId;
    private Integer studentId;
    private Integer counselorId;
    private int rating; // 1-5 scale
    private String comments;
    private boolean isAnonymous;

    // Constants for validation
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    public Feedback() {
        super();
        System.out.println("New feedback entry created");
    }

    // Getters and Setters with validation

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("Appointment ID must be a positive number");
        }
        this.appointmentId = appointmentId;
        updateTimestamp();
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive number");
        }
        this.studentId = studentId;
        updateTimestamp();
    }

    public Integer getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Integer counselorId) {
        if (counselorId == null || counselorId <= 0) {
            throw new IllegalArgumentException("Counselor ID must be a positive number");
        }
        this.counselorId = counselorId;
        updateTimestamp();
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                String.format("Rating must be between %d and %d", MIN_RATING, MAX_RATING)
            );
        }
        this.rating = rating;
        updateTimestamp();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments != null ? comments.trim() : null;
        updateTimestamp();
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        if (this.isAnonymous != anonymous) {
            System.out.println("Feedback " + getId() + " anonymous status changed to: " + anonymous);
            this.isAnonymous = anonymous;
            updateTimestamp();
        }
    }

    // Business Logic Methods

    /**
     * Gets a string representation of the rating using stars.
     * @return A string of star characters representing the rating
     */
    public String getStarRating() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < MAX_RATING; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    /**
     * Validates that all required fields are set before saving.
     * @throws IllegalStateException if required fields are not set
     */
    public void validate() {
        if (appointmentId == null || studentId == null || counselorId == null) {
            throw new IllegalStateException("Required fields are missing");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalStateException("Invalid rating value");
        }
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Feedback feedback = (Feedback) o;
        return rating == feedback.rating &&
               isAnonymous == feedback.isAnonymous &&
               Objects.equals(appointmentId, feedback.appointmentId) &&
               Objects.equals(studentId, feedback.studentId) &&
               Objects.equals(counselorId, feedback.counselorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), appointmentId, studentId, counselorId, rating, isAnonymous);
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + getId() +
                ", appointmentId=" + appointmentId +
                ", studentId=" + (isAnonymous ? "[HIDDEN]" : studentId) +
                ", counselorId=" + counselorId +
                ", rating=" + getStarRating() +
                ", isAnonymous=" + isAnonymous +
                ", createdDate=" + getCreatedDate() +
                ", updatedDate=" + getUpdatedDate() +
                '}';
    }
}
