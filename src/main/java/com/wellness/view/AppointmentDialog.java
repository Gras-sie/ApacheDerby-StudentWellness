package com.wellness.view;

import com.wellness.model.Appointment;
import com.wellness.model.Appointment.Status;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dialog for adding or editing appointments.
 */
public class AppointmentDialog extends JDialog {
    private final Appointment appointment;
    private boolean saved;
    
    // Form fields
    private JComboBox<String> studentCombo;
    private JComboBox<String> counselorCombo;
    private JFormattedTextField dateField;
    private JComboBox<String> timeCombo;
    private JComboBox<Status> statusCombo;
    private JTextArea notesArea;
    
    // Available time slots (30-minute intervals from 9:00 to 17:00)
    private final String[] TIME_SLOTS = {
        "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
        "15:00", "15:30", "16:00", "16:30", "17:00"
    };
    
    public AppointmentDialog(Frame owner, Appointment appointment) {
        super(owner, appointment == null ? "Add New Appointment" : "Edit Appointment", true);
        this.appointment = appointment != null ? appointment : new Appointment();
        this.saved = false;
        
        initializeUI();
        if (appointment != null) {
            loadAppointmentData();
        }
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        // Main panel with form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Student selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Student:"), gbc);
        
        gbc.gridx = 1;
        studentCombo = new JComboBox<>();
        // TODO: Load actual students
        studentCombo.addItem("John Smith (ID: 1001)");
        studentCombo.addItem("Jane Doe (ID: 1002)");
        formPanel.add(studentCombo, gbc);
        
        // Counselor selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Counselor:"), gbc);
        
        gbc.gridx = 1;
        counselorCombo = new JComboBox<>();
        // TODO: Load actual counselors
        counselorCombo.addItem("Dr. Sarah Johnson (Psychology)");
        counselorCombo.addItem("Dr. Michael Brown (Career)");
        counselorCombo.addActionListener(e -> updateAvailableTimes());
        formPanel.add(counselorCombo, gbc);
        
        // Date selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_');
            dateField = new JFormattedTextField(dateMask);
            dateField.setColumns(10);
            dateField.addActionListener(e -> updateAvailableTimes());
        } catch (java.text.ParseException ex) {
            dateField = new JFormattedTextField();
        }
        formPanel.add(dateField, gbc);
        
        // Time selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Time:"), gbc);
        
        gbc.gridx = 1;
        timeCombo = new JComboBox<>(TIME_SLOTS);
        formPanel.add(timeCombo, gbc);
        
        // Status selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(Status.values());
        formPanel.add(statusCombo, gbc);
        
        // Notes
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        formPanel.add(scrollPane, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveAppointment());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default button for Enter key
        getRootPane().setDefaultButton(saveButton);
        
        // Set minimum size
        setMinimumSize(new Dimension(450, 400));
    }
    
    private void loadAppointmentData() {
        if (appointment.getStudentId() != null) {
            // TODO: Set selected student in combo
            studentCombo.setSelectedIndex(0); // Placeholder
        }
        
        if (appointment.getCounselorId() != null) {
            // TODO: Set selected counselor in combo
            counselorCombo.setSelectedIndex(0); // Placeholder
        }
        
        if (appointment.getStartTime() != null) {
            // Set date
            dateField.setText(appointment.getStartTime().toLocalDate().toString());
            
            // Set time
            String timeStr = appointment.getStartTime().toLocalTime().toString();
            timeCombo.setSelectedItem(timeStr.substring(0, 5)); // Format as HH:mm
        }
        
        if (appointment.getStatus() != null) {
            statusCombo.setSelectedItem(appointment.getStatus());
        }
        
        if (appointment.getNotes() != null) {
            notesArea.setText(appointment.getNotes());
        }
    }
    
    private void updateAvailableTimes() {
        // Clear existing items
        timeCombo.removeAllItems();
        
        // Add all time slots for now (will be filtered by availability later)
        for (String slot : TIME_SLOTS) {
            timeCombo.addItem(slot);
        }
        
        // TODO: Filter time slots based on counselor availability
    }
    
    private boolean validateForm() {
        if (studentCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a student.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (counselorCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a counselor.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (dateField.getText() == null || dateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select a date.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (timeCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a time.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // TODO: Add more validation as needed
        
        return true;
    }
    
    private void saveAppointment() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Extract student ID from the selected item (format: "Name (ID: 123)")
            String studentInfo = (String) studentCombo.getSelectedItem();
            int studentId = Integer.parseInt(studentInfo.substring(studentInfo.indexOf("ID: ") + 4, studentInfo.length() - 1));

            // Extract counselor ID (placeholder - implement actual extraction)
            int counselorId = 1; // Placeholder

            // Get selected date and time
            LocalDate selectedDate = LocalDate.parse(dateField.getText());
            String timeStr = (String) timeCombo.getSelectedItem();
            LocalTime time = LocalTime.parse(timeStr);
            LocalDateTime dateTime = LocalDateTime.of(selectedDate, time);

            appointment.setStudentId(studentId);
            appointment.setCounselorId(counselorId);
            appointment.setStartTime(dateTime);
            appointment.setEndTime(dateTime.plusMinutes(30)); // 30-minute appointments
            appointment.setStatus((Status) statusCombo.getSelectedItem());
            appointment.setNotes(notesArea.getText().trim());

            // Mark as saved and close
            saved = true;
            dispose();

            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving appointment: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
}
