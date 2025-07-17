package com.wellness.view;

import com.wellness.controller.CounselorController;
import com.wellness.model.Counselor;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.text.ParseException;
import javax.swing.text.MaskFormatter;

/**
 * Dialog for adding or editing a counselor's information.
 */
public class CounselorDialog extends JDialog {
    private final CounselorController controller;
    private final Counselor counselor;
    private boolean saved;
    
    // Form fields
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JFormattedTextField phoneField;
    private JComboBox<String> specializationCombo;
    private JTextArea bioArea;
    private JLabel photoLabel;
    private JCheckBox activeCheckBox;
    private File photoFile;
    
    // Common specializations
    private static final String[] SPECIALIZATIONS = {
        "General Counseling",
        "Mental Health",
        "Academic Advising",
        "Career Counseling",
        "Addiction Counseling",
        "Family Therapy"
    };
    
    public CounselorDialog(Frame owner, Counselor counselor) {
        super(owner, counselor == null ? "Add New Counselor" : "Edit Counselor", true);
        this.controller = CounselorController.getInstance();
        this.counselor = counselor != null ? counselor : new Counselor();
        this.saved = false;
        
        initializeUI();
        loadCounselorData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(600, 500));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: Photo
        photoLabel = new JLabel(new ImageIcon(createDefaultPhoto()));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JButton uploadButton = new JButton("Upload Photo");
        uploadButton.addActionListener(e -> uploadPhoto());
        
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        photoPanel.add(uploadButton, BorderLayout.SOUTH);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        formPanel.add(photoPanel, gbc);
        
        // Reset gridbag constraints
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: First Name
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        formPanel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 2;
        firstNameField = new JTextField(20);
        formPanel.add(firstNameField, gbc);
        
        // Row 1: Last Name
        gbc.gridx = 1;
        gbc.gridy++;
        formPanel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 2;
        lastNameField = new JTextField(20);
        formPanel.add(lastNameField, gbc);
        
        // Row 2: Email
        gbc.gridx = 1;
        gbc.gridy++;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 2;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Row 3: Phone
        gbc.gridx = 1;
        gbc.gridy++;
        formPanel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 2;
        try {
            MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");
            phoneFormatter.setPlaceholderCharacter('_');
            phoneField = new JFormattedTextField(phoneFormatter);
            phoneField.setColumns(15);
            formPanel.add(phoneField, gbc);
        } catch (ParseException e) {
            phoneField = new JFormattedTextField();
            formPanel.add(phoneField, gbc);
        }
        
        // Row 4: Specialization
        gbc.gridx = 1;
        gbc.gridy++;
        formPanel.add(new JLabel("Specialization:"), gbc);
        
        gbc.gridx = 2;
        specializationCombo = new JComboBox<>(SPECIALIZATIONS);
        specializationCombo.setEditable(true);
        formPanel.add(specializationCombo, gbc);
        
        // Row 5: Bio (spans multiple rows)
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        JPanel bioPanel = new JPanel(new BorderLayout(5, 5));
        bioPanel.setBorder(BorderFactory.createTitledBorder("Biography"));
        
        bioArea = new JTextArea(5, 30);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        
        JScrollPane bioScrollPane = new JScrollPane(bioArea);
        bioPanel.add(bioScrollPane, BorderLayout.CENTER);
        
        // Add HTML formatting toolbar
        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);
        
        JButton boldButton = new JButton("B");
        boldButton.setFont(boldButton.getFont().deriveFont(Font.BOLD));
        boldButton.addActionListener(e -> insertHtmlTag("<b>", "</b>"));
        
        JButton italicButton = new JButton("I");
        italicButton.setFont(italicButton.getFont().deriveFont(Font.ITALIC));
        italicButton.addActionListener(e -> insertHtmlTag("<i>", "</i>"));
        
        JButton ulButton = new JButton("• List");
        ulButton.addActionListener(e -> insertHtmlTag("<ul>\n<li>", "</li>\n</ul>"));
        
        formatToolbar.add(boldButton);
        formatToolbar.add(italicButton);
        formatToolbar.add(ulButton);
        
        bioPanel.add(formatToolbar, BorderLayout.NORTH);
        formPanel.add(bioPanel, gbc);
        
        // Reset gridbag constraints
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 6: Active status
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setSelected(true);
        formPanel.add(activeCheckBox, gbc);
        
        // Add form panel to dialog
        add(formPanel, BorderLayout.CENTER);
        
        // Add button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCounselor());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        pack();
        setLocationRelativeTo(getParent());
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void loadCounselorData() {
        if (counselor != null) {
            firstNameField.setText(counselor.getFirstName());
            lastNameField.setText(counselor.getLastName());
            emailField.setText(counselor.getEmail());
            phoneField.setValue(counselor.getPhoneNumber());
            
            if (counselor.getSpecialization() != null) {
                specializationCombo.setSelectedItem(counselor.getSpecialization());
            }
            
            if (counselor.getBio() != null) {
                bioArea.setText(counselor.getBio());
            }
            
            activeCheckBox.setSelected(counselor.isActive());
            
            // Load photo if available
            if (counselor.getPhotoPath() != null && !counselor.getPhotoPath().isEmpty()) {
                try {
                    ImageIcon photoIcon = new ImageIcon(counselor.getPhotoPath());
                    Image img = photoIcon.getImage().getScaledInstance(
                        photoLabel.getWidth(), 
                        photoLabel.getHeight(), 
                        Image.SCALE_SMOOTH);
                    photoLabel.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    // If there's an error loading the photo, use the default
                    photoLabel.setIcon(new ImageIcon(createDefaultPhoto()));
                }
            }
        }
    }
    
    private void saveCounselor() {
        try {
            // Validate form
            if (firstNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update counselor object
            counselor.setFirstName(firstNameField.getText().trim());
            counselor.setLastName(lastNameField.getText().trim());
            counselor.setEmail(emailField.getText().trim());
            
            String phone = phoneField.getText().trim();
            counselor.setPhoneNumber(phone.isEmpty() ? null : phone);
            
            Object selectedSpecialization = specializationCombo.getSelectedItem();
            counselor.setSpecialization(selectedSpecialization != null ? selectedSpecialization.toString() : null);
            
            String bio = bioArea.getText().trim();
            counselor.setBio(bio.isEmpty() ? null : bio);
            
            counselor.setActive(activeCheckBox.isSelected());
            
            // Save photo if uploaded
            if (photoFile != null) {
                try {
                    // Create photos directory if it doesn't exist
                    File photosDir = new File("photos");
                    if (!photosDir.exists()) {
                        photosDir.mkdirs();
                    }
                    
                    // Generate unique filename
                    String extension = "";
                    String fileName = photoFile.getName();
                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        extension = fileName.substring(i);
                    }
                    String newFileName = "counselor_" + System.currentTimeMillis() + extension;
                    Path targetPath = new File(photosDir, newFileName).toPath();
                    
                    // Copy the file to the photos directory
                    Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Update the counselor's photo path
                    counselor.setPhotoPath(targetPath.toString());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error saving photo: " + e.getMessage(), 
                        "Photo Error", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            
            // Save counselor
            if (counselor.getId() == 0) {
                // New counselor
                controller.createCounselor(counselor);
            } else {
                // Update existing counselor
                controller.updateCounselor(counselor);
            }
            
            saved = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving counselor: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Counselor Photo");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Check file size (max 5MB)
                long fileSizeInMB = selectedFile.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    throw new Exception("Image size should be less than 5MB");
                }
                
                // Load and scale image
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                Image img = icon.getImage();
                
                // Scale image to fit 150x150 while maintaining aspect ratio
                int width = 150;
                int height = 150;
                
                if (icon.getIconWidth() > icon.getIconHeight()) {
                    height = (int)((double)icon.getIconHeight() / icon.getIconWidth() * width);
                } else {
                    width = (int)((double)icon.getIconWidth() / icon.getIconHeight() * height);
                }
                
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(scaledImg));
                photoFile = selectedFile;
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void insertHtmlTag(String startTag, String endTag) {
        int start = bioArea.getSelectionStart();
        int end = bioArea.getSelectionEnd();
        String selectedText = bioArea.getSelectedText();
        
        if (selectedText == null) {
            selectedText = "";
        }
        
        String newText = startTag + selectedText + endTag;
        bioArea.replaceSelection(newText);
        
        // Set cursor position after the inserted text
        if (selectedText.isEmpty()) {
            bioArea.setCaretPosition(start + startTag.length());
        } else {
            bioArea.setCaretPosition(end + startTag.length() + endTag.length());
        }
        
        bioArea.requestFocus();
    }
    
    private Image createDefaultPhoto() {
        // Create a default profile image
        int width = 150;
        int height = 150;
        
        // Create a buffered image with transparency
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // Get graphics context
        Graphics2D g2d = image.createGraphics();
        
        // Draw background
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRect(0, 0, width, height);
        
        // Draw user icon
        g2d.setColor(Color.GRAY);
        g2d.fillOval(25, 25, 100, 100);
        
        // Draw user icon details
        g2d.setColor(Color.WHITE);
        g2d.fillOval(50, 40, 50, 40); // Head
        g2d.fillOval(45, 80, 60, 50);  // Body
        
        // Clean up
        g2d.dispose();
        
        return image;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public static void main(String[] args) {
        // For testing
        SwingUtilities.invokeLater(() -> {
            CounselorDialog dialog = new CounselorDialog(null, null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }
}
