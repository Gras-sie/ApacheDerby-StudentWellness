package com.wellness.dao;

import com.wellness.model.Entity;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base implementation of the BaseDAO interface.
 * Provides common CRUD operations for all entity DAOs.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's identifier (must be Integer)
 */
public abstract class BaseDAOImpl<T extends Entity, ID> implements BaseDAO<T, ID> {
    
    protected final Connection connection;
    protected final String tableName;
    protected static final Logger LOGGER = Logger.getLogger(BaseDAOImpl.class.getName());
    
    protected BaseDAOImpl(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }
    
    /**
     * Maps a ResultSet row to an entity object.
     * Must be implemented by concrete DAO classes.
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    /**
     * Sets the parameters for an INSERT statement.
     * Must be implemented by concrete DAO classes.
     */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Sets the parameters for an UPDATE statement.
     * Must be implemented by concrete DAO classes.
     */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Returns the SQL for an INSERT statement.
     */
    protected abstract String getInsertSQL();
    
    /**
     * Returns the SQL for an UPDATE statement.
     */
    protected abstract String getUpdateSQL();
    
    @Override
    public T save(T entity) {
        try {
            if (entity.getId() == null) {
                return create(entity);
            } else {
                update(entity);
                return entity;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving entity", e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }
    
    private T create(T entity) throws SQLException {
        String sql = getInsertSQL();
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, entity);
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating entity failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    @SuppressWarnings("unchecked")
                    ID id = (ID) Integer.valueOf(generatedKeys.getInt(1));
                    entity.setId((Integer) id);
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained.");
                }
            }
            
            return entity;
        }
    }
    
    private void update(T entity) throws SQLException {
        String sql = getUpdateSQL();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating entity failed, no rows affected.");
            }
        }
    }
    
    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (id instanceof Integer) {
                ps.setInt(1, (Integer) id);
            } else {
                throw new IllegalArgumentException("ID must be of type Integer");
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entity by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all entities", e);
        }
        
        return entities;
    }
    
    @Override
    public boolean existsById(ID id) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (id instanceof Integer) {
                ps.setInt(1, (Integer) id);
            } else {
                throw new IllegalArgumentException("ID must be of type Integer");
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if entity exists with ID: " + id, e);
            return false;
        }
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting entities", e);
        }
        
        return 0;
    }
    
@Override
    public boolean deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (id instanceof Integer) {
                ps.setInt(1, (Integer) id);
            } else {
                throw new IllegalArgumentException("ID must be of type Integer");
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting entity with ID: " + id, e);
            return false;
        }
    }
    
    @Override
    public boolean delete(T entity) {
        if (entity.getId() == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ID id = (ID) entity.getId();
        return deleteById(id);
    }
    
    @Override
    public long deleteAll() {
        String sql = "DELETE FROM " + tableName;
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting all entities", e);
            return 0;
        }
    }
    
    /**
     * Executes a query with the given parameters and returns the result as a list.
     * @param sql The SQL query
     * @param params The query parameters
     * @return A list of entities matching the query
     */
    protected List<T> executeQueryList(String sql, Object... params) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + sql, e);
        }
        
        return results;
    }
}
