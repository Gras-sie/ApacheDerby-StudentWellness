package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Validates email addresses according to RFC 5322 standard.
 */
public class EmailValidator implements Validator<String> {
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private final boolean allowEmpty;
    private final String customMessage;

    public EmailValidator() {
        this(false, null);
    }

    public EmailValidator(boolean allowEmpty) {
        this(allowEmpty, null);
    }

    public EmailValidator(boolean allowEmpty, String customMessage) {
        this.allowEmpty = allowEmpty;
        this.customMessage = customMessage;
    }

    @Override
    public void validate(String email, String fieldName) throws ValidationException {
        String field = fieldName != null && !fieldName.isEmpty() ? fieldName : "Email";
        
        if (email == null || email.trim().isEmpty()) {
            if (!allowEmpty) {
                throw new ValidationException(
                    field + " cannot be empty",
                    customMessage != null ? customMessage : "Please provide an email address"
                );
            }
            return;
        }

        if (!PATTERN.matcher(email).matches()) {
            throw new ValidationException(
                "Invalid email format: " + email,
                customMessage != null ? customMessage : "Please enter a valid email address"
            );
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && PATTERN.matcher(email).matches();
    }
}
