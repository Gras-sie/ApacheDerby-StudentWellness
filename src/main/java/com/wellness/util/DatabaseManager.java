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
        initializeDatabase();
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
     * Initializes the database by creating necessary tables if they don't exist.
     */
    private static void initializeDatabase() {
        String[] createTables = {
            // Create Counselors table
            "CREATE TABLE counselors (" +
            "  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "  name VARCHAR(100) NOT NULL," +
            "  specialization VARCHAR(100)," +
            "  availability VARCHAR(100)," +
            "  email VARCHAR(100) NOT NULL UNIQUE," +
            "  phone VARCHAR(20)" +
            ")",
            
            // Create Appointments table
            "CREATE TABLE appointments (" +
            "  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "  student_name VARCHAR(100) NOT NULL," +
            "  counselor_id INT NOT NULL," +
            "  appointment_date DATE NOT NULL," +
            "  appointment_time TIME NOT NULL," +
            "  status VARCHAR(20) DEFAULT 'SCHEDULED'," +
            "  notes CLOB," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE CASCADE" +
            ")",
            
            // Create Feedback table
            "CREATE TABLE feedback (" +
            "  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "  student_name VARCHAR(100) NOT NULL," +
            "  counselor_id INT NOT NULL," +
            "  rating INT CHECK (rating BETWEEN 1 AND 5)," +
            "  comments CLOB," +
            "  feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE CASCADE" +
            ")"
        };
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Execute a simple query to verify the connection is valid
            stmt.execute("SELECT 1 FROM SYS.SYSTABLES");
            
            // Check if tables already exist
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "COUNSELORS", null);
            
            if (!tables.next()) {
                // Tables don't exist, create them
                for (String query : createTables) {
                    stmt.executeUpdate(query);
                }
                LOGGER.info("Database tables created successfully");
                
                // Insert sample data
                insertSampleData(conn);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Inserts sample data into the database for testing purposes.
     */
    private static void insertSampleData(Connection conn) throws SQLException {
        String[] sampleData = {
            // Insert sample counselors
            "INSERT INTO counselors (name, specialization, availability, email, phone) VALUES " +
            "('Dr. Sarah Johnson', 'Stress Management', 'Mon-Fri 9am-5pm', 'sarah.johnson@wellness.com', '123-456-7890')",
            
            "INSERT INTO counselors (name, specialization, availability, email, phone) VALUES " +
            "('Dr. Michael Chen', 'Anxiety Disorders', 'Tue-Thu 10am-6pm', 'michael.chen@wellness.com', '123-456-7891')",
            
            // Insert sample appointments
            "INSERT INTO appointments (student_name, counselor_id, appointment_date, appointment_time, status) VALUES " +
            "('John Doe', 1, CURRENT_DATE, '14:30:00', 'SCHEDULED')",
            
            // Insert sample feedback
            "INSERT INTO feedback (student_name, counselor_id, rating, comments) VALUES " +
            "('Jane Smith', 1, 5, 'Excellent session, very helpful!')"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String query : sampleData) {
                stmt.executeUpdate(query);
            }
            LOGGER.info("Sample data inserted successfully");
        }
    }
    
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
            dir.mkdirs();
        }
        
        // Create a connection without using the pool for backup operation
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Normalize the path and escape single quotes for SQL
            String normalizedPath = dir.getAbsolutePath();
            normalizedPath = normalizedPath.replace("'", "''");
            
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
        }
        
        // Normalize the backup directory path
        String normalizedPath = new java.io.File(backupDir).getAbsolutePath();
        
        // Now restore from backup using a direct connection
        String restoreUrl = "jdbc:derby:;restoreFrom=" + normalizedPath;
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
