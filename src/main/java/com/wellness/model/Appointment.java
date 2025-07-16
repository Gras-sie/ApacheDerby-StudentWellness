package com.wellness.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an appointment in the Wellness Management System.
 * Includes validation for appointment times and status management.
 */
public class Appointment extends Entity {
    public enum Status {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    private Integer counselorId;
    private Integer studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
    private Status status;

    public Appointment() {
        super();
        this.status = Status.SCHEDULED;
        System.out.println("New appointment created with status: " + status);
    }

    // Getters and Setters with validation

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        this.startTime = startTime;
        updateTimestamp();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (startTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        this.endTime = endTime;
        updateTimestamp();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        updateTimestamp();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        System.out.println("Appointment " + getId() + " status changed from " + this.status + " to " + status);
        this.status = status;
        updateTimestamp();
    }

    // Business Logic Methods

    /**
     * Checks if this appointment conflicts with another appointment.
     * @param other The other appointment to check for conflicts with
     * @return true if there is a time conflict, false otherwise
     */
    public boolean hasConflict(Appointment other) {
        if (other == null || !other.getCounselorId().equals(this.counselorId)) {
            return false;
        }
        return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
    }

    /**
     * Marks the appointment as completed.
     */
    public void markAsCompleted() {
        setStatus(Status.COMPLETED);
    }

    /**
     * Cancels the appointment with an optional reason.
     * @param reason The reason for cancellation
     */
    public void cancel(String reason) {
        if (status == Status.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }
        setStatus(Status.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            setNotes((notes != null ? notes + "\n" : "") + "Cancellation reason: " + reason);
        }
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(counselorId, that.counselorId) &&
               Objects.equals(studentId, that.studentId) &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), counselorId, studentId, startTime, endTime);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + getId() +
                ", counselorId=" + counselorId +
                ", studentId=" + studentId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", createdDate=" + getCreatedDate() +
                ", updatedDate=" + getUpdatedDate() +
                '}';
    }
}
