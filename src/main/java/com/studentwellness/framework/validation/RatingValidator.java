package com.studentwellness.framework.validation;

import com.studentwellness.framework.exception.ValidationException;

/**
 * Validates rating values, typically on a 1-5 scale.
 */
public class RatingValidator implements Validator<Integer> {
    private final int minRating;
    private final int maxRating;
    private final boolean allowNull;
    private final String customMessage;

    public RatingValidator() {
        this(1, 5, false, null);
    }

    public RatingValidator(int minRating, int maxRating) {
        this(minRating, maxRating, false, null);
    }

    public RatingValidator(int minRating, int maxRating, boolean allowNull, String customMessage) {
        if (minRating >= maxRating) {
            throw new IllegalArgumentException("minRating must be less than maxRating");
        }
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.allowNull = allowNull;
        this.customMessage = customMessage;
    }

    @Override
    public void validate(Integer rating, String fieldName) throws ValidationException {
        String field = fieldName != null && !fieldName.isEmpty() ? fieldName : "Rating";
        
        if (rating == null) {
            if (!allowNull) {
                throw new ValidationException(
                    field + " cannot be null",
                    customMessage != null ? customMessage : "Please provide a rating"
                );
            }
            return;
        }

        if (rating < minRating || rating > maxRating) {
            throw new ValidationException(
                field + " must be between " + minRating + " and " + maxRating,
                customMessage != null ? customMessage : 
                    "Please provide a rating between " + minRating + " and " + maxRating
            );
        }
    }

    public static boolean isValidRating(Integer rating, int min, int max) {
        return rating != null && rating >= min && rating <= max;
    }
}
