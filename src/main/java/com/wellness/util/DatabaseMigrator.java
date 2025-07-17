package com.wellness.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles database schema migrations by executing SQL scripts from the classpath.
 * Migrations are executed in version order based on the file naming convention:
 * V{version}__{description}.sql
 */
public class DatabaseMigrator {
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrator.class.getName());
    private static final String MIGRATION_PATH = "/db/migration/";
    private static final Pattern MIGRATION_FILE_PATTERN = 
        Pattern.compile("^V(\\d+)__[a-zA-Z0-9_]+\\.sql$");

    private DatabaseMigrator() {
        // Utility class
    }

    /**
     * Executes all pending database migrations.
     * @throws SQLException if a database error occurs
     */
    public static void migrate() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            ensureMigrationTable(conn);
            List<String> migrations = findMigrations();
            executeMigrations(conn, migrations);
            LOGGER.info("Database migrations completed successfully");
        } catch (IOException e) {
            throw new SQLException("Failed to read migration files", e);
        }
    }

    /**
     * Ensures the schema_migrations table exists.
     */
    private static void ensureMigrationTable(Connection conn) throws SQLException {
        String createTable = "CREATE TABLE IF NOT EXISTS schema_migrations (" +
                          "version VARCHAR(50) PRIMARY KEY," +
                          "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                          ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
        }
    }

    /**
     * Finds all migration files in the classpath.
     */
    private static List<String> findMigrations() throws IOException {
        List<String> migrations = new ArrayList<>();
        try (var in = DatabaseMigrator.class.getResourceAsStream(MIGRATION_PATH);
             var br = new BufferedReader(new InputStreamReader(
                 Objects.requireNonNull(in), StandardCharsets.UTF_8))) {
            
            String resource;
            while ((resource = br.readLine()) != null) {
                if (MIGRATION_FILE_PATTERN.matcher(resource).matches()) {
                    migrations.add(resource);
                }
            }
        }
        migrations.sort(String::compareTo);
        return migrations;
    }

    /**
     * Executes all pending migrations.
     */
    private static void executeMigrations(Connection conn, List<String> migrations) 
            throws SQLException, IOException {
        
        for (String migration : migrations) {
            String version = migration.split("__")[0].substring(1); // Remove 'V' prefix
            
            // Check if migration was already applied
            if (isMigrationApplied(conn, version)) {
                LOGGER.log(Level.INFO, "Skipping already applied migration: {0}", migration);
                continue;
            }

            LOGGER.log(Level.INFO, "Applying migration: {0}", migration);
            String sql = loadMigrationFile(migration);
            
            try (Statement stmt = conn.createStatement()) {
                // Split SQL by semicolon but ignore those within strings
                String[] sqlStatements = sql.split(";\\s*\n");
                for (String sqlStmt : sqlStatements) {
                    if (!sqlStmt.trim().isEmpty()) {
                        stmt.execute(sqlStmt);
                    }
                }
                
                // Record the migration
                recordMigration(conn, version);
                LOGGER.log(Level.INFO, "Applied migration: {0}", migration);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to apply migration: " + migration, e);
                throw new SQLException("Migration failed: " + migration, e);
            }
        }
    }

    /**
     * Checks if a migration was already applied.
     */
    private static boolean isMigrationApplied(Connection conn, String version) throws SQLException {
        String sql = "SELECT 1 FROM schema_migrations WHERE version = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, version);
            return stmt.executeQuery().next();
        }
    }

    /**
     * Records a migration as applied.
     */
    private static void recordMigration(Connection conn, String version) throws SQLException {
        String sql = "INSERT INTO schema_migrations (version) VALUES (?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, version);
            stmt.executeUpdate();
        }
    }

    /**
     * Loads a migration file from the classpath.
     */
    private static String loadMigrationFile(String filename) throws IOException {
        String resource = MIGRATION_PATH + filename;
        try (InputStream in = DatabaseMigrator.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Migration file not found: " + resource);
            }
            return new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        }
    }
}
