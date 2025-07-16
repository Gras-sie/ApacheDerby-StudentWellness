package com.wellness.view.components;

import com.wellness.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A custom date picker component that combines a text field with a calendar button.
 */
public class DatePicker extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField dateField;
    private final JButton calendarButton;
    private final SimpleDateFormat dateFormat;
    private Date selectedDate;

    /**
     * Creates a new DatePicker with the default date format (yyyy-MM-dd).
     */
    public DatePicker() {
        this("yyyy-MM-dd");
    }

    /**
     * Creates a new DatePicker with a custom date format.
     *
     * @param dateFormatPattern The date format pattern (e.g., "MM/dd/yyyy")
     */
    public DatePicker(String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
        this.selectedDate = new Date();
        
        setLayout(new BorderLayout(5, 0));
        
        // Date field
        dateField = new JTextField(dateFormat.format(selectedDate), 10);
        dateField.setFont(UIUtils.NORMAL_FONT);
        dateField.setBorder(UIUtils.createTextField().getBorder());
        
        // Add focus listener to validate date when focus is lost
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    Date parsedDate = dateFormat.parse(dateField.getText().trim());
                    setDate(parsedDate);
                } catch (Exception ex) {
                    // If parsing fails, revert to the last valid date
                    dateField.setText(dateFormat.format(selectedDate));
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        
        // Calendar button
        calendarButton = new JButton("📅");
        calendarButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        calendarButton.setFocusPainted(false);
        calendarButton.setBorderPainted(false);
        calendarButton.setContentAreaFilled(false);
        calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarButton.setToolTipText("Select date");
        
        // Add action listener to show calendar popup
        calendarButton.addActionListener(this::showCalendarPopup);
        
        // Add components to panel
        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
        
        // Set preferred size
        setPreferredSize(new Dimension(
            dateField.getPreferredSize().width + calendarButton.getPreferredSize().width + 5,
            dateField.getPreferredSize().height
        ));
    }
    
    /**
     * Shows the calendar popup for date selection.
     */
    private void showCalendarPopup(ActionEvent e) {
        // Create a dialog
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Create a calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        calendarPanel.setBackground(Color.WHITE);
        
        // Create month navigation
        JPanel navPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");
        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(UIUtils.TITLE_FONT);
        
        // Month navigation buttons styling
        for (JButton button : new JButton[]{prevButton, nextButton}) {
            button.setFont(UIUtils.NORMAL_FONT);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        // Calendar model
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate != null ? selectedDate : new Date());
        
        // Update month label
        updateMonthLabel(monthLabel, calendar);
        
        // Month navigation actions
        prevButton.addActionListener(evt -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthLabel(monthLabel, calendar);
            dialog.pack();
        });
        
        nextButton.addActionListener(evt -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthLabel(monthLabel, calendar);
            dialog.pack();
        });
        
        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextButton, BorderLayout.EAST);
        
        // Create days of week header
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        daysPanel.setBackground(Color.WHITE);
        
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setFont(UIUtils.SMALL_FONT);
            dayLabel.setForeground(Color.GRAY);
            daysPanel.add(dayLabel);
        }
        
        // Create calendar days
        JPanel daysGrid = createDaysGrid(calendar, dialog);
        
        // Add components to calendar panel
        calendarPanel.add(navPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);
        calendarPanel.add(daysGrid, BorderLayout.SOUTH);
        
        // Add to dialog and display
        dialog.add(calendarPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Creates the grid of days for the calendar.
     */
    private JPanel createDaysGrid(Calendar calendar, JDialog dialog) {
        JPanel daysGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        daysGrid.setBackground(Color.WHITE);
        
        // Create a copy of the calendar to avoid modifying the original
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Get the day of week of the first day of the month (0 = Sunday, 1 = Monday, etc.)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // Add empty cells for days before the first day of the month
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysGrid.add(new JLabel(""));
        }
        
        // Get the number of days in the month
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Create buttons for each day of the month
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(UIUtils.NORMAL_FONT);
            dayButton.setFocusPainted(false);
            dayButton.setBorderPainted(false);
            dayButton.setContentAreaFilled(false);
            dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Highlight the selected date
            if (selectedDate != null) {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                
                if (selectedCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    selectedCal.get(Calendar.DAY_OF_MONTH) == day) {
                    dayButton.setForeground(UIUtils.PRIMARY_COLOR);
                    dayButton.setFont(dayButton.getFont().deriveFont(Font.BOLD));
                }
            }
            
            // Add action listener to select the date
            final int selectedDay = day;
            dayButton.addActionListener(e -> {
                cal.set(Calendar.DAY_OF_MONTH, selectedDay);
                setDate(cal.getTime());
                dialog.dispose();
            });
            
            daysGrid.add(dayButton);
        }
        
        return daysGrid;
    }
    
    /**
     * Updates the month label with the current month and year.
     */
    private void updateMonthLabel(JLabel label, Calendar calendar) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");
        label.setText(monthFormat.format(calendar.getTime()));
    }
    
    /**
     * Gets the selected date.
     *
     * @return The selected date, or null if no date is selected
     */
    public Date getDate() {
        return selectedDate;
    }
    
    /**
     * Sets the selected date.
     *
     * @param date The date to select
     */
    public void setDate(Date date) {
        if (date == null) {
            this.selectedDate = null;
            this.dateField.setText("");
        } else {
            this.selectedDate = date;
            this.dateField.setText(dateFormat.format(date));
        }
        firePropertyChange("date", null, date);
    }
    
    /**
     * Gets the date as a formatted string.
     *
     * @return The formatted date string, or an empty string if no date is selected
     */
    public String getFormattedDate() {
        return selectedDate != null ? dateFormat.format(selectedDate) : "";
    }
    
    /**
     * Sets the date from a formatted string.
     *
     * @param dateString The date string to parse
     * @throws IllegalArgumentException If the date string is not in the expected format
     */
    public void setFormattedDate(String dateString) throws IllegalArgumentException {
        if (dateString == null || dateString.trim().isEmpty()) {
            setDate(null);
        } else {
            try {
                setDate(dateFormat.parse(dateString));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Expected: " + dateFormat.toPattern(), e);
            }
        }
    }
    
    /**
     * Sets whether the component is enabled.
     *
     * @param enabled true to enable the component, false to disable it
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dateField.setEnabled(enabled);
        calendarButton.setEnabled(enabled);
    }
    
    /**
     * Adds a property change listener for the "date" property.
     *
     * @param listener The property change listener to add
     */
    public void addDateChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener("date", listener);
    }
    
    /**
     * Removes a property change listener for the "date" property.
     *
     * @param listener The property change listener to remove
     */
    public void removeDateChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener("date", listener);
    }
}
