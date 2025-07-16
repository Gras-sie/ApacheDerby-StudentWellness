package com.wellness.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for common database operations.
 */
public class DbUtils {
    private static final Logger LOGGER = Logger.getLogger(DbUtils.class.getName());
    
    private DbUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     * 
     * @param sql The SQL query to execute
     * @param params The parameters for the prepared statement
     * @return The number of affected rows
     * @throws SQLException if a database error occurs
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing update query: " + sql, e);
            throw e;
        }
    }
    
    /**
     * Executes a query and returns a ResultSet.
     * 
     * @param sql The SQL query to execute
     * @param params The parameters for the prepared statement
     * @return A ResultSet containing the query results
     * @throws SQLException if a database error occurs
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            conn.close(); // Close connection on error
            LOGGER.log(Level.SEVERE, "Error executing query: " + sql, e);
            throw e;
        }
    }
    
    /**
     * Executes a query and returns the result as a list of objects.
     * 
     * @param sql The SQL query to execute
     * @param rowMapper A function that maps a ResultSet row to an object
     * @param params The parameters for the prepared statement
     * @param <T> The type of objects in the result list
     * @return A list of objects mapped from the query results
     * @throws SQLException if a database error occurs
     */
    public static <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) 
            throws SQLException {
        
        List<T> results = new ArrayList<>();
        try (ResultSet rs = executeQuery(sql, params)) {
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }
    
    /**
     * Executes a query and returns a single result object.
     * 
     * @param sql The SQL query to execute
     * @param rowMapper A function that maps a ResultSet row to an object
     * @param params The parameters for the prepared statement
     * @param <T> The type of the result object
     * @return The result object, or null if no results
     * @throws SQLException if a database error occurs
     */
    public static <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) 
            throws SQLException {
        
        try (ResultSet rs = executeQuery(sql, params)) {
            return rs.next() ? rowMapper.mapRow(rs) : null;
        }
    }
    
    /**
     * Sets parameters on a PreparedStatement.
     * 
     * @param pstmt The PreparedStatement to set parameters on
     * @param params The parameters to set
     * @throws SQLException if a database error occurs
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) 
            throws SQLException {
        
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * Functional interface for mapping ResultSet rows to objects.
     * 
     * @param <T> The type of object to map to
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
    
    /**
     * Closes resources quietly, logging any exceptions.
     * 
     * @param closeables The resources to close
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error closing resource", e);
                }
            }
        }
    }
    
    /**
     * Starts a transaction.
     * 
     * @param conn The database connection
     * @throws SQLException if a database error occurs
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }
    
    /**
     * Commits a transaction.
     * 
     * @param conn The database connection
     * @throws SQLException if a database error occurs
     */
    public static void commitTransaction(Connection conn) throws SQLException {
        try {
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Rolls back a transaction.
     * 
     * @param conn The database connection
     */
    public static void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error rolling back transaction", e);
        }
    }
}
