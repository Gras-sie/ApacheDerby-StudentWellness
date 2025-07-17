package com.wellness.view;

import java.awt.*;
import javax.swing.*;

/**
 * Main frame for counselor management system.
 * Contains tabs for different counselor management functionalities.
 */
public class CounselorManagementFrame extends JFrame {
    
    public CounselorManagementFrame() {
        super("Counselor Management System");
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add counselor management panel
        CounselorPanel counselorPanel = new CounselorPanel();
        tabbedPane.addTab("Counselors", null, counselorPanel, "Manage Counselors");
        
        // Add availability management panel
        AvailabilityPanel availabilityPanel = new AvailabilityPanel();
        tabbedPane.addTab("Availability", null, availabilityPanel, "Manage Counselor Availability");
        
        // Add reports panel (placeholder for future implementation)
        JPanel reportsPanel = new JPanel();
        reportsPanel.add(new JLabel("Reports and Analytics (Coming Soon)"));
        tabbedPane.addTab("Reports", null, reportsPanel, "View Reports and Analytics");
        
        // Add tabbed pane to frame
        add(tabbedPane);
        
        // Add window listener to handle cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Perform any cleanup here if needed
                dispose();
            }
        });
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Schedule a job for the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            CounselorManagementFrame frame = new CounselorManagementFrame();
            frame.setVisible(true);
        });
    }
}
