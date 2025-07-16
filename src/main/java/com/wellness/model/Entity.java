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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    protected void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    protected void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate != null ? updatedDate : LocalDateTime.now();
    }

    /**
     * Updates the updatedDate timestamp to current time.
     */
    protected void updateTimestamp() {
        this.updatedDate = LocalDateTime.now();
        System.out.println("Entity " + this.getClass().getSimpleName() + " updated at: " + updatedDate);
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
