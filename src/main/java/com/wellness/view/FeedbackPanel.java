package com.wellness.view;

import com.wellness.controller.FeedbackController;
import com.wellness.model.Feedback;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Main panel for managing feedback.
 * Displays a list of feedback with filtering and sorting options.
 */
public class FeedbackPanel extends JPanel {
    
    private final FeedbackController controller;
    private final JTable feedbackTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> filterComboBox;
    private final JComboBox<Integer> ratingComboBox;
    private final JTextField searchField;
    
    private static final String[] COLUMN_NAMES = {"ID", "Student", "Counselor", "Rating", "Comments", "Anonymous", "Date"};
    private static final int[] COLUMN_WIDTHS = {50, 100, 100, 80, 200, 80, 120};
    
    public FeedbackPanel(FeedbackController controller) {
        this.controller = controller;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create toolbar with filter controls
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // Filter by rating
        toolBar.add(new JLabel("Filter by rating:"));
        ratingComboBox = new JComboBox<>(new Integer[]{null, 1, 2, 3, 4, 5});
        ratingComboBox.addActionListener(e -> filterFeedback());
        toolBar.add(ratingComboBox);
        
        // Filter by type
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("Filter by:"));
        filterComboBox = new JComboBox<>(new String[]{"All", "This Week", "This Month", "Last 30 Days"});
        filterComboBox.addActionListener(e -> filterFeedback());
        toolBar.add(filterComboBox);
        
        // Search
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> filterFeedback());
        toolBar.add(searchField);
        
        // Buttons
        toolBar.add(Box.createHorizontalGlue());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshFeedback());
        toolBar.add(refreshButton);
        
        JButton addButton = new JButton("Add Feedback");
        addButton.addActionListener(e -> showAddFeedbackDialog());
        toolBar.add(addButton);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Create table
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        feedbackTable = new JTable(tableModel);
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            feedbackTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        
        // Add right-click menu
        JPopupMenu popupMenu = createTablePopupMenu();
        feedbackTable.setComponentPopupMenu(popupMenu);
        
        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load initial data
        refreshFeedback();
    }
    
    private JPopupMenu createTablePopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> viewSelectedFeedback());
        menu.add(viewItem);
        
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> editSelectedFeedback());
        menu.add(editItem);
        
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteSelectedFeedback());
        menu.add(deleteItem);
        
        return menu;
    }
    
    private void refreshFeedback() {
        List<Feedback> feedbackList = controller.getAllFeedback();
        updateTable(feedbackList);
    }
    
    private void filterFeedback() {
        String searchText = searchField.getText().trim();
        Integer ratingFilter = (Integer) ratingComboBox.getSelectedItem();
        
        List<Feedback> filteredList;
        
        if (!searchText.isEmpty()) {
            filteredList = controller.searchFeedback(searchText);
        } else if (ratingFilter != null) {
            filteredList = controller.getFeedbackByRating(ratingFilter, ratingFilter);
        } else {
            filteredList = controller.getAllFeedback();
        }
        
        updateTable(filteredList);
    }
    
    private void updateTable(List<Feedback> feedbackList) {
        tableModel.setRowCount(0); // Clear existing data
        
        if (feedbackList != null) {
            for (Feedback feedback : feedbackList) {
                Object[] row = {
                    feedback.getId(),
                    feedback.isAnonymous() ? "Anonymous" : "Student " + feedback.getStudentId(),
                    "Counselor " + feedback.getCounselorId(),
                    feedback.getRating() + " " + feedback.getStarRating(),
                    feedback.getComments().length() > 50 ? 
                        feedback.getComments().substring(0, 47) + "..." : 
                        feedback.getComments(),
                    feedback.isAnonymous() ? "Yes" : "No",
                    feedback.getCreatedDate()
                };
                tableModel.addRow(row);
            }
        }
    }
    
    private void viewSelectedFeedback() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Get the feedback ID from the selected row
            int feedbackId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Get all feedback and find the one with matching ID
            List<Feedback> allFeedback = controller.getAllFeedback();
            if (allFeedback != null) {
                Optional<Feedback> feedback = allFeedback.stream()
                    .filter(f -> f.getId() != null && f.getId() == feedbackId)
                    .findFirst();
                
                if (feedback.isPresent()) {
                    showFeedbackDetails(feedback.get());
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Feedback details not found.", 
                        "Not Found", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a feedback entry to view.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void editSelectedFeedback() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow >= 0) {
            int feedbackId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Find the feedback with the matching ID
            List<Feedback> allFeedback = controller.getAllFeedback();
            if (allFeedback != null) {
                Optional<Feedback> feedbackToEdit = allFeedback.stream()
                    .filter(f -> f.getId() != null && f.getId() == feedbackId)
                    .findFirst();
                
                if (feedbackToEdit.isPresent()) {
                    // TODO: Open the feedback form dialog in edit mode with the selected feedback
                    JOptionPane.showMessageDialog(this, 
                        "Editing feedback with ID: " + feedbackId, 
                        "Edit Feedback", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Could not find the selected feedback to edit.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a feedback entry to edit.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSelectedFeedback() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow >= 0) {
            if (controller.confirmDelete()) {
                int feedbackId = (int) tableModel.getValueAt(selectedRow, 0);
                if (controller.deleteFeedback(feedbackId)) {
                    refreshFeedback();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a feedback entry to delete.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showAddFeedbackDialog() {
        // Implementation for adding new feedback
        // This would open a dialog with a form to submit new feedback
        JOptionPane.showMessageDialog(this, 
            "Add Feedback form will be implemented here.", 
            "Add Feedback", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showFeedbackDetails(Feedback feedback) {
        // Show detailed view of feedback in a dialog
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setText(formatFeedbackDetails(feedback));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Feedback Details - ID: " + feedback.getId(), 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String formatFeedbackDetails(Feedback feedback) {
        return String.format(
            "Feedback ID: %d\n" +
            "Student ID: %s\n" +
            "Counselor ID: %d\n" +
            "Rating: %d %s\n" +
            "Anonymous: %s\n" +
            "Date: %s\n\n" +
            "Comments:\n%s",
            feedback.getId(),
            feedback.isAnonymous() ? "[Anonymous]" : feedback.getStudentId(),
            feedback.getCounselorId(),
            feedback.getRating(),
            feedback.getStarRating(),
            feedback.isAnonymous() ? "Yes" : "No",
            feedback.getCreatedDate(),
            feedback.getComments()
        );
    }
}
