package com.wellness;

import com.wellness.util.DatabaseManager;
import com.wellness.util.DbUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for database operations.
 */
public class DatabaseTest {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTest.class.getName());
    
    @BeforeAll
    public static void setUp() {
        // DatabaseManager is initialized automatically when the class is loaded
        LOGGER.info("Starting database tests...");
    }
    
    @AfterAll
    public static void tearDown() {
        // DatabaseManager.shutdown(); // Uncomment if you want to shut down after tests
    }
    
    @Test
    public void testDatabaseConnection() {
        try (java.sql.Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn, "Database connection should not be null");
            assertFalse(conn.isClosed(), "Database connection should be open");
            
            // Test if we can execute a simple query
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("VALUES 1")) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals(1, rs.getInt(1), "Should return 1");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            fail("Database connection test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testTablesExist() {
        String[] tables = {"APPOINTMENTS", "COUNSELORS", "FEEDBACK"};
        
        try (java.sql.Connection conn = DatabaseManager.getConnection()) {
            java.sql.DatabaseMetaData dbmd = conn.getMetaData();
            
            for (String table : tables) {
                try (java.sql.ResultSet rs = dbmd.getTables(null, null, table, null)) {
                    assertTrue(rs.next(), "Table " + table + " should exist");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Table existence test failed", e);
            fail("Table existence test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testSampleDataInserted() {
        try {
            // Test counselors table
            String sql = "SELECT COUNT(*) FROM counselors";
            Integer count = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
            assertTrue(count != null && count > 0, "Should have at least one counselor");
            
            // Test appointments table
            sql = "SELECT COUNT(*) FROM appointments";
            count = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
            assertTrue(count != null && count >= 0, "Should return a count of appointments");
            
            // Test feedback table
            sql = "SELECT COUNT(*) FROM feedback";
            count = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
            assertTrue(count != null && count > 0, "Should have at least one feedback entry");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Sample data test failed", e);
            fail("Sample data test failed: " + e.getMessage());
        }
    }
    
    @Test
    void testBackupAndRestore() {
        String backupDir = System.getProperty("java.io.tmpdir") + "/wellness_backup_" + System.currentTimeMillis();
        
        try (java.sql.Connection conn = DatabaseManager.getConnection()) {
            // Start with a clean state - delete any existing test data
            String cleanupSql = "DELETE FROM counselors WHERE email LIKE 'test_%'";
            DbUtils.executeUpdate(cleanupSql);
            
            // Take a backup
            DatabaseManager.backupDatabase(backupDir);
            
            // Get current data count
            String sql = "SELECT COUNT(*) FROM counselors";
            int originalCount = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
            
            try {
                // Add test data with a unique email
                String testEmail = "test_" + System.currentTimeMillis() + "@example.com";
                sql = "INSERT INTO counselors (name, specialization, email) VALUES (?, ?, ?)";
                DbUtils.executeUpdate(sql, "Test Counselor", "Test", testEmail);
                
                // Verify data was added
                sql = "SELECT COUNT(*) FROM counselors";
                int newCount = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
                assertTrue(newCount > originalCount, "Should have more counselors after insert");
                
                // Restore from backup
                DatabaseManager.restoreDatabase(backupDir);
                
                // Verify data was restored
                sql = "SELECT COUNT(*) FROM counselors";
                int restoredCount = DbUtils.queryForObject(sql, rs -> rs.getInt(1));
                assertEquals(originalCount, restoredCount, "Should have original count after restore");
                
                // Verify our test data is gone
                sql = "SELECT COUNT(*) FROM counselors WHERE email = ?";
                int testDataCount = DbUtils.queryForObject(sql, rs -> rs.getInt(1), testEmail);
                assertEquals(0, testDataCount, "Test data should not exist after restore");
                
            } finally {
                // Clean up backup directory
                try {
                    java.nio.file.Files.walk(java.nio.file.Paths.get(backupDir))
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to clean up backup directory", e);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Backup/restore test failed", e);
            fail("Backup/restore test failed: " + e.getMessage());
        }
    }
}
