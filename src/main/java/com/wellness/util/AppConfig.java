package com.wellness.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages application configuration settings.
 * Loads and saves settings from/to a properties file.
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "config/application.properties";
    private static final String DEFAULT_CONFIG_FILE = "/config/default-application.properties";
    
    private static AppConfig instance;
    private final Properties properties;
    private final File configFile;
    
    // Configuration keys
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_PATH = "db.path";
    public static final String UI_THEME = "ui.theme";
    public static final String UI_LANGUAGE = "ui.language";
    public static final String AUTO_BACKUP = "backup.auto";
    public static final String BACKUP_RETENTION_DAYS = "backup.retention.days";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private AppConfig() {
        properties = new Properties();
        configFile = new File(CONFIG_FILE);
        loadConfiguration();
    }
    
    /**
     * Gets the singleton instance of AppConfig.
     *
     * @return the AppConfig instance
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    /**
     * Loads the configuration from the properties file.
     * If the file doesn't exist, creates it with default values.
     */
    private void loadConfiguration() {
        try {
            // Create config directory if it doesn't exist
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            // If config file doesn't exist, create it with default values
            if (!configFile.exists()) {
                createDefaultConfig();
                return;
            }
            
            // Load existing configuration
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                LOGGER.info("Configuration loaded from " + configFile.getAbsolutePath());
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading configuration", e);
            // Try to load defaults if there's an error
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Creates a default configuration file.
     */
    private void createDefaultConfig() throws IOException {
        LOGGER.info("Creating default configuration file");
        
        // Set default values
        setDefaultProperties();
        
        // Save to file
        saveConfiguration();
    }
    
    /**
     * Loads the default configuration from the classpath.
     */
    private void loadDefaultConfiguration() {
        LOGGER.info("Loading default configuration from classpath");
        try (var is = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                LOGGER.info("Default configuration loaded from classpath");
            } else {
                LOGGER.warning("Default configuration file not found in classpath: " + DEFAULT_CONFIG_FILE);
                setDefaultProperties();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading default configuration", e);
            setDefaultProperties();
        }
    }
    
    /**
     * Sets default configuration properties.
     */
    private void setDefaultProperties() {
        // Database settings
        properties.setProperty(DB_URL, "jdbc:derby:wellnessDB;create=true");
        properties.setProperty(DB_USER, "wellness");
        properties.setProperty(DB_PASSWORD, "wellness123");
        properties.setProperty(DB_PATH, "wellnessDB");
        
        // UI settings
        properties.setProperty(UI_THEME, "light");
        properties.setProperty(UI_LANGUAGE, "en");
        
        // Backup settings
        properties.setProperty(AUTO_BACKUP, "true");
        properties.setProperty(BACKUP_RETENTION_DAYS, "30");
    }
    
    /**
     * Saves the current configuration to the properties file.
     *
     * @return true if the configuration was saved successfully, false otherwise
     */
    public boolean saveConfiguration() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "Wellness Management System Configuration");
            LOGGER.info("Configuration saved to " + configFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving configuration", e);
            return false;
        }
    }
    
    /**
     * Gets a string property value.
     *
     * @param key the property key
     * @param defaultValue the default value to return if the key is not found
     * @return the property value, or the default value if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets a string property value.
     *
     * @param key the property key
     * @return the property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets an integer property value.
     *
     * @param key the property key
     * @param defaultValue the default value to return if the key is not found or is not a valid integer
     * @return the property value as an integer, or the default value if not found or invalid
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a boolean property value.
     *
     * @param key the property key
     * @param defaultValue the default value to return if the key is not found
     * @return the property value as a boolean, or the default value if not found
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Sets a property value.
     *
     * @param key the property key
     * @param value the property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Sets a boolean property value.
     *
     * @param key the property key
     * @param value the property value
     */
    public void setProperty(String key, boolean value) {
        properties.setProperty(key, Boolean.toString(value));
    }
    
    /**
     * Sets an integer property value.
     *
     * @param key the property key
     * @param value the property value
     */
    public void setProperty(String key, int value) {
        properties.setProperty(key, Integer.toString(value));
    }
    
    /**
     * Reloads the configuration from the properties file.
     */
    public void reload() {
        loadConfiguration();
    }
    
    /**
     * Gets all properties as a Properties object.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return new Properties(properties);
    }
}
