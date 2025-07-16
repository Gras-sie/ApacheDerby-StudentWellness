package com.wellness.service;

import com.wellness.exception.ServiceException;
import com.wellness.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Base service interface with common CRUD operations.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's identifier
 */
public interface BaseService<T extends Entity, ID> {
    
    /**
     * Saves the given entity.
     *
     * @param entity The entity to save
     * @return The saved entity
     * @throws ServiceException If an error occurs during save
     */
    T save(T entity) throws ServiceException;
    
    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to find
     * @return An Optional containing the found entity, or empty if not found
     * @throws ServiceException If an error occurs during find
     */
    Optional<T> findById(ID id) throws ServiceException;
    
    /**
     * Retrieves all entities.
     *
     * @return A list of all entities
     * @throws ServiceException If an error occurs during retrieval
     */
    List<T> findAll() throws ServiceException;
    
    /**
     * Deletes the entity with the given ID.
     *
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false otherwise
     * @throws ServiceException If an error occurs during deletion
     */
    boolean delete(ID id) throws ServiceException;
    
    /**
     * Counts all entities.
     *
     * @return The total count of entities
     * @throws ServiceException If an error occurs during count
     */
    long count() throws ServiceException;
}
