package com.wellness.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base class for all entities in the Wellness Management System.
 * Provides common properties and methods for all domain objects.
 */
public abstract class Entity {
    protected Integer id;
    protected LocalDateTime createdDate;
    protected LocalDateTime updatedDate;

    /**
     * Default constructor initializes timestamps.
     */
    protected Entity() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        System.out.println("Entity created at: " + createdDate);
    }

    // Getters and Setters with validation

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        this.id = id;
        updateTimestamp();
    }

    /**
     * @return The creation timestamp of this entity
     */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    /**
     * Alias for getCreatedDate() for compatibility with some views
     * @return The creation timestamp of this entity
     */
    public LocalDateTime getCreatedAt() {
        return getCreatedDate();
    }

    protected void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
    }

    /**
     * @return The last update timestamp of this entity
     */
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    /**
     * Alias for getUpdatedDate() for compatibility with some views
     * @return The last update timestamp of this entity
     */
    public LocalDateTime getUpdatedAt() {
        return getUpdatedDate();
    }

    protected void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate != null ? updatedDate : LocalDateTime.now();
    }

    /**
     * Updates the updatedDate timestamp to current time.
     */
    /**
     * Updates the timestamp to the current time.
     * This is called automatically when any setter is called.
     */
    protected void updateTimestamp() {
        this.updatedDate = LocalDateTime.now();
        System.out.println("Entity " + this.getClass().getSimpleName() + " updated at: " + updatedDate);
    }
    
    /**
     * Sets the ID of this entity with proper type conversion.
     * @param id The ID to set (can be Number or String)
     */
    public void setId(Object id) {
        if (id == null) {
            this.id = null;
        } else if (id instanceof Number) {
            this.id = ((Number) id).intValue();
        } else if (id instanceof String) {
            try {
                this.id = Integer.parseInt(id.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID must be a number", e);
            }
        } else {
            throw new IllegalArgumentException("ID must be a number");
        }
        updateTimestamp();
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
