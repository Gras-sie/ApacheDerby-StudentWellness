package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Validates dates and date ranges.
 */
public class DateValidator implements Validator<String> {
    private final String dateFormat;
    private final boolean allowEmpty;
    private final String customMessage;
    private final LocalDate minDate;
    private final LocalDate maxDate;

    public DateValidator() {
        this("yyyy-MM-dd", null, null, null, false, null);
    }

    public DateValidator(String dateFormat) {
        this(dateFormat, null, null, null, false, null);
    }

    public DateValidator(String dateFormat, LocalDate minDate, LocalDate maxDate, 
                        String customMessage, boolean allowEmpty, String fieldName) {
        this.dateFormat = dateFormat != null ? dateFormat : "yyyy-MM-dd";
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.customMessage = customMessage;
        this.allowEmpty = allowEmpty;
    }

    @Override
    public void validate(String dateStr, String fieldName) throws ValidationException {
        String field = fieldName != null && !fieldName.isEmpty() ? fieldName : "Date";
        
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (!allowEmpty) {
                throw new ValidationException(
                    field + " cannot be empty",
                    customMessage != null ? customMessage : "Please provide a date"
                );
            }
            return;
        }

        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(dateFormat)
                .withResolverStyle(ResolverStyle.STRICT);
            date = LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                "Invalid date format for " + field + ". Expected format: " + dateFormat,
                customMessage != null ? customMessage : "Please enter a valid date in format: " + dateFormat
            );
        }

        if (minDate != null && date.isBefore(minDate)) {
            throw new ValidationException(
                field + " cannot be before " + minDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                customMessage != null ? customMessage : "Date cannot be before " + minDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
        }

        if (maxDate != null && date.isAfter(maxDate)) {
            throw new ValidationException(
                field + " cannot be after " + maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                customMessage != null ? customMessage : "Date cannot be after " + maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
        }
    }

    public static boolean isValidDate(String dateStr, String format) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(format)
                .withResolverStyle(ResolverStyle.STRICT);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
