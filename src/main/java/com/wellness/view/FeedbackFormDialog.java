package com.wellness.view;

import com.wellness.controller.FeedbackController;
import com.wellness.dao.CounselorDAO;
import com.wellness.model.Counselor;
import com.wellness.model.Feedback;
import com.wellness.view.components.StarRatingComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A dialog for adding or editing feedback.
 */
public class FeedbackFormDialog extends JDialog {
    
    private final FeedbackController controller;
    private final Feedback feedback;
    private final boolean isEditMode;
    
    private JTextField studentNameField;
    private JComboBox<Counselor> counselorComboBox;
    private StarRatingComponent ratingComponent;
    private JTextArea commentsArea;
    private JCheckBox anonymousCheckBox;
    private JLabel charCountLabel;
    private static final int MAX_COMMENT_LENGTH = 2000;
    
    /**
     * Creates a new feedback form dialog.
     * 
     * @param parent The parent frame
     * @param controller The feedback controller
     * @param feedback The feedback to edit, or null to create new feedback
     */
    public FeedbackFormDialog(JFrame parent, FeedbackController controller, Feedback feedback) {
        super(parent, feedback == null ? "Add Feedback" : "Edit Feedback", true);
        this.controller = controller;
        this.feedback = feedback != null ? feedback : new Feedback();
        this.isEditMode = feedback != null;
        
        initializeUI();
        if (isEditMode) {
            populateForm();
        }
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Feedback Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Student Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Student Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        studentNameField = new JTextField(20);
        formPanel.add(studentNameField, gbc);
        
        // Counselor
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Counselor:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        counselorComboBox = new JComboBox<>();
        counselorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Counselor) {
                    Counselor c = (Counselor) value;
                    setText(c.getFirstName() + " " + c.getLastName());
                }
                return this;
            }
        });
        // Create a mock implementation of CounselorDAO
        CounselorDAO counselorDAO = new com.wellness.dao.CounselorDAO() {
            private final List<Counselor> mockCounselors = new ArrayList<>();
            
            // Initialize mock data
            {
                // Add some mock data
                Counselor c1 = new Counselor();
                c1.setId(1);
                c1.setFirstName("John");
                c1.setLastName("Doe");
                c1.setEmail("john.doe@example.com");
                c1.setSpecialization("Anxiety");
                mockCounselors.add(c1);
                
                Counselor c2 = new Counselor();
                c2.setId(2);
                c2.setFirstName("Jane");
                c2.setLastName("Smith");
                c2.setEmail("jane.smith@example.com");
                c2.setSpecialization("Depression");
                mockCounselors.add(c2);
            }
            
            @Override
            public Optional<Counselor> findById(Integer id) {
                return mockCounselors.stream().filter(c -> c.getId().equals(id)).findFirst();
            }
            
            @Override
            public List<Counselor> findAll() {
                return new ArrayList<>(mockCounselors);
            }
            
            @Override
            public Counselor save(Counselor entity) {
                if (entity.getId() == null) {
                    entity.setId(mockCounselors.size() + 1);
                }
                mockCounselors.removeIf(c -> c.getId().equals(entity.getId()));
                mockCounselors.add(entity);
                return entity;
            }
            
            @Override
            public boolean deleteById(Integer id) {
                return mockCounselors.removeIf(c -> c.getId().equals(id));
            }
            
            @Override
            public boolean delete(Counselor entity) {
                return entity != null && deleteById(entity.getId());
            }
            
            @Override
            public long deleteAll() {
                int count = mockCounselors.size();
                mockCounselors.clear();
                return count;
            }
            
            @Override
            public boolean existsById(Integer id) {
                return mockCounselors.stream().anyMatch(c -> c.getId().equals(id));
            }
            
            @Override
            public long count() {
                return mockCounselors.size();
            }
            
            @Override
            public Optional<Counselor> findByEmail(String email) {
                return mockCounselors.stream().filter(c -> email.equals(c.getEmail())).findFirst();
            }
            
            @Override
            public List<Counselor> findBySpecialization(String specialization) {
                return mockCounselors.stream()
                    .filter(c -> specialization.equals(c.getSpecialization()))
                    .collect(Collectors.toList());
            }
            
            @Override
            public List<Counselor> findActiveCounselors() {
                return new ArrayList<>(mockCounselors);
            }
            
            @Override
            public List<Counselor> findInactiveCounselors() {
                return new ArrayList<>();
            }
            
            @Override
            public List<Counselor> searchByName(String name) {
                String lowerName = name.toLowerCase();
                return mockCounselors.stream()
                    .filter(c -> c.getFirstName().toLowerCase().contains(lowerName) || 
                               c.getLastName().toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
            }
            
            @Override
            public boolean existsByEmail(String email) {
                return mockCounselors.stream().anyMatch(c -> email.equals(c.getEmail()));
            }
            
            @Override
            public long countActive() {
                return mockCounselors.size();
            }
            
            @Override
            public long countInactive() {
                return 0;
            }
            
            @Override
            public boolean deactivate(Integer id) {
                return mockCounselors.removeIf(c -> c.getId().equals(id));
            }
            
            @Override
            public boolean reactivate(Integer id) {
                return false; // Not needed for mock
            }
        };
        
        // Populate the combo box with active counselors
        try {
            List<Counselor> counselors = counselorDAO.findActiveCounselors();
            for (Counselor counselor : counselors) {
                counselorComboBox.addItem(counselor);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading counselors: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        formPanel.add(counselorComboBox, gbc);
        
        // Rating
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Rating:"), gbc);
        
        gbc.gridx = 1;
        ratingComponent = new StarRatingComponent();
        formPanel.add(ratingComponent, gbc);
        
        // Comments
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Comments:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsArea = new JTextArea(5, 30);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        
        // Add character counter
        charCountLabel = new JLabel(MAX_COMMENT_LENGTH + " characters remaining");
        charCountLabel.setForeground(Color.GRAY);
        charCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        commentsArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            
            private void updateCharCount() {
                int remaining = MAX_COMMENT_LENGTH - commentsArea.getText().length();
                charCountLabel.setText(remaining + " characters remaining");
                charCountLabel.setForeground(remaining < 0 ? Color.RED : Color.GRAY);
            }
        });
        
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        commentsPanel.add(charCountLabel, BorderLayout.SOUTH);
        
        formPanel.add(commentsPanel, gbc);
        
        // Anonymous checkbox
        gbc.gridx = 1;
        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        anonymousCheckBox = new JCheckBox("Submit feedback anonymously");
        formPanel.add(anonymousCheckBox, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JButton saveButton = new JButton(isEditMode ? "Update" : "Submit");
        saveButton.addActionListener(e -> saveFeedback());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set the main panel to the dialog
        add(mainPanel);
    }
    
    private void populateForm() {
        if (feedback != null) {
            studentNameField.setText("Student " + feedback.getStudentId());
            // Counselor selection is handled in the initialization
            ratingComponent.setRating(feedback.getRating());
            commentsArea.setText(feedback.getComments());
            anonymousCheckBox.setSelected(feedback.isAnonymous());
        }
    }
    
    private void saveFeedback() {
        try {
            // Validate form
            if (studentNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter student name.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Counselor selectedCounselor = (Counselor) counselorComboBox.getSelectedItem();
            if (selectedCounselor == null) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a counselor.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int rating = ratingComponent.getRating();
            if (rating < 1 || rating > 5) {
                JOptionPane.showMessageDialog(this, 
                    "Please provide a rating between 1 and 5 stars.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String comments = commentsArea.getText().trim();
            if (comments.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter your comments.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (comments.length() > MAX_COMMENT_LENGTH) {
                JOptionPane.showMessageDialog(this, 
                    "Comments cannot exceed " + MAX_COMMENT_LENGTH + " characters.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update feedback object
            feedback.setRating(rating);
            feedback.setComments(comments);
            feedback.setAnonymous(anonymousCheckBox.isSelected());
            
            // Set the selected counselor
            Counselor selected = (Counselor) counselorComboBox.getSelectedItem();
            if (selected != null) {
                feedback.setCounselorId(selected.getId());
            }
            
            // Set default values for required fields
            if (feedback.getStudentId() == null) {
                feedback.setStudentId(1); // Should be the logged-in student's ID
            }
            if (feedback.getAppointmentId() == null) {
                feedback.setAppointmentId(1); // Should be the current appointment ID if applicable
            }
            
            if (isEditMode) {
                if (controller.updateFeedback(feedback)) {
                    JOptionPane.showMessageDialog(this, 
                        "Feedback updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } else {
                // Let the controller handle setting the created date
                if (controller.submitFeedback(feedback)) {
                    JOptionPane.showMessageDialog(this, 
                        "Thank you for your feedback!", 
                        "Feedback Submitted", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "An error occurred: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the dialog and returns the created/updated feedback.
     * 
     * @return The feedback object if saved, null otherwise
     */
    public Feedback showDialog() {
        setVisible(true);
        return feedback;
    }
}
