package com.wellness.view;

import com.wellness.util.BackupScheduler;
import com.wellness.util.DatabaseBackupUtil;
import com.wellness.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application window for the Wellness Management System.
 * Provides the main navigation framework and UI components.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private static final String APP_TITLE = "Wellness Management System";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private BackupScheduler backupScheduler;
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    
    /**
     * Creates a new MainFrame.
     */
    public MainFrame() {
        initializeUI();
        initializeBackupScheduler();
        addShutdownHook();
    }
    
    /**
     * Initializes the user interface components.
     */
    private void initializeUI() {
        // Set up the main window
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Center on screen
        
        // Set application icon
        try {
            // Replace with actual icon path
            // ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }
        
        // Initialize components
        createMenuBar();
        createMainContent();
        createStatusBar();
        
        // Apply system look and feel for a more native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Error setting system look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Creates and configures the menu bar.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        // Backup submenu
        JMenu backupMenu = new JMenu("Backup");
        backupMenu.setMnemonic(KeyEvent.VK_B);
        
        JMenuItem backupNowItem = new JMenuItem("Backup Now");
        backupNowItem.addActionListener(e -> performBackup());
        
        JMenuItem restoreItem = new JMenuItem("Restore from Backup...");
        restoreItem.addActionListener(e -> showRestoreDialog());
        
        JMenuItem backupSettingsItem = new JMenuItem("Backup Settings...");
        backupSettingsItem.addActionListener(e -> showBackupSettings());
        
        backupMenu.add(backupNowItem);
        backupMenu.add(restoreItem);
        backupMenu.addSeparator();
        backupMenu.add(backupSettingsItem);
        
        fileMenu.add(backupMenu);
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(exitItem);
        
        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue()); // Right-align help menu
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Creates the main content area with tabbed navigation.
     */
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        
        // Add tabs (placeholders for now)
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Appointments", createAppointmentsPanel());
        tabbedPane.addTab("Counselors", createCounselorsPanel());
        tabbedPane.addTab("Feedback", createFeedbackPanel());
        
        // Set keyboard navigation
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_D);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_A);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_C);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_F);
        
        // Add tooltips for tabs
        tabbedPane.setToolTipTextAt(0, "View system overview and statistics");
        tabbedPane.setToolTipTextAt(1, "Manage appointments");
        tabbedPane.setToolTipTextAt(2, "Manage counselors");
        tabbedPane.setToolTipTextAt(3, "View and manage feedback");
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the status bar at the bottom of the window.
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        
        statusLabel = new JLabel(" Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        JLabel connectionStatus = new JLabel("Connected to database");
        connectionStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(connectionStatus, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Initializes and starts the backup scheduler.
     */
    private void initializeBackupScheduler() {
        try {
            backupScheduler = new BackupScheduler();
            backupScheduler.start();
            LOGGER.info("Backup scheduler started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize backup scheduler", e);
            JOptionPane.showMessageDialog(this,
                "Failed to initialize backup scheduler: " + e.getMessage(),
                "Backup Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Adds a shutdown hook to ensure proper cleanup.
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (backupScheduler != null) {
                backupScheduler.stop();
                LOGGER.info("Backup scheduler stopped");
            }
        }));
    }
    
    /**
     * Updates the status bar message.
     * 
     * @param message the status message to display
     */
    private void setStatus(String message) {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(" " + message));
        }
    }
    
    /**
     * Performs a manual backup of the database.
     */
    private void performBackup() {
        new Thread(() -> {
            try {
                setStatus("Creating backup...");
                String backupPath = DatabaseBackupUtil.createBackup();
                if (backupPath != null) {
                    JOptionPane.showMessageDialog(this,
                        "Backup created successfully!\nLocation: " + backupPath,
                        "Backup Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to create backup. Check logs for details.",
                        "Backup Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during manual backup", e);
                JOptionPane.showMessageDialog(this,
                    "Error creating backup: " + e.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
            } finally {
                setStatus("Ready");
            }
        }, "ManualBackup").start();
    }
    
    /**
     * Shows the restore backup dialog.
     */
    private void showRestoreDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
            }
            
            @Override
            public String getDescription() {
                return "Backup Files (*.zip)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "WARNING: This will replace the current database with the backup.\n" +
                    "Are you sure you want to continue?",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        try {
                            setStatus("Restoring from backup...");
                            boolean success = DatabaseBackupUtil.restoreBackup(selectedFile);
                            if (success) {
                                JOptionPane.showMessageDialog(this,
                                    "Database restored successfully!\n" +
                                    "The application will now restart to apply changes.",
                                    "Restore Complete",
                                    JOptionPane.INFORMATION_MESSAGE);
                                
                                // Restart the application
                                restartApplication();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                    "Failed to restore backup. Check logs for details.",
                                    "Restore Failed",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error during restore", e);
                            JOptionPane.showMessageDialog(this,
                                "Error restoring backup: " + e.getMessage(),
                                "Restore Error",
                                JOptionPane.ERROR_MESSAGE);
                        } finally {
                            setStatus("Ready");
                        }
                    }, "RestoreBackup").start();
                }
            }
        }
    }
    
    /**
     * Restarts the application.
     */
    private void restartApplication() {
        try {
            // Get the java command used to start this application
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            // Get the classpath
            String classpath = System.getProperty("java.class.path");
            // Get the main class name
            String mainClass = this.getClass().getCanonicalName();
            
            // Build the command
            ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, mainClass);
                
            // Start the new process
            builder.start();
            
            // Exit the current application
            System.exit(0);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error restarting application", e);
            JOptionPane.showMessageDialog(this,
                "Error restarting application. Please restart manually.",
                "Restart Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the backup settings dialog.
     */
    private void showBackupSettings() {
        AppConfig config = AppConfig.getInstance();
        boolean autoBackup = config.getBooleanProperty(AppConfig.AUTO_BACKUP, true);
        String intervalHours = config.getProperty("backup.interval.hours", "24");
        String retentionDays = config.getProperty("backup.retention.days", "30");
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        JCheckBox autoBackupCheck = new JCheckBox("Enable automatic backups", autoBackup);
        JTextField intervalField = new JTextField(intervalHours, 5);
        JTextField retentionField = new JTextField(retentionDays, 5);
        
        panel.add(new JLabel("Automatic Backups:"));
        panel.add(autoBackupCheck);
        panel.add(new JLabel("Backup Interval (hours):"));
        panel.add(intervalField);
        panel.add(new JLabel("Retention Period (days):"));
        panel.add(retentionField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Backup Settings", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Validate input
                int hours = Integer.parseInt(intervalField.getText().trim());
                int days = Integer.parseInt(retentionField.getText().trim());
                
                if (hours < 1 || days < 1) {
                    throw new NumberFormatException("Values must be greater than 0");
                }
                
                // Save settings
                config.setProperty(AppConfig.AUTO_BACKUP, autoBackupCheck.isSelected());
                config.setProperty("backup.interval.hours", String.valueOf(hours));
                config.setProperty("backup.retention.days", String.valueOf(days));
                
                if (!config.saveConfiguration()) {
                    throw new Exception("Failed to save configuration");
                }
                
                // Restart scheduler if settings changed
                if (backupScheduler != null) {
                    backupScheduler.stop();
                    backupScheduler = new BackupScheduler();
                    backupScheduler.start();
                }
                
                JOptionPane.showMessageDialog(this,
                    "Backup settings saved successfully.",
                    "Settings Saved",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid input. Please enter valid numbers greater than 0.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving backup settings", e);
                JOptionPane.showMessageDialog(this,
                    "Error saving settings: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Placeholder panel creation methods
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Dashboard - Coming Soon"));
        return panel;
    }
    
    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Appointments - Coming Soon"));
        return panel;
    }
    
    private JPanel createCounselorsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Counselors - Coming Soon"));
        return panel;
    }
    
    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Feedback - Coming Soon"));
        return panel;
    }
    
    /**
     * Displays the About dialog.
     */
    private void showAboutDialog() {
        String aboutText = "<html><div style='width:300px;'>" +
            "<h2>Wellness Management System</h2>" +
            "<p>Version 1.0.0</p>" +
            "<p>© 2025 Wellness Management System</p>" +
            "<p>Manage student wellness appointments and feedback.</p>" +
            "</div></html>";
            
        JOptionPane.showMessageDialog(
            this,
            aboutText,
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Updates the status bar message.
     *
     * @param message The message to display
     */
    public void setStatusMessage(String message) {
        statusLabel.setText(" " + message);
    }
    
    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Run the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
