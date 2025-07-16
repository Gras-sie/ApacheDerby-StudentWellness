package com.wellness.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for UI-related constants and helper methods.
 */
public class UIUtils {
    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(51, 122, 183);  // Bootstrap primary blue
    public static final Color SUCCESS_COLOR = new Color(92, 184, 92);   // Bootstrap success green
    public static final Color INFO_COLOR = new Color(91, 192, 222);     // Bootstrap info blue
    public static final Color WARNING_COLOR = new Color(240, 173, 78);  // Bootstrap warning orange
    public static final Color DANGER_COLOR = new Color(217, 83, 79);    // Bootstrap danger red
    public static final Color LIGHT_GRAY = new Color(248, 249, 250);    // Light background
    public static final Color BORDER_COLOR = new Color(206, 212, 218);  // Border color
    
    // Fonts
    private static final String FONT_FAMILY = "Segoe UI";
    public static final Font HEADER_FONT = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font NORMAL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 12);
    
    // Sizes and dimensions
    public static final Dimension BUTTON_SIZE = new Dimension(120, 32);
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(160, 40);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(200, 28);
    public static final Insets STANDARD_MARGIN = new Insets(10, 10, 10, 10);
    
    // Icons (using system icons for now - replace with custom icons later)
    public static final Icon APPOINTMENT_ICON = UIManager.getIcon("OptionPane.informationIcon");
    public static final Icon COUNSELOR_ICON = UIManager.getIcon("OptionPane.informationIcon");
    public static final Icon FEEDBACK_ICON = UIManager.getIcon("OptionPane.informationIcon");
    public static final Icon DASHBOARD_ICON = UIManager.getIcon("OptionPane.informationIcon");
    
    /**
     * Creates a standardized button with consistent styling.
     *
     * @param text The button text
     * @return A styled JButton
     */
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    /**
     * Creates a panel with a titled border.
     *
     * @param title The title text
     * @return A JPanel with a titled border
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            TITLE_FONT,
            Color.DARK_GRAY
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * Creates a standardized label with consistent styling.
     *
     * @param text The label text
     * @param bold Whether the text should be bold
     * @return A styled JLabel
     */
    public static JLabel createLabel(String text, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? TITLE_FONT : NORMAL_FONT);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }
    
    /**
     * Creates a standardized text field with consistent styling.
     *
     * @return A styled JTextField
     */
    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(NORMAL_FONT);
        textField.setPreferredSize(TEXT_FIELD_SIZE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return textField;
    }
    
    /**
     * Centers a window on the screen.
     *
     * @param window The window to center
     */
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(
            (screenSize.width - window.getWidth()) / 2,
            (screenSize.height - window.getHeight()) / 2
        );
    }
}
