package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validates time values and time ranges.
 */
public class TimeValidator implements Validator<String> {
    private final String timeFormat;
    private final boolean allowEmpty;
    private final String customMessage;
    private final LocalTime minTime;
    private final LocalTime maxTime;

    public TimeValidator() {
        this("HH:mm", null, null, null, false, null);
    }

    public TimeValidator(String timeFormat) {
        this(timeFormat, null, null, null, false, null);
    }

    public TimeValidator(String timeFormat, LocalTime minTime, LocalTime maxTime, 
                        String customMessage, boolean allowEmpty, String fieldName) {
        this.timeFormat = timeFormat != null ? timeFormat : "HH:mm";
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.customMessage = customMessage;
        this.allowEmpty = allowEmpty;
    }

    @Override
    public void validate(String timeStr, String fieldName) throws ValidationException {
        String field = fieldName != null && !fieldName.isEmpty() ? fieldName : "Time";
        
        if (timeStr == null || timeStr.trim().isEmpty()) {
            if (!allowEmpty) {
                throw new ValidationException(
                    field + " cannot be empty",
                    customMessage != null ? customMessage : "Please provide a time"
                );
            }
            return;
        }

        LocalTime time;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
            time = LocalTime.parse(timeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                "Invalid time format for " + field + ". Expected format: " + timeFormat,
                customMessage != null ? customMessage : "Please enter a valid time in format: " + timeFormat
            );
        }

        if (minTime != null && time.isBefore(minTime)) {
            throw new ValidationException(
                field + " cannot be before " + minTime.format(DateTimeFormatter.ofPattern(timeFormat)),
                customMessage != null ? customMessage : "Time cannot be before " + 
                    minTime.format(DateTimeFormatter.ofPattern(timeFormat))
            );
        }

        if (maxTime != null && time.isAfter(maxTime)) {
            throw new ValidationException(
                field + " cannot be after " + maxTime.format(DateTimeFormatter.ofPattern(timeFormat)),
                customMessage != null ? customMessage : "Time cannot be after " + 
                    maxTime.format(DateTimeFormatter.ofPattern(timeFormat))
            );
        }
    }

    public static boolean isValidTime(String timeStr, String format) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalTime.parse(timeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
