package com.wellness.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles scheduling of automatic database backups.
 */
public class BackupScheduler {
    private static final Logger LOGGER = Logger.getLogger(BackupScheduler.class.getName());
    private static final long DEFAULT_BACKUP_INTERVAL = TimeUnit.HOURS.toMillis(24); // 24 hours
    private static final long INITIAL_DELAY = TimeUnit.MINUTES.toMillis(5); // 5 minutes
    
    private final Timer timer;
    private final AppConfig config;
    private boolean isRunning;
    
    /**
     * Creates a new BackupScheduler with the specified configuration.
     */
    public BackupScheduler() {
        this.timer = new Timer("DatabaseBackupScheduler", true);
        this.config = AppConfig.getInstance();
        this.isRunning = false;
    }
    
    /**
     * Starts the backup scheduler.
     */
    public void start() {
        if (isRunning) {
            LOGGER.warning("Backup scheduler is already running");
            return;
        }
        
        if (!config.getBooleanProperty(AppConfig.AUTO_BACKUP, true)) {
            LOGGER.info("Automatic backups are disabled in configuration");
            return;
        }
        
        long interval = getBackupInterval();
        LOGGER.info("Starting backup scheduler with interval: " + formatInterval(interval));
        
        timer.scheduleAtFixedRate(new BackupTask(), INITIAL_DELAY, interval);
        isRunning = true;
        
        // Perform an immediate backup if one hasn't been done yet today
        if (shouldRunInitialBackup()) {
            LOGGER.info("Performing initial backup");
            new Thread(new BackupTask(), "InitialBackup").start();
        }
    }
    
    /**
     * Stops the backup scheduler.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        LOGGER.info("Stopping backup scheduler");
        timer.cancel();
        isRunning = false;
    }
    
    /**
     * Gets the backup interval from configuration.
     * 
     * @return the backup interval in milliseconds
     */
    private long getBackupInterval() {
        try {
            String intervalStr = config.getProperty("backup.interval.hours", "24");
            long hours = Long.parseLong(intervalStr);
            return TimeUnit.HOURS.toMillis(Math.max(1, hours)); // Minimum 1 hour
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid backup interval in configuration, using default", e);
            return DEFAULT_BACKUP_INTERVAL;
        }
    }
    
    /**
     * Checks if an initial backup should be performed.
     * 
     * @return true if an initial backup should be performed, false otherwise
     */
    private boolean shouldRunInitialBackup() {
        // Check if a backup exists from today
        // If not, we should run an initial backup
        return DatabaseBackupUtil.getLatestBackup() == null;
    }
    
    /**
     * Formats a time interval in a human-readable format.
     */
    private String formatInterval(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return (millis / 1000) + " seconds";
        } else if (millis < 3600000) {
            return (millis / 60000) + " minutes";
        } else if (millis < 86400000) {
            return (millis / 3600000) + " hours";
        } else {
            return (millis / 86400000) + " days";
        }
    }
    
    /**
     * TimerTask implementation for performing backups.
     */
    private class BackupTask extends TimerTask {
        @Override
        public void run() {
            try {
                LOGGER.info("Starting scheduled database backup");
                String backupPath = DatabaseBackupUtil.createBackup();
                if (backupPath != null) {
                    LOGGER.info("Scheduled backup completed successfully: " + backupPath);
                    
                    // Clean up old backups based on retention policy
                    int retentionDays = config.getIntProperty("backup.retention.days", 30);
                    DatabaseBackupUtil.cleanupOldBackups(retentionDays);
                } else {
                    LOGGER.warning("Scheduled backup failed");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during scheduled backup", e);
            }
        }
    }
}
