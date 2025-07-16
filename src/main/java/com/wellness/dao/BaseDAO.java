package com.wellness.dao;

import com.wellness.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Base interface for all DAO (Data Access Object) implementations.
 * Provides common CRUD operations for all entities.
 *
 * @param <T> The entity type this DAO manages
 * @param <ID> The type of the entity's identifier
 */
public interface BaseDAO<T extends Entity, ID> {
    
    /**
     * Saves the given entity to the database.
     * If the entity already exists (has an ID), it will be updated.
     * Otherwise, a new record will be inserted.
     *
     * @param entity The entity to save
     * @return The saved entity (with generated ID if new)
     */
    T save(T entity);
    
    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to find
     * @return An Optional containing the found entity, or empty if not found
     */
    Optional<T> findById(ID id);
    
    /**
     * Retrieves all entities of type T.
     *
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Checks if an entity with the given ID exists.
     *
     * @param id The ID to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);
    
    /**
     * Counts all entities of type T.
     *
     * @return The total count of entities
     */
    long count();
    
    /**
     * Deletes the entity with the given ID.
     *
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false if no entity was found with the given ID
     */
    boolean deleteById(ID id);
    
    /**
     * Deletes the given entity.
     *
     * @param entity The entity to delete
     * @return true if the entity was deleted, false otherwise
     */
    boolean delete(T entity);
    
    /**
     * Deletes all entities.
     *
     * @return The number of entities deleted
     */
    long deleteAll();
}
