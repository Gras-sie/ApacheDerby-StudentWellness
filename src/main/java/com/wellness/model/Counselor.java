package com.wellness.model;

import java.util.Objects;

/**
 * Represents a counselor in the Wellness Management System.
 * Includes personal information, contact details, and professional information.
 */
public class Counselor extends Entity {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String bio; // Alias for biography for backward compatibility
    private String biography; // Rich text biography
    private String photoPath; // Path to the counselor's photo
    private boolean isActive;

    public Counselor() {
        super();
        this.isActive = true;
        System.out.println("New counselor profile created");
    }

    // Getters and Setters with validation

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        this.firstName = firstName.trim();
        updateTimestamp();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        this.lastName = lastName.trim();
        updateTimestamp();
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase().trim();
        updateTimestamp();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        // Basic phone number validation (allows various formats)
        if (phoneNumber != null && !phoneNumber.matches("^[+]?[0-9\\s-()]{6,20}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        updateTimestamp();
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization != null ? specialization.trim() : null;
        updateTimestamp();
    }

    public String getBio() {
        return bio != null ? bio : "";
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio.trim() : null;
        this.biography = this.bio; // Keep in sync
        updateTimestamp();
    }
    
    public String getBiography() {
        return biography != null ? biography : getBio();
    }
    
    public void setBiography(String biography) {
        this.biography = biography != null ? biography.trim() : null;
        this.bio = this.biography; // Keep in sync
        updateTimestamp();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        updateTimestamp();
    }
    
    // For compatibility with controller
    public boolean getIsActive() {
        return isActive();
    }
    
    public void setIsActive(boolean active) {
        setActive(active);
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath != null ? photoPath.trim() : null;
        updateTimestamp();
    }

    // Business Logic Methods

    /**
     * Deactivates the counselor's account.
     */
    public void deactivate() {
        setActive(false);
    }

    /**
     * Reactivates the counselor's account.
     */
    public void reactivate() {
        setActive(true);
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Counselor counselor = (Counselor) o;
        return Objects.equals(email, counselor.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email);
    }

    @Override
    public String toString() {
        return "Counselor{" +
                "id=" + getId() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", specialization='" + specialization + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
    
    /**
     * Creates a deep copy of this counselor.
     * @return A new Counselor instance with the same property values.
     */
    @Override
    public Counselor clone() {
        try {
            return (Counselor) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new InternalError(e);
        }
    }
}
