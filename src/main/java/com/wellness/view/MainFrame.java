package com.wellness.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
    
    /**
     * Creates a new MainFrame.
     */
    public MainFrame() {
        initializeUI();
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
