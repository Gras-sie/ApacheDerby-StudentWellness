package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Validates phone numbers in various international formats.
 */
public class PhoneValidator implements Validator<String> {
    // Matches international phone numbers with optional + and country code
    private static final String PHONE_REGEX = 
        "^\\+?[0-9]{1,3}?[-.\\s]?\\(?[0-9]{1,4}\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}$";
    private static final Pattern PATTERN = Pattern.compile(PHONE_REGEX);

    private final boolean allowEmpty;
    private final String customMessage;
    private final int minLength;
    private final int maxLength;

    public PhoneValidator() {
        this(false, 8, 15, null);
    }

    public PhoneValidator(boolean allowEmpty) {
        this(allowEmpty, 8, 15, null);
    }

    public PhoneValidator(boolean allowEmpty, int minLength, int maxLength, String customMessage) {
        this.allowEmpty = allowEmpty;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.customMessage = customMessage;
    }

    @Override
    public void validate(String phone, String fieldName) throws ValidationException {
        String field = fieldName != null && !fieldName.isEmpty() ? fieldName : "Phone number";
        
        if (phone == null || phone.trim().isEmpty()) {
            if (!allowEmpty) {
                throw new ValidationException(
                    field + " cannot be empty",
                    customMessage != null ? customMessage : "Please provide a phone number"
                );
            }
            return;
        }

        // Remove all non-digit characters for length check
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() < minLength) {
            throw new ValidationException(
                field + " is too short. Minimum length is " + minLength + " digits",
                customMessage != null ? customMessage : "Phone number is too short"
            );
        }

        if (digitsOnly.length() > maxLength) {
            throw new ValidationException(
                field + " is too long. Maximum length is " + maxLength + " digits",
                customMessage != null ? customMessage : "Phone number is too long"
            );
        }

        if (!PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                "Invalid phone number format: " + phone,
                customMessage != null ? customMessage : "Please enter a valid phone number"
            );
        }
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PATTERN.matcher(phone).matches();
    }
}
