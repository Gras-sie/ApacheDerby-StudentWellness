package com.wellness.exception;

/**
 * Exception class for service layer exceptions.
 * Wraps lower-level exceptions and provides user-friendly error messages.
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;
    private final String userMessage;

    /**
     * Enumeration of error codes for different types of service exceptions.
     */
    public enum ErrorCode {
        VALIDATION_ERROR,
        NOT_FOUND,
        DUPLICATE_ENTRY,
        DATABASE_ERROR,
        UNAUTHORIZED,
        CONFLICT,
        INVALID_INPUT,
        UNEXPECTED_ERROR
    }

    /**
     * Constructs a new ServiceException with the specified detail message and error code.
     *
     * @param message the detail message
     * @param errorCode the error code
     */
    public ServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    /**
     * Constructs a new ServiceException with the specified detail message, cause, and error code.
     *
     * @param message the detail message
     * @param cause the cause
     * @param errorCode the error code
     */
    public ServiceException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    /**
     * Constructs a new ServiceException with the specified detail message, user message, cause, and error code.
     *
     * @param message the detail message
     * @param userMessage the user-friendly message
     * @param cause the cause
     * @param errorCode the error code
     */
    public ServiceException(String message, String userMessage, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    /**
     * Returns the error code associated with this exception.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a user-friendly error message.
     *
     * @return the user-friendly message
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * Factory method for creating a validation error exception.
     *
     * @param message the validation error message
     * @return a new ServiceException with VALIDATION_ERROR code
     */
    public static ServiceException validationError(String message) {
        return new ServiceException(message, ErrorCode.VALIDATION_ERROR);
    }

    /**
     * Factory method for creating a not found exception.
     *
     * @param entityName the name of the entity that was not found
     * @param id the ID that was not found
     * @return a new ServiceException with NOT_FOUND code
     */
    public static ServiceException notFound(String entityName, Object id) {
        return new ServiceException(
            String.format("%s with ID %s not found", entityName, id),
            ErrorCode.NOT_FOUND
        );
    }

    /**
     * Factory method for creating a database error exception.
     *
     * @param message the error message
     * @param cause the cause of the error
     * @return a new ServiceException with DATABASE_ERROR code
     */
    public static ServiceException databaseError(String message, Throwable cause) {
        return new ServiceException(
            "Database error: " + message,
            "A database error occurred. Please try again later.",
            cause,
            ErrorCode.DATABASE_ERROR
        );
    }

    /**
     * Factory method for creating a conflict exception.
     *
     * @param message the conflict message
     * @return a new ServiceException with CONFLICT code
     */
    public static ServiceException conflict(String message) {
        return new ServiceException(message, ErrorCode.CONFLICT);
    }

    /**
     * Factory method for creating an invalid input exception.
     *
     * @param message the validation message
     * @return a new ServiceException with INVALID_INPUT code
     */
    public static ServiceException invalidInput(String message) {
        return new ServiceException(message, ErrorCode.INVALID_INPUT);
    }
}
