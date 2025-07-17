package com.wellness.view;

import com.wellness.controller.CounselorController;
import com.wellness.model.Counselor;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for managing counselors in the Wellness Management System.
 * Provides a comprehensive interface for CRUD operations on counselors.
 */
public class CounselorPanel extends JPanel {
    private final CounselorController controller;
    private JTable counselorTable;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> specializationFilter;
    
    private static final String[] COLUMN_NAMES = {
        "ID", "Name", "Email", "Phone", "Specialization", "Status"
    };
    
    public CounselorPanel() {
        this.controller = CounselorController.getInstance();
        initializeUI();
        loadCounselors();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        addButton = new JButton("Add Counselor");
        addButton.addActionListener(e -> showAddEditDialog(null));
        
        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelectedCounselor());
        
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedCounselor());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCounselors());
        
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(refreshButton);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Add search field with document listener for real-time filtering
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterCounselors(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterCounselors(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterCounselors(); }
        });
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Specialization:"));
        filterPanel.add(specializationFilter);
        
        add(filterPanel, BorderLayout.CENTER);
        
        // Create counselor table
        counselorTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Enable row selection
        counselorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        counselorTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = counselorTable.getSelectedRow() >= 0;
            editButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
        });
        
        // Add double-click listener for editing
        counselorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCounselor();
                }
            }
        });
        
        // Set up status column renderer
        TableColumn statusColumn = counselorTable.getColumnModel().getColumn(5);
        statusColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof String) {
                    boolean active = "Active".equals(value);
                    if (active) {
                        c.setBackground(isSelected ? new Color(200, 255, 200) : new Color(230, 255, 230));
                    } else {
                        c.setBackground(isSelected ? new Color(255, 200, 200) : new Color(255, 230, 230));
                    }
                }
                return c;
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(counselorTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(scrollPane, BorderLayout.SOUTH);
        
        // Set minimum size for the panel
        setMinimumSize(new Dimension(800, 500));
    }
    
    private void loadCounselors() {
        try {
            List<Counselor> counselors = controller.getAllCounselors();
            DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0);
            
            for (Counselor counselor : counselors) {
                model.addRow(new Object[]{
                    counselor.getId(),
                    counselor.getFirstName() + " " + counselor.getLastName(),
                    counselor.getEmail(),
                    counselor.getPhoneNumber(),
                    counselor.getSpecialization(),
                    counselor.isActive() ? "Active" : "Inactive"
                });
            }
            
            counselorTable.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading counselors: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterCounselors() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilterValue = (String) statusFilter.getSelectedItem();
        String specializationFilterValue = (String) specializationFilter.getSelectedItem();
        
        List<Counselor> allCounselors = controller.getAllCounselors();
        List<Counselor> filtered = allCounselors;
        
        // Apply search filter
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                .filter(c -> 
                    (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(searchText)) ||
                    (c.getLastName() != null && c.getLastName().toLowerCase().contains(searchText)) ||
                    (c.getEmail() != null && c.getEmail().toLowerCase().contains(searchText)) ||
                    (c.getPhoneNumber() != null && c.getPhoneNumber().contains(searchText)) ||
                    (c.getSpecialization() != null && c.getSpecialization().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        }
        
        // Apply status filter
        if (!"All".equals(statusFilterValue)) {
            boolean active = "Active".equals(statusFilterValue);
            filtered = filtered.stream()
                .filter(c -> c.isActive() == active)
                .collect(Collectors.toList());
        }
        
        // Apply specialization filter
        if (!"All".equals(specializationFilterValue)) {
            filtered = filtered.stream()
                .filter(c -> c.getSpecialization() != null && 
                           c.getSpecialization().equals(specializationFilterValue))
                .collect(Collectors.toList());
        }
        
        // Update the table with filtered results
        DefaultTableModel model = (DefaultTableModel) counselorTable.getModel();
        model.setRowCount(0);
        
        for (Counselor counselor : filtered) {
            model.addRow(new Object[]{
                counselor.getId(),
                counselor.getFirstName() + " " + counselor.getLastName(),
                counselor.getEmail(),
                counselor.getPhoneNumber(),
                counselor.getSpecialization(),
                counselor.isActive() ? "Active" : "Inactive"
            });
        }
    }
    
    private void showAddEditDialog(Counselor counselor) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // First Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        JTextField firstNameField = new JTextField(20);
        if (counselor != null) {
            firstNameField.setText(counselor.getFirstName());
        }
        panel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        JTextField lastNameField = new JTextField(20);
        if (counselor != null) {
            lastNameField.setText(counselor.getLastName());
        }
        panel.add(lastNameField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        if (counselor != null) {
            emailField.setText(counselor.getEmail());
        }
        panel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(20);
        if (counselor != null) {
            phoneField.setText(counselor.getPhoneNumber());
        }
        panel.add(phoneField, gbc);
        
        // Specialization
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Specialization:"), gbc);
        
        gbc.gridx = 1;
        JTextField specializationField = new JTextField(20);
        if (counselor != null) {
            specializationField.setText(counselor.getSpecialization());
        }
        panel.add(specializationField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        if (counselor != null) {
            statusCombo.setSelectedItem(counselor.isActive() ? "Active" : "Inactive");
        }
        panel.add(statusCombo, gbc);
        
        // Show dialog
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            counselor == null ? "Add New Counselor" : "Edit Counselor",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (counselor == null) {
                    counselor = new Counselor();
                }
                
                counselor.setFirstName(firstNameField.getText().trim());
                counselor.setLastName(lastNameField.getText().trim());
                counselor.setEmail(emailField.getText().trim());
                counselor.setPhoneNumber(phoneField.getText().trim());
                counselor.setSpecialization(specializationField.getText().trim());
                counselor.setActive("Active".equals(statusCombo.getSelectedItem()));
                
                if (counselor.getId() == null) {
                    controller.createCounselor(counselor);
                    JOptionPane.showMessageDialog(this,
                        "Counselor added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    controller.updateCounselor(counselor);
                    JOptionPane.showMessageDialog(this,
                        "Counselor updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
                // Refresh the table
                loadCounselors();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving counselor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editSelectedCounselor() {
        int selectedRow = counselorTable.getSelectedRow();
        if (selectedRow >= 0) {
            int counselorId = (Integer) counselorTable.getValueAt(selectedRow, 0);
            try {
                controller.getCounselorById(counselorId).ifPresentOrElse(
                    this::showAddEditDialog,
                    () -> JOptionPane.showMessageDialog(this,
                        "Counselor not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE)
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading counselor details: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSelectedCounselor() {
        int selectedRow = counselorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a counselor to delete.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int counselorId = (Integer) counselorTable.getValueAt(selectedRow, 0);
        String counselorName = (String) counselorTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + counselorName + "?\nThis action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (controller.deleteCounselor(counselorId)) {
                    JOptionPane.showMessageDialog(this, 
                        "Counselor deleted successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadCounselors();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to delete counselor.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot delete counselor: " + e.getMessage(), 
                    "Deletion Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "An error occurred: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
