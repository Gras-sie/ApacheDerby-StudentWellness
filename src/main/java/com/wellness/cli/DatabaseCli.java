package com.wellness.cli;

import com.wellness.util.DatabaseManager;
import com.wellness.util.DatabaseMigrator;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command-line interface for database management tasks.
 */
public class DatabaseCli {
    private static final Logger LOGGER = Logger.getLogger(DatabaseCli.class.getName());
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length == 0) {
            showUsage();
            return;
        }

        String command = args[0].toLowerCase();
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            switch (command) {
                case "migrate":
                    handleMigrate(commandArgs);
                    break;
                case "reset":
                    handleReset(commandArgs);
                    break;
                case "backup":
                    handleBackup(commandArgs);
                    break;
                case "restore":
                    handleRestore(commandArgs);
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    showUsage();
                    System.exit(1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing command: " + command, e);
            System.exit(1);
        } finally {
            DatabaseManager.shutdown();
            scanner.close();
        }
    }

    private static void handleMigrate(String[] args) throws SQLException {
        System.out.println("Running database migrations...");
        DatabaseMigrator.migrate();
        System.out.println("Database migrations completed successfully.");
    }

    private static void handleReset(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: No confirmation provided. Use --force to confirm database reset.");
            System.exit(1);
        }

        if (!"--force".equals(args[0])) {
            System.err.println("Error: Reset requires --force flag to confirm.");
            System.exit(1);
        }

        System.out.println("WARNING: This will drop all database tables and data!");
        System.out.print("Are you sure you want to continue? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!"yes".equals(confirmation)) {
            System.out.println("Database reset cancelled.");
            return;
        }

        try {
            System.out.println("Initializing database...");
            DatabaseManager.initializeDatabase();
            
            System.out.println("Resetting database...");
            try (var conn = DatabaseManager.getConnection();
                 var stmt = conn.createStatement()) {
                
                // Drop all tables
                System.out.println("Dropping existing tables...");
                dropTableIfExists(stmt, "feedback");
                dropTableIfExists(stmt, "appointments");
                dropTableIfExists(stmt, "counselors");
                dropTableIfExists(stmt, "schema_migrations");
                
                System.out.println("Running migrations...");
                DatabaseMigrator.migrate();
                
                System.out.println("Database reset completed successfully.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reset database", e);
            throw new RuntimeException("Database reset failed: " + e.getMessage(), e);
        }
    }
    
    private static void dropTableIfExists(java.sql.Statement stmt, String tableName) throws SQLException {
        try {
            stmt.execute("DROP TABLE " + tableName);
            System.out.println("Dropped table: " + tableName);
        } catch (SQLException e) {
            // Ignore if table doesn't exist
            if (!e.getSQLState().equals("42Y55")) {
                System.err.println("Error dropping table " + tableName + ": " + e.getMessage());
                throw e;
            }
            System.out.println("Table " + tableName + " does not exist, skipping...");
        }
    }

    private static void handleBackup(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: No backup directory specified.");
            System.exit(1);
        }
        String backupDir = args[0];
        
        try {
            System.out.println("Creating database backup in: " + backupDir);
            DatabaseManager.backupDatabase(backupDir);
            System.out.println("Backup completed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Backup failed", e);
            throw new RuntimeException("Backup failed", e);
        }
    }

    private static void handleRestore(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: No backup directory specified.");
            System.exit(1);
        }
        String backupDir = args[0];
        
        System.out.println("WARNING: This will restore the database from backup, overwriting any existing data!");
        System.out.print("Are you sure you want to continue? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!"yes".equals(confirmation)) {
            System.out.println("Database restore cancelled.");
            return;
        }

        try {
            System.out.println("Restoring database from: " + backupDir);
            DatabaseManager.restoreDatabase(backupDir);
            System.out.println("Database restore completed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Restore failed", e);
            throw new RuntimeException("Restore failed", e);
        }
    }

    private static void showUsage() {
        System.out.println("Usage: java -cp <classpath> " + DatabaseCli.class.getName() + " <command> [args]");
        System.out.println("\nCommands:");
        System.out.println("  migrate                  Run database migrations");
        System.out.println("  reset --force            Reset database (drops all tables and data)");
        System.out.println("  backup <directory>       Create a backup of the database");
        System.out.println("  restore <directory>      Restore database from backup");
        System.out.println("\nExamples:");
        System.out.println("  java -cp target/classes " + DatabaseCli.class.getName() + " migrate");
        System.out.println("  java -cp target/classes " + DatabaseCli.class.getName() + " backup /path/to/backup");
    }
}
