package com.wellness.view;

import com.wellness.controller.CounselorController;
import com.wellness.model.Counselor;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;   
import javax.swing.*;
import javax.swing.table.*;

/**
 * Panel for managing counselor availability and schedules.
 */
public class AvailabilityPanel extends JPanel {
    private final CounselorController controller = CounselorController.getInstance();
    private JComboBox<Counselor> counselorCombo;
    private JTable scheduleTable;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton applyButton;
    private JButton clearButton;
    private JButton saveButton;
    
    // Time slots for counselor availability
    private static final LocalTime[] TIME_SLOTS = {
        LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
        LocalTime.of(11, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
        LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    };
    
    // Data model for the schedule
    private final Map<LocalDate, Map<LocalTime, Boolean>> scheduleData = new HashMap<>();
    
    public AvailabilityPanel() {
        initializeUI();
        loadCounselors();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // Counselor selection
        toolBar.add(new JLabel("Counselor:"));
        counselorCombo = new JComboBox<>();
        counselorCombo.addActionListener(e -> loadSchedule());
        toolBar.add(counselorCombo);
        
        toolBar.add(Box.createHorizontalStrut(20));
        
        // Date range selection
        toolBar.add(new JLabel("From:"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "MMM dd, yyyy"));
        toolBar.add(startDateSpinner);
        
        toolBar.add(new JLabel("To:"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "MMM dd, yyyy"));
        toolBar.add(endDateSpinner);
        
        // Set default date range (next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);
        setDate(startDateSpinner, today);
        setDate(endDateSpinner, nextWeek);
        
        // Action buttons
        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> loadSchedule());
        toolBar.add(applyButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearSchedule());
        toolBar.add(clearButton);
        
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSchedule());
        toolBar.add(saveButton);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Create schedule table
        scheduleTable = new JTable(new ScheduleTableModel());
        scheduleTable.setRowHeight(30);
        scheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        TableColumnModel columnModel = scheduleTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Time column
        for (int i = 1; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(120); // Day columns
        }
        
        // Enable cell selection
        scheduleTable.setCellSelectionEnabled(true);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Weekly Schedule"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Add quick action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton setWeekdayButton = new JButton("Set Weekday Hours");
        setWeekdayButton.addActionListener(e -> markWeekdayHours(true));
        
        JButton clearWeekdayButton = new JButton("Clear Weekday Hours");
        clearWeekdayButton.addActionListener(e -> markWeekdayHours(false));
        
        JButton setWeekendButton = new JButton("Set Weekend Hours");
        setWeekendButton.addActionListener(e -> markWeekendHours(true));
        
        JButton clearWeekendButton = new JButton("Clear Weekend Hours");
        clearWeekendButton.addActionListener(e -> markWeekendHours(false));
        
        buttonPanel.add(setWeekdayButton);
        buttonPanel.add(clearWeekdayButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(setWeekendButton);
        buttonPanel.add(clearWeekendButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCounselors() {
        try {
            List<Counselor> counselors = controller.getAllCounselors();
            counselorCombo.removeAllItems();
            counselors.forEach(counselorCombo::addItem);
            
            if (counselorCombo.getItemCount() > 0) {
                loadSchedule();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading counselors: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSchedule() {
        Counselor selectedCounselor = (Counselor) counselorCombo.getSelectedItem();
        if (selectedCounselor == null) {
            return;
        }
        
        scheduleData.clear();
        LocalDate startDate = getDate(startDateSpinner);
        LocalDate endDate = getDate(endDateSpinner);
        
        try {
            // Initialize schedule data structure with default values
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                Map<LocalTime, Boolean> daySchedule = new HashMap<>();
                for (LocalTime time : TIME_SLOTS) {
                    // Load default availability (initially all false)
                    // Database integration will be implemented in the controller
                    daySchedule.put(time, false);
                }
                scheduleData.put(date, daySchedule);
            }
            
            // Update table
            ((ScheduleTableModel) scheduleTable.getModel()).fireTableDataChanged();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading schedule: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveSchedule() {
        Counselor selectedCounselor = (Counselor) counselorCombo.getSelectedItem();
        if (selectedCounselor == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a counselor", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Save availability to database through the controller
            // This is a placeholder for the actual database integration
            scheduleData.forEach((date, timeSlots) -> {
                timeSlots.forEach((time, isAvailable) -> {
                    if (isAvailable) {
                        // This will be replaced with actual database calls
                        // to save the availability for the selected counselor
                        System.out.printf("Saving availability: %s at %s%n", date, time);
                    }
                });
            });
            
            JOptionPane.showMessageDialog(this, 
                "Schedule saved successfully", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving schedule: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearSchedule() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Clear all availability for the selected date range?", 
            "Confirm Clear", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            for (Map<LocalTime, Boolean> daySchedule : scheduleData.values()) {
                for (LocalTime time : daySchedule.keySet()) {
                    daySchedule.put(time, false);
                }
            }
            ((ScheduleTableModel) scheduleTable.getModel()).fireTableDataChanged();
        }
    }
    
    private void markWeekdayHours(boolean available) {
        LocalDate startDate = getDate(startDateSpinner);
        LocalDate endDate = getDate(endDateSpinner);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                Map<LocalTime, Boolean> daySchedule = scheduleData.get(date);
                if (daySchedule != null) {
                    for (LocalTime time : TIME_SLOTS) {
                        if (time.isAfter(LocalTime.of(7, 59)) && time.isBefore(LocalTime.of(17, 0))) {
                            daySchedule.put(time, available);
                        }
                    }
                }
            }
        }
        
        ((ScheduleTableModel) scheduleTable.getModel()).fireTableDataChanged();
    }
    
    private void markWeekendHours(boolean available) {
        LocalDate startDate = getDate(startDateSpinner);
        LocalDate endDate = getDate(endDateSpinner);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                Map<LocalTime, Boolean> daySchedule = scheduleData.get(date);
                if (daySchedule != null) {
                    for (LocalTime time : TIME_SLOTS) {
                        daySchedule.put(time, available);
                    }
                }
            }
        }
        
        ((ScheduleTableModel) scheduleTable.getModel()).fireTableDataChanged();
    }
    
    // Helper methods
    private LocalDate getDate(JSpinner spinner) {
        return ((java.sql.Date) spinner.getValue()).toLocalDate();
    }
    
    private void setDate(JSpinner spinner, LocalDate date) {
        spinner.setValue(java.sql.Date.valueOf(date));
    }
    
    // Table model for the schedule
    private class ScheduleTableModel extends AbstractTableModel {
        private final String[] columnNames;
        
        public ScheduleTableModel() {
            // First column is for time slots, rest are days
            this.columnNames = new String[8];
            columnNames[0] = "Time";
            
            // Get the start date
            LocalDate date = getDate(startDateSpinner);
            
            // Set column names to day names and dates
            for (int i = 1; i < columnNames.length; i++) {
                if (date.isAfter(getDate(endDateSpinner))) {
                    columnNames[i] = "";
                } else {
                    columnNames[i] = String.format("%s\n%s", 
                        date.getDayOfWeek().toString().substring(0, 3),
                        date.format(DateTimeFormatter.ofPattern("MM/dd")));
                    date = date.plusDays(1);
                }
            }
        }
        
        @Override
        public int getRowCount() {
            return TIME_SLOTS.length;
        }
        
        @Override
        public int getColumnCount() {
            return 8; // Time + 7 days
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0; // Only day cells are editable
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                // Time column
                return TIME_SLOTS[rowIndex].toString();
            } else {
                // Day columns
                LocalDate date = getDate(startDateSpinner).plusDays(columnIndex - 1);
                if (date.isAfter(getDate(endDateSpinner))) {
                    return null;
                }
                
                Map<LocalTime, Boolean> daySchedule = scheduleData.get(date);
                if (daySchedule == null) {
                    return false;
                }
                
                return daySchedule.getOrDefault(TIME_SLOTS[rowIndex], false);
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex > 0) {
                LocalDate date = getDate(startDateSpinner).plusDays(columnIndex - 1);
                if (!date.isAfter(getDate(endDateSpinner))) {
                    Map<LocalTime, Boolean> daySchedule = scheduleData.computeIfAbsent(date, k -> new HashMap<>());
                    daySchedule.put(TIME_SLOTS[rowIndex], (Boolean) aValue);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
            }
        }
    }
}
