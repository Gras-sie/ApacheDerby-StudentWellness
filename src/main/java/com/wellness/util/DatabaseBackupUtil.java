package com.wellness.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for backing up and restoring the database.
 */
public class DatabaseBackupUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseBackupUtil.class.getName());
    private static final String BACKUP_DIR = "backups";
    private static final String BACKUP_PREFIX = "wellness_backup_";
    private static final String BACKUP_EXT = ".zip";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String DB_PROPERTIES = "database.properties";

    private DatabaseBackupUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a backup of the database.
     *
     * @return the path to the created backup file, or null if backup failed
     */
    public static String createBackup() {
        String backupPath = null;
        String timestamp = DATE_FORMAT.format(new Date());
        String backupFileName = BACKUP_PREFIX + timestamp + BACKUP_EXT;
        
        try {
            // Create backup directory if it doesn't exist
            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            
            // Get the database directory from properties
            String dbPath = getDatabasePath();
            if (dbPath == null) {
                LOGGER.severe("Failed to determine database path from properties");
                return null;
            }
            
            // Create a zip file for the backup
            Path backupFile = backupDir.resolve(backupFileName);
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile.toFile()))) {
                // Add database files to the zip
                addDirectoryToZip("", new File(dbPath), zos);
                
                // Add database properties if it exists
                File dbProps = new File(DB_PROPERTIES);
                if (dbProps.exists()) {
                    addFileToZip(DB_PROPERTIES, dbProps, zos);
                }
                
                backupPath = backupFile.toAbsolutePath().toString();
                LOGGER.info("Database backup created successfully: " + backupPath);
            }
            
            // Clean up old backups (keep last 5)
            cleanupOldBackups(5);
            
            return backupPath;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating database backup", e);
            return null;
        }
    }
    
    /**
     * Restores the database from a backup file.
     *
     * @param backupFile the backup file to restore from
     * @return true if restore was successful, false otherwise
     */
    public static boolean restoreBackup(File backupFile) {
        if (backupFile == null || !backupFile.exists() || !backupFile.getName().endsWith(BACKUP_EXT)) {
            LOGGER.warning("Invalid backup file: " + (backupFile != null ? backupFile.getPath() : "null"));
            return false;
        }
        
        LOGGER.info("Starting database restore from: " + backupFile.getAbsolutePath());
        
        try {
            // 1. Shutdown the database
            if (!shutdownDatabase()) {
                LOGGER.severe("Failed to shutdown database before restore");
                return false;
            }
            
            // 2. Create a temporary directory for extraction
            Path tempDir = Files.createTempDirectory("wellness_restore_" + System.currentTimeMillis());
            LOGGER.info("Created temporary directory for restore: " + tempDir);
            
            try {
                // 3. Extract the backup to the temporary directory
                if (!extractBackup(backupFile, tempDir)) {
                    LOGGER.severe("Failed to extract backup file");
                    return false;
                }
                
                // 4. Get the database path from configuration
                String dbPath = getDatabasePath();
                if (dbPath == null) {
                    LOGGER.severe("Failed to determine database path from properties");
                    return false;
                }
                
                // 5. Backup current database (just in case)
                String timestamp = DATE_FORMAT.format(new Date());
                Path currentDbBackup = Paths.get(BACKUP_DIR, "pre_restore_" + timestamp);
                if (!backupCurrentDatabase(dbPath, currentDbBackup)) {
                    LOGGER.warning("Failed to backup current database before restore - continuing anyway");
                }
                
                // 6. Replace the database files
                if (!replaceDatabaseFiles(tempDir, dbPath)) {
                    LOGGER.severe("Failed to replace database files");
                    // Try to restore from our backup if available
                    if (Files.exists(currentDbBackup)) {
                        LOGGER.info("Attempting to restore from pre-restore backup");
                        replaceDatabaseFiles(currentDbBackup, dbPath);
                    }
                    return false;
                }
                
                LOGGER.info("Database restore completed successfully");
                return true;
                
            } finally {
                // 7. Clean up temporary directory
                try {
                    deleteDirectory(tempDir);
                    LOGGER.info("Cleaned up temporary restore directory");
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error cleaning up temporary directory", e);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during database restore", e);
            return false;
        } finally {
            // 8. Restart the database
            try {
                // The database will be restarted automatically on next connection
                DatabaseManager.initializeDatabase();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to reinitialize database after restore", e);
            }
        }
    }
    
    /**
     * Shuts down the Derby database.
     *
     * @return true if shutdown was successful, false otherwise
     */
    private static boolean shutdownDatabase() {
        try {
            // Try to get a connection with shutdown=true to properly shutdown Derby
            String shutdownUrl = "jdbc:derby:;shutdown=true";
            try (Connection conn = DriverManager.getConnection(shutdownUrl)) {
                // This should not be reached on successful shutdown
                LOGGER.warning("Database did not shut down as expected");
                return false;
            } catch (SQLException e) {
                // Expected exception on successful shutdown
                if (e.getErrorCode() == 50000 && "XJ015".equals(e.getSQLState())) {
                    LOGGER.info("Database shutdown completed successfully");
                    return true;
                }
                throw e; // Unexpected exception
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error shutting down database", e);
            return false;
        }
    }
    
    /**
     * Extracts a backup file to the specified directory.
     */
    private static boolean extractBackup(File backupFile, Path targetDir) {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(backupFile)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                Path entryPath = targetDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (java.io.InputStream is = zipFile.getInputStream(entry);
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(entryPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
            
            LOGGER.info("Backup extracted successfully to: " + targetDir);
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error extracting backup file", e);
            return false;
        }
    }
    
    /**
     * Backs up the current database to the specified location.
     */
    private static boolean backupCurrentDatabase(String dbPath, Path backupPath) {
        try {
            File dbDir = new File(dbPath);
            if (!dbDir.exists()) {
                LOGGER.warning("Database directory does not exist: " + dbPath);
                return false;
            }
            
            // Create a zip of the current database
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupPath.toFile()))) {
                addDirectoryToZip("", dbDir, zos);
                LOGGER.info("Created backup of current database at: " + backupPath);
                return true;
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating backup of current database", e);
            return false;
        }
    }
    
    /**
     * Replaces the database files with the restored files.
     */
    private static boolean replaceDatabaseFiles(Path sourceDir, String targetDbPath) {
        try {
            // Delete existing database files
            File targetDir = new File(targetDbPath);
            if (targetDir.exists()) {
                if (!deleteDirectory(targetDir.toPath())) {
                    LOGGER.severe("Failed to delete existing database directory");
                    return false;
                }
            }
            
            // Copy the restored files
            Files.createDirectories(targetDir.toPath().getParent());
            Files.move(sourceDir, targetDir.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            LOGGER.info("Database files replaced successfully");
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error replacing database files", e);
            return false;
        }
    }
    
    /**
     * Gets the most recent backup file.
     *
     * @return the most recent backup file, or null if no backups exist
     */
    public static File getLatestBackup() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return null;
        }
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> 
            name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_EXT)
        );
        
        if (backupFiles == null || backupFiles.length == 0) {
            return null;
        }
        
        // Sort by last modified date (newest first)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> 
            Long.compare(f2.lastModified(), f1.lastModified())
        );
        
        return backupFiles[0];
    }
    
    /**
     * Deletes backup files older than the specified number of days.
     *
     * @param retentionDays the number of days to keep backups
     * @return the number of backups deleted
     */
    public static int cleanupOldBackups(int retentionDays) {
        if (retentionDays < 1) {
            LOGGER.warning("Invalid retention days: " + retentionDays + ", using default of 30");
            retentionDays = 30;
        }
        
        long cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);
        File backupDir = new File(BACKUP_DIR);
        
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            LOGGER.info("Backup directory does not exist: " + backupDir.getAbsolutePath());
            return 0;
        }
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> 
            name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_EXT)
        );
        
        if (backupFiles == null || backupFiles.length == 0) {
            LOGGER.info("No backup files found to clean up");
            return 0;
        }
        
        int deletedCount = 0;
        for (File backup : backupFiles) {
            if (backup.lastModified() < cutoffTime) {
                if (backup.delete()) {
                    LOGGER.info("Deleted old backup: " + backup.getName());
                    deletedCount++;
                } else {
                    LOGGER.warning("Failed to delete old backup: " + backup.getName());
                }
            }
        }
        
        LOGGER.info("Cleaned up " + deletedCount + " old backup(s)");
        return deletedCount;
    }
    
    /**
     * Recursively deletes a directory and all its contents.
     */
    private static boolean deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return true;
        }
        
        Files.walkFileTree(directory, new java.nio.file.SimpleFileVisitor<Path>() {
            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) 
                    throws IOException {
                Files.delete(file);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
            
            @Override
            public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc) 
                    throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
        
        return true;
    }
    
    /**
     * Gets the database path from the properties file.
     *
     * @return the database path, or null if not found
     */
    private static String getDatabasePath() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(DB_PROPERTIES)) {
            props.load(fis);
            return props.getProperty("db.path");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading database properties", e);
            return null;
        }
    }
    
    /**
     * Adds a directory to a zip file.
     */
    private static void addDirectoryToZip(String parentPath, File dir, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                String path = parentPath + file.getName() + "/";
                zos.putNextEntry(new ZipEntry(path));
                zos.closeEntry();
                addDirectoryToZip(path, file, zos);
            } else {
                addFileToZip(parentPath + file.getName(), file, zos);
            }
        }
    }
    
    /**
     * Adds a file to a zip file.
     */
    private static void addFileToZip(String entryName, File file, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
    }
}
