package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;

/**
 * Base interface for all validators.
 * @param <T> The type of value to validate
 */
public interface Validator<T> {
    /**
     * Validates the given value.
     * @param value The value to validate
     * @param fieldName The name of the field being validated (used for error messages)
     * @throws ValidationException if validation fails
     */
    void validate(T value, String fieldName) throws ValidationException;

    /**
     * Validates the given value and returns a boolean result.
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    default boolean isValid(T value) {
        try {
            validate(value, "");
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
