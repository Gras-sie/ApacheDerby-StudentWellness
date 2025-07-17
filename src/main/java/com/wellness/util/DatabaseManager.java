package com.wellness.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * DatabaseManager handles all database operations including connection management,
 * database initialization, and resource cleanup.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:derby:wellnessDB;create=true";
    private static final String DB_USER = "wellness";
    private static final String DB_PASSWORD = "wellness123";
    
    private static BasicDataSource dataSource;
    private static final Object lock = new Object();
    
    static {
        initializeDataSource();
    }
    
    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initializes the connection pool with the specified configuration.
     */
    private static void initializeDataSource() {
        try {
            // Create a new BasicDataSource and set properties directly
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
            dataSource.setUrl(DB_URL);
            dataSource.setUsername(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            dataSource.setInitialSize(5);
            dataSource.setMaxTotal(20);
            dataSource.setMaxIdle(10);
            dataSource.setMinIdle(5);
            dataSource.setMaxWait(java.time.Duration.ofSeconds(10));
            
            LOGGER.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database connection pool", e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Initializes the database by running migrations.
     * This is automatically called when the application starts.
     */
    public static void initializeDatabase() {
        try {
            DatabaseMigrator.migrate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Resets the database by dropping all tables and running migrations again.
     * WARNING: This will delete all data in the database.
     * @throws SQLException if a database error occurs
     */
    public static void resetDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Disable foreign key constraints
            stmt.execute("SET CONSTRAINTS ALL DEFERRED");
            
            // Drop tables if they exist
            dropTableIfExists(stmt, "feedback");
            dropTableIfExists(stmt, "appointments");
            dropTableIfExists(stmt, "counselors");
            dropTableIfExists(stmt, "schema_migrations");
            
            LOGGER.info("Dropped all database tables");
            
            // Re-run migrations to recreate schema
            DatabaseMigrator.migrate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error resetting database", e);
            throw e;
        }
    }
    
    /**
     * Drops a table if it exists.
     */
    private static void dropTableIfExists(Statement stmt, String tableName) throws SQLException {
        try {
            stmt.execute("DROP TABLE " + tableName);
            LOGGER.log(Level.INFO, "Dropped table: {0}", tableName);
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("42Y55")) {
                // Table doesn't exist, which is fine
                LOGGER.log(Level.FINE, "Table {0} does not exist, skipping drop", tableName);
            } else {
                throw e;
            }
        }
    }
    
    // Note: Removed unused helper methods exportTable, truncateTable, and importTable
    // as they are no longer needed with the migration-based approach
    
    /**
     * Gets a database connection from the connection pool.
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initializeDataSource();
                }
            }
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes the data source and shuts down the embedded Derby database.
     */
    public static void shutdown() {
        try {
            if (dataSource != null) {
                dataSource.close();
                LOGGER.info("Database connection pool closed successfully");
                
                // Shutdown Derby
                try {
                    DriverManager.getConnection("jdbc:derby:;shutdown=true");
                } catch (SQLException e) {
                    // Expected exception on successful shutdown
                    if (e.getErrorCode() == 45000 && "XJ015".equals(e.getSQLState())) {
                        LOGGER.info("Derby shutdown completed successfully");
                    } else {
                        LOGGER.log(Level.SEVERE, "Error shutting down Derby", e);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection pool", e);
        }
    }
    
    /**
     * Creates a backup of the database to the specified directory.
     * @param backupDir The directory where the backup should be stored
     * @throws SQLException if a database error occurs
     */
    public static void backupDatabase(String backupDir) throws SQLException {
        // Ensure the backup directory exists
        java.io.File dir = new java.io.File(backupDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new SQLException("Failed to create backup directory: " + backupDir);
            }
        }
        
        // Normalize the path and escape single quotes for SQL
        String normalizedPath = dir.getAbsolutePath();
        normalizedPath = normalizedPath.replace("'", "''");
        
        // Create a connection without using the pool for backup operation
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            String backupCommand = String.format(
                "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE('%s')", 
                normalizedPath
            );
            
            LOGGER.info("Creating database backup with command: " + backupCommand);
            stmt.execute(backupCommand);
            LOGGER.info("Database backup created successfully in: " + normalizedPath);
        }
    }
    
    /**
     * Restores the database from a backup in the specified directory.
     * @param backupDir The directory containing the backup
     * @throws SQLException if a database error occurs
     */
    public static void restoreDatabase(String backupDir) throws SQLException {
        // First, shutdown the database
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing data source during restore", e);
            }
            dataSource = null;
            // Also shutdown Derby to release all file locks
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException e) {
                // Expected error code/state on successful shutdown
                if (!(e.getErrorCode() == 45000 && "XJ015".equals(e.getSQLState()))) {
                    LOGGER.log(Level.WARNING, "Unexpected error during Derby shutdown before restore", e);
                }
            }
        }
        
        // Normalize the backup directory path
        String normalizedPath = new java.io.File(backupDir).getAbsolutePath();
        
        // Now restore from backup using a direct connection (must include DB name)
        String baseUrl = DB_URL.split(";")[0]; // e.g., jdbc:derby:wellnessDB
        String restoreUrl = baseUrl + ";restoreFrom=" + normalizedPath;
        LOGGER.info("Attempting to restore database from: " + normalizedPath);
        
        try (Connection conn = DriverManager.getConnection(restoreUrl);
             Statement stmt = conn.createStatement()) {
            // Execute a simple query to verify the connection is valid
            stmt.execute("SELECT 1 FROM SYS.SYSTABLES");
            LOGGER.info("Database restored and verified from: " + normalizedPath);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error restoring database", e);
            throw e;
        }
        
        // Reinitialize the data source after restore
        try {
            initializeDataSource();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reinitializing data source after restore", e);
            throw new SQLException("Failed to reinitialize data source after restore", e);
        }
    }
}
