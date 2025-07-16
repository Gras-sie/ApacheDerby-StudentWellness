package com.wellness.view;

import com.wellness.controller.AppointmentController;
import com.wellness.model.Appointment;
import com.wellness.model.Appointment.Status;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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
    private JXDatePicker dateFromFilter;
    private JXDatePicker dateToFilter;
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
        dateFromFilter = new JXDatePicker();
        dateFromFilter.setFormats("yyyy-MM-dd");
        dateFromFilter.setPreferredSize(new Dimension(120, dateFromFilter.getPreferredSize().height));
        dateFromFilter.getDateEditor().addPropertyChangeListener(e -> applyFilters());
        filterPanel.add(dateFromFilter);
        
        filterPanel.add(new JLabel("To:"));
        dateToFilter = new JXDatePicker();
        dateToFilter.setFormats("yyyy-MM-dd");
        dateToFilter.setPreferredSize(new Dimension(120, dateToFilter.getPreferredSize().height));
        dateToFilter.getDateEditor().addPropertyChangeListener(e -> applyFilters());
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
        
        // Enable right-click context menu
        createContextMenu();
        
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Status bar
        JLabel statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);
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
        // Implementation to load counselors from controller
        // This is a placeholder - implement actual loading
        counselorFilter.removeAllItems();
        counselorFilter.addItem(null); // Add "All" option
        // TODO: Load actual counselors from controller
        counselorFilter.addItem("John Doe");
        counselorFilter.addItem("Jane Smith");
    }
    
    private void updateTable(List<Appointment> appointments) {
        String[] columnNames = {"ID", "Student", "Counselor", "Date/Time", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
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
        // Get filter values
        String searchText = searchField.getText().toLowerCase();
        Status status = (Status) statusFilter.getSelectedItem();
        Date fromDate = dateFromFilter.getDate();
        Date toDate = dateToFilter.getDate();
        
        // Apply filters using controller
        // TODO: Implement filter logic
    }
    
    private void showAppointmentDialog(Appointment appointment) {
        // TODO: Implement appointment dialog
        JOptionPane.showMessageDialog(this, 
            "Appointment dialog will be implemented here", 
            "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void editSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            int appointmentId = (int) appointmentTable.getModel().getValueAt(modelRow, 0);
            // TODO: Load appointment by ID and show edit dialog
            showAppointmentDialog(null);
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
                    // TODO: Call controller to cancel appointment
                    JOptionPane.showMessageDialog(this, 
                        "Appointment cancelled successfully.", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error cancelling appointment: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to cancel.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void createContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> editSelectedAppointment());
        
        JMenuItem cancelItem = new JMenuItem("Cancel");
        cancelItem.addActionListener(e -> cancelSelectedAppointment());
        
        JMenuItem viewDetailsItem = new JMenuItem("View Details");
        viewDetailsItem.addActionListener(e -> viewAppointmentDetails());
        
        popupMenu.add(editItem);
        popupMenu.add(cancelItem);
        popupMenu.addSeparator();
        popupMenu.add(viewDetailsItem);
        
        appointmentTable.setComponentPopupMenu(popupMenu);
    }
    
    private void viewAppointmentDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            int appointmentId = (int) appointmentTable.getModel().getValueAt(modelRow, 0);
            // TODO: Load appointment details and show in a dialog
            JOptionPane.showMessageDialog(this, 
                "Appointment details will be shown here", 
                "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportToCSV() {
        // TODO: Implement CSV export
        JOptionPane.showMessageDialog(this, 
            "CSV export will be implemented here", 
            "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Custom cell renderer for appointment status
     */
    private static class StatusCellRenderer extends JLabel implements TableCellRenderer {
        public StatusCellRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Set default colors
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            // Set text and colors based on status
            if (value instanceof Status) {
                Status status = (Status) value;
                setText(status.toString());
                
                // Set background color based on status
                switch (status) {
                    case SCHEDULED:
                        setBackground(new Color(200, 255, 200)); // Light green
                        break;
                    case COMPLETED:
                        setBackground(new Color(200, 230, 255)); // Light blue
                        break;
                    case CANCELLED:
                        setBackground(new Color(255, 200, 200)); // Light red
                        break;
                    case NO_SHOW:
                        setBackground(new Color(255, 255, 150)); // Light yellow
                        break;
                    default:
                        setBackground(table.getBackground());
                }
                
                // Center the text
                setHorizontalAlignment(JLabel.CENTER);
            }
            
            return this;
        }
    }
}
