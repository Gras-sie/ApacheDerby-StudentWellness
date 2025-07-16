package com.wellness.service;

import com.wellness.exception.ServiceException;
import com.wellness.model.Entity;
import com.wellness.dao.BaseDAO;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base service implementation providing common CRUD operations.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's identifier
 */
public abstract class BaseServiceImpl<T extends Entity, ID> implements BaseService<T, ID> {
    
    protected final BaseDAO<T, ID> dao;
    protected final String entityName;
    
    /**
     * Constructs a new BaseServiceImpl with the given DAO and entity name.
     *
     * @param dao The data access object
     * @param entityName The name of the entity (for error messages)
     */
    protected BaseServiceImpl(BaseDAO<T, ID> dao, String entityName) {
        this.dao = dao;
        this.entityName = entityName;
    }
    
    @Override
    public T save(T entity) throws ServiceException {
        try {
            validateForSave(entity);
            return dao.save(entity);
        } catch (Exception e) {
            throw ServiceException.databaseError("Error saving " + entityName, e);
        }
    }
    
    @Override
    public Optional<T> findById(ID id) throws ServiceException {
        try {
            if (id == null) {
                throw ServiceException.invalidInput("ID cannot be null");
            }
            return dao.findById(id);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding " + entityName + " by ID: " + id, e);
        }
    }
    
    @Override
    public List<T> findAll() throws ServiceException {
        try {
            return dao.findAll();
        } catch (Exception e) {
            throw ServiceException.databaseError("Error finding all " + entityName + "s", e);
        }
    }
    
    @Override
    public boolean delete(ID id) throws ServiceException {
        try {
            if (id == null) {
                throw ServiceException.invalidInput("ID cannot be null");
            }
            
            if (!dao.existsById(id)) {
                throw ServiceException.notFound(entityName, id);
            }
            
            validateForDelete(id);
            return dao.deleteById(id);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.databaseError("Error deleting " + entityName + " with ID: " + id, e);
        }
    }
    
    @Override
    public long count() throws ServiceException {
        try {
            return dao.count();
        } catch (Exception e) {
            throw ServiceException.databaseError("Error counting " + entityName + "s", e);
        }
    }
    
    /**
     * Validates an entity before saving.
     * Subclasses should override this to add specific validation logic.
     *
     * @param entity The entity to validate
     * @throws ServiceException If validation fails
     */
    protected void validateForSave(T entity) throws ServiceException {
        if (entity == null) {
            throw ServiceException.invalidInput(entityName + " cannot be null");
        }
        // Add common validation logic here
    }
    
    /**
     * Validates an entity before deletion.
     * Subclasses should override this to add specific validation logic.
     *
     * @param id The ID of the entity to delete
     * @throws ServiceException If validation fails
     */
    protected void validateForDelete(ID id) throws ServiceException {
        // Add common validation logic here
    }
    
    /**
     * Helper method to get an entity by ID or throw a not found exception.
     *
     * @param id The ID of the entity to find
     * @return The found entity
     * @throws ServiceException If the entity is not found
     */
    protected T getByIdOrThrow(ID id) throws ServiceException {
        return findById(id).orElseThrow(() -> 
            ServiceException.notFound(entityName, id)
        );
    }
}
