package com.wellness.view;

import com.wellness.controller.AppointmentController;
import com.wellness.model.Appointment;
import com.wellness.model.Appointment.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Panel for managing appointments in the Wellness Management System.
 * Provides a tabbed interface for viewing, adding, editing, and canceling appointments.
 */
public class AppointmentPanel extends JPanel {
    private final AppointmentController controller;
    private JTable appointmentTable;
    private JButton addButton;
    private JButton editButton;
    private JButton cancelButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JComboBox<Status> statusFilter;
    private JComboBox<String> counselorFilter;
    private JFormattedTextField dateFromFilter;
    private JFormattedTextField dateToFilter;
    private JButton exportButton;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public AppointmentPanel(AppointmentController controller) {
        this.controller = controller;
        initializeUI();
        loadAppointments();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        addButton = new JButton("New Appointment");
        addButton.addActionListener(e -> showAppointmentDialog(null));
        
        editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedAppointment());
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancelSelectedAppointment());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAppointments());
        
        exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> exportToCSV());
        
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(cancelButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(refreshButton);
        toolBar.add(exportButton);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.addActionListener(e -> applyFilters());
        filterPanel.add(searchField);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        filterPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(Status.values());
        statusFilter.insertItemAt(null, 0); // Add "All" option
        statusFilter.setSelectedIndex(0);
        statusFilter.addActionListener(e -> applyFilters());
        filterPanel.add(statusFilter);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        filterPanel.add(new JLabel("From:"));
        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_');
            dateFromFilter = new JFormattedTextField(dateMask);
            dateFromFilter.setColumns(10);
        } catch (ParseException ex) {
            dateFromFilter = new JFormattedTextField();
        }
        filterPanel.add(dateFromFilter);
        
        filterPanel.add(new JLabel("To:"));
        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_');
            dateToFilter = new JFormattedTextField(dateMask);
            dateToFilter.setColumns(10);
        } catch (ParseException ex) {
            dateToFilter = new JFormattedTextField();
        }
        filterPanel.add(dateToFilter);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        filterPanel.add(new JLabel("Counselor:"));
        counselorFilter = new JComboBox<>();
        counselorFilter.addItem(null); // Add "All" option
        // Load counselors from controller
        loadCounselors();
        counselorFilter.addActionListener(e -> applyFilters());
        filterPanel.add(counselorFilter);
        
        add(filterPanel, BorderLayout.CENTER);
        
        // Create appointment table
        appointmentTable = new JTable();
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setAutoCreateRowSorter(true);
        
        // Add double-click listener for editing
        appointmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedAppointment();
                }
            }
        });
        
        // Setup context menu is now handled in the constructor
        
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Status bar
        JLabel statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);
        
        // Setup document listeners
        setupDocumentListeners();
        
        // Setup context menu
        setupContextMenu();
    }
    
    private void setupDocumentListeners() {
        DocumentListener filterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components don't fire these events
            }
        };
        
        searchField.getDocument().addDocumentListener(filterListener);
        dateFromFilter.getDocument().addDocumentListener(filterListener);
        dateToFilter.getDocument().addDocumentListener(filterListener);
    }
    
    private void loadAppointments() {
        try {
            List<Appointment> appointments = controller.getAllAppointments();
            updateTable(appointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading appointments: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCounselors() {
        try {
            // Clear existing items
            counselorFilter.removeAllItems();
            
            // Add default "All" option
            counselorFilter.addItem(null);
            
            // Load counselors from controller - using hardcoded list for now
            // TODO: Replace with actual implementation when available
            List<String> counselors = new ArrayList<>();
            counselors.add("Dr. John Smith");
            counselors.add("Dr. Sarah Johnson");
            counselors.add("Dr. Michael Brown");
            
            for (String counselor : counselors) {
                counselorFilter.addItem(counselor);
            }
        } catch (Exception e) {
            System.err.println("Error loading counselors: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading counselors. Using default list.",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
                
            // Add default test data
            counselorFilter.addItem("Dr. John Smith");
            counselorFilter.addItem("Dr. Sarah Johnson");
            counselorFilter.addItem("Dr. Michael Brown");
        }
    }
    
    private void updateTable(List<Appointment> appointments) {
        String[] columnNames = {"ID", "Student", "Counselor", "Date/Time", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        for (Appointment appt : appointments) {
            // Format the appointment data for display
            Object[] row = {
                appt.getId(),
                "Student " + appt.getStudentId(), // Replace with actual student name
                "Counselor " + appt.getCounselorId(), // Replace with actual counselor name
                appt.getStartTime() != null ? appt.getStartTime().format(DATE_FORMAT) : "",
                appt.getStatus(),
                appt.getNotes()
            };
            model.addRow(row);
        }
        
        appointmentTable.setModel(model);
        
        // Set up table renderers
        appointmentTable.setDefaultRenderer(Status.class, new StatusCellRenderer());
    }
    
    private void applyFilters() {
        try {
            // Get filter values
            String searchText = searchField.getText().toLowerCase();
            Status status = (Status) statusFilter.getSelectedItem();
            LocalDate fromDate = null;
            LocalDate toDate = null;
            
            // Parse date filters
            String fromText = dateFromFilter.getText().trim();
            String toText = dateToFilter.getText().trim();
            
            if (!fromText.isEmpty() && fromText.length() == 10) {
                fromDate = LocalDate.parse(fromText, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            if (!toText.isEmpty() && toText.length() == 10) {
                toDate = LocalDate.parse(toText, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            // Apply filters using controller
            List<Appointment> filteredAppointments = controller.getAllAppointments();
            
            // Apply search text filter
            if (!searchText.isEmpty()) {
                filteredAppointments = filteredAppointments.stream()
                    .filter(a -> String.valueOf(a.getId()).contains(searchText) ||
                               String.valueOf(a.getStudentId()).contains(searchText) ||
                               String.valueOf(a.getCounselorId()).contains(searchText) ||
                               (a.getNotes() != null && a.getNotes().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
            }
            
            // Apply status filter
            if (status != null) {
                filteredAppointments = filteredAppointments.stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
            }
            
            // Apply date range filter
            final LocalDate filterFromDate = fromDate;
            final LocalDate filterToDate = toDate;
            
            filteredAppointments = filteredAppointments.stream()
                .filter(a -> a.getStartTime() != null && 
                          (filterFromDate == null || !a.getStartTime().toLocalDate().isBefore(filterFromDate)) &&
                          (filterToDate == null || !a.getStartTime().toLocalDate().isAfter(filterToDate)))
                .collect(Collectors.toList());
            
            // Update the table with filtered appointments
            updateTable(filteredAppointments);
            
        } catch (Exception e) {
            // Log error and show user-friendly message
            System.err.println("Error applying filters: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error applying filters. Please check your input and try again.",
                "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAppointmentDialog(Appointment appointment) {
        // Show appointment dialog with the selected appointment
        if (appointment != null) {
            AppointmentDialog dialog = new AppointmentDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                appointment
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadAppointments(); // Refresh the table
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Appointment dialog will be implemented here", 
                "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            int appointmentId = (int) appointmentTable.getModel().getValueAt(modelRow, 0);

            // Get the appointment from the controller
            try {
                // Load appointment from controller - using dummy implementation for now
                // TODO: Replace with actual implementation when available
                Appointment dummyAppointment = new Appointment();
                dummyAppointment.setId(appointmentId);
                dummyAppointment.setStudentId(1);
                dummyAppointment.setCounselorId(1);
                dummyAppointment.setStartTime(LocalDateTime.now());
                dummyAppointment.setStatus(Appointment.Status.SCHEDULED);
                dummyAppointment.setNotes("Sample appointment details");
                Optional<Appointment> appointmentOpt = Optional.of(dummyAppointment);
                if (appointmentOpt.isPresent()) {
                    showAppointmentDialog(appointmentOpt.get());
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Could not load appointment details.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading appointment: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            int appointmentId = (int) appointmentTable.getModel().getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = controller.cancelAppointment(appointmentId, "Cancelled by user");
                    if (success) {
                        loadAppointments();
                        JOptionPane.showMessageDialog(this,
                            "Appointment cancelled successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to cancel appointment.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Error cancelling appointment: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment to cancel.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> viewAppointmentDetails());
        
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> editSelectedAppointment());
        
        JMenuItem cancelItem = new JMenuItem("Cancel");
        cancelItem.addActionListener(e -> cancelSelectedAppointment());
        
        contextMenu.add(viewItem);
        contextMenu.add(editItem);
        contextMenu.add(cancelItem);
        
        // Set context menu for the table
        appointmentTable.setComponentPopupMenu(contextMenu);
        
        // Add mouse listener for right-click
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = appointmentTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < appointmentTable.getRowCount()) {
                        appointmentTable.setRowSelectionInterval(row, row);
                    } else {
                        appointmentTable.clearSelection();
                    }
                }
            }
        });
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                // Write header
                TableModel model = appointmentTable.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
                
                // Write data
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        writer.write(value != null ? value.toString() : "");
                        if (j < model.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }
                
                JOptionPane.showMessageDialog(this,
                    "Appointments exported successfully to: " + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting to CSV: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewAppointmentDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            int appointmentId = (int) appointmentTable.getModel().getValueAt(modelRow, 0);

            try {
                // Get the appointment from the controller
                // Load appointment from controller - using dummy implementation for now
                // TODO: Replace with actual implementation when available
                Appointment dummyAppointment = new Appointment();
                dummyAppointment.setId(appointmentId);
                dummyAppointment.setStudentId(1);
                dummyAppointment.setCounselorId(1);
                dummyAppointment.setStartTime(LocalDateTime.now());
                dummyAppointment.setStatus(Appointment.Status.SCHEDULED);
                dummyAppointment.setNotes("Sample appointment details");
                Optional<Appointment> appointmentOpt = Optional.of(dummyAppointment);
                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    StringBuilder details = new StringBuilder();
                    details.append("<html><b>Appointment Details</b><br><br>");
                    details.append("<b>ID:</b> ").append(appointment.getId()).append("<br>");
                    details.append("<b>Student ID:</b> ").append(appointment.getStudentId()).append("<br>");
                    details.append("<b>Counselor ID:</b> ").append(appointment.getCounselorId()).append("<br>");
                    details.append("<b>Date/Time:</b> ").append(appointment.getStartTime()).append("<br>");
                    details.append("<b>Status:</b> ").append(appointment.getStatus()).append("<br>");
                    details.append("<b>Notes:</b> ").append(appointment.getNotes() != null ? appointment.getNotes() : "").append("<br>");

                    JOptionPane.showMessageDialog(this, 
                        details.toString(), 
                        "Appointment Details", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Appointment not found.", 
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading appointment details: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to view details.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Custom cell renderer for appointment status
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value instanceof Status) {
                Status status = (Status) value;
                switch (status) {
                    case SCHEDULED:
                        c.setBackground(new Color(255, 255, 200)); // Light yellow
                        break;
                    case COMPLETED:
                        c.setBackground(new Color(200, 255, 200)); // Light green
                        break;
                    case CANCELLED:
                        c.setBackground(new Color(255, 200, 200)); // Light red
                        break;
                    default:
                        c.setBackground(table.getBackground());
                }
            }
            
            return c;
        }
    }
}
