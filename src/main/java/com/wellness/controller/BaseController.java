package com.wellness.controller;

import com.wellness.exception.ControllerException;
import com.wellness.exception.ServiceException;
import com.wellness.model.Entity;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base controller class providing common functionality for all controllers.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's identifier
 */
public abstract class BaseController<T extends Entity, ID> {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    protected final String entityName;
    
    /**
     * Constructs a new BaseController with the given entity name.
     *
     * @param entityName The name of the entity (for logging and error messages)
     */
    protected BaseController(String entityName) {
        this.entityName = entityName;
    }
    
    /**
     * Handles a service operation and logs any exceptions.
     *
     * @param <R> The return type
     * @param operation The operation to perform
     * @param operationName The name of the operation (for logging)
     * @return The result of the operation
     * @throws ControllerException If the operation fails
     */
    protected <R> R handleServiceCall(
            ServiceOperation<R> operation, 
            String operationName) throws ControllerException {
        
        long startTime = System.currentTimeMillis();
        LOGGER.info(String.format("Starting %s operation for %s", operationName, entityName));
        
        try {
            R result = operation.execute();
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info(String.format("%s operation completed in %d ms", operationName, duration));
            return result;
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, 
                String.format("%s failed: %s", operationName, e.getMessage()), 
                e);
            throw new ControllerException(
                String.format("Error during %s: %s", operationName.toLowerCase(), e.getUserMessage()),
                e.getErrorCode().name(),
                e
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, 
                String.format("Unexpected error during %s: %s", operationName, e.getMessage()), 
                e);
            throw new ControllerException(
                String.format("An unexpected error occurred during %s", operationName.toLowerCase()),
                "UNEXPECTED_ERROR",
                e
            );
        }
    }
    
    /**
     * Validates that an ID is not null.
     *
     * @param id The ID to validate
     * @param idName The name of the ID (for error messages)
     * @throws ControllerException If the ID is null
     */
    protected void validateId(ID id, String idName) throws ControllerException {
        if (id == null) {
            throw new ControllerException(
                String.format("%s ID cannot be null", idName),
                "INVALID_INPUT"
            );
        }
    }
    
    /**
     * Validates that an entity is not null.
     *
     * @param entity The entity to validate
     * @throws ControllerException If the entity is null
     */
    protected void validateEntity(T entity) throws ControllerException {
        if (entity == null) {
            throw new ControllerException(
                String.format("%s cannot be null", entityName),
                "INVALID_INPUT"
            );
        }
    }
    
    /**
     * Validates that an optional entity is present.
     *
     * @param optional The optional to validate
     * @param id The ID that was searched for
     * @return The entity if present
     * @throws ControllerException If the optional is empty
     */
    protected T validateOptional(Optional<T> optional, ID id) throws ControllerException {
        return optional.orElseThrow(() -> new ControllerException(
            String.format("%s with ID %s not found", entityName, id),
            "NOT_FOUND"
        ));
    }
    
    /**
     * Validates that a list is not empty.
     *
     * @param list The list to validate
     * @param errorMessage The error message to use if the list is empty
     * @throws ControllerException If the list is empty
     */
    protected <E> void validateListNotEmpty(List<E> list, String errorMessage) throws ControllerException {
        if (list == null || list.isEmpty()) {
            throw new ControllerException(errorMessage, "NOT_FOUND");
        }
    }
    
    /**
     * Functional interface for service operations that can throw ServiceException.
     *
     * @param <R> The return type of the operation
     */
    @FunctionalInterface
    protected interface ServiceOperation<R> {
        R execute() throws ServiceException;
    }
}
