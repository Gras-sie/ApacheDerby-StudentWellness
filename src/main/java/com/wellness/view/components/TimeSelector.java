package com.wellness.view.components;

import com.wellness.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A custom time selector component that combines spinner controls for hours and minutes.
 */
public class TimeSelector extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JSpinner hourSpinner;
    private final JSpinner minuteSpinner;
    private final JComboBox<String> amPmComboBox;
    private final SimpleDateFormat timeFormat;
    private final boolean use24HourFormat;
    
    /**
     * Creates a new TimeSelector with 12-hour format.
     */
    public TimeSelector() {
        this(false);
    }
    
    /**
     * Creates a new TimeSelector with the specified time format.
     * 
     * @param use24HourFormat true to use 24-hour format, false for 12-hour format
     */
    public TimeSelector(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
        this.timeFormat = new SimpleDateFormat(use24HourFormat ? "HH:mm" : "h:mm a");
        
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // Create hour spinner
        SpinnerNumberModel hourModel = new SpinnerNumberModel(12, 1, use24HourFormat ? 23 : 12, 1);
        hourSpinner = new JSpinner(hourModel);
        styleSpinner(hourSpinner);
        
        // Create minute spinner
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 5);
        minuteSpinner = new JSpinner(minuteModel);
        styleSpinner(minuteSpinner);
        
        // Create AM/PM selector for 12-hour format
        if (!use24HourFormat) {
            amPmComboBox = new JComboBox<>(new String[]{"AM", "PM"});
            amPmComboBox.setFont(UIUtils.NORMAL_FONT);
            amPmComboBox.setMaximumRowCount(2);
            amPmComboBox.setFocusable(false);
            
            // Update hour spinner when AM/PM changes
            amPmComboBox.addActionListener(e -> firePropertyChange("time", null, getTime()));
        } else {
            amPmComboBox = null;
        }
        
        // Add change listeners to update time
        hourSpinner.addChangeListener(e -> firePropertyChange("time", null, getTime()));
        minuteSpinner.addChangeListener(e -> firePropertyChange("time", null, getTime()));
        
        // Add components to panel
        add(hourSpinner);
        add(new JLabel(":"));
        add(minuteSpinner);
        if (!use24HourFormat) {
            add(amPmComboBox);
        }
    }
    
    /**
     * Applies consistent styling to a spinner.
     */
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(UIUtils.NORMAL_FONT);
        spinner.setPreferredSize(new Dimension(60, 28));
        
        // Make the spinner editor use the same font
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(UIUtils.NORMAL_FONT);
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBorder(UIUtils.createTextField().getBorder());
        }
    }
    
    /**
     * Gets the selected time as a Date object.
     * 
     * @return The selected time, or null if no time is selected
     */
    public Date getTime() {
        try {
            Calendar cal = Calendar.getInstance();
            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            
            if (!use24HourFormat && amPmComboBox != null) {
                // Convert 12-hour to 24-hour format
                boolean isPM = "PM".equals(amPmComboBox.getSelectedItem());
                if (hour == 12) {
                    hour = isPM ? 12 : 0;  // 12 AM = 00:00, 12 PM = 12:00
                } else if (isPM) {
                    hour += 12;  // 1-11 PM = 13-23:00
                }
            }
            
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Sets the selected time.
     * 
     * @param time The time to select, or null to clear the selection
     */
    public void setTime(Date time) {
        if (time == null) {
            hourSpinner.setValue(12);
            minuteSpinner.setValue(0);
            if (amPmComboBox != null) {
                amPmComboBox.setSelectedItem("AM");
            }
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        if (use24HourFormat) {
            hourSpinner.setValue(hour);
        } else {
            // Convert 24-hour to 12-hour format
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;  // 0:00 becomes 12:00 AM
            
            hourSpinner.setValue(hour12);
            if (amPmComboBox != null) {
                amPmComboBox.setSelectedItem(hour < 12 ? "AM" : "PM");
            }
        }
        
        minuteSpinner.setValue((minute / 5) * 5); // Round to nearest 5 minutes
    }
    
    /**
     * Gets the time as a formatted string.
     * 
     * @return The formatted time string, or an empty string if no time is selected
     */
    public String getFormattedTime() {
        Date time = getTime();
        return time != null ? timeFormat.format(time) : "";
    }
    
    /**
     * Sets the time from a formatted string.
     * 
     * @param timeString The time string to parse
     * @throws IllegalArgumentException If the time string is not in the expected format
     */
    public void setFormattedTime(String timeString) throws IllegalArgumentException {
        if (timeString == null || timeString.trim().isEmpty()) {
            setTime(null);
            return;
        }
        
        try {
            // Try to parse the time string
            Date time = timeFormat.parse(timeString);
            setTime(time);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format. Expected: " + 
                (use24HourFormat ? "HH:mm" : "h:mm a"), e);
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
        hourSpinner.setEnabled(enabled);
        minuteSpinner.setEnabled(enabled);
        if (amPmComboBox != null) {
            amPmComboBox.setEnabled(enabled);
        }
    }
    
    /**
     * Adds a property change listener for the "time" property.
     * 
     * @param listener The property change listener to add
     */
    public void addTimeChangeListener(ActionListener listener) {
        addPropertyChangeListener("time", evt -> {
            if (evt.getPropertyName().equals("time")) {
                listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "timeChanged"));
            }
        });
    }
    
    /**
     * Removes a property change listener for the "time" property.
     * 
     * @param listener The property change listener to remove
     */
    public void removeTimeChangeListener(ActionListener listener) {
        // Note: This is a simplified implementation. For a complete implementation,
        // you would need to track the listeners and remove the specific one.
        removePropertyChangeListener("time", null);
    }
}
