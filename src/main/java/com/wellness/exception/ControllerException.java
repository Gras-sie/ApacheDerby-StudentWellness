package com.wellness.exception;

/**
 * Exception class for controller layer exceptions.
 * Wraps service layer exceptions and provides user-friendly error messages.
 */
public class ControllerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    
    /**
     * Constructs a new ControllerException with the specified detail message and error code.
     *
     * @param message the detail message
     * @param errorCode the error code
     */
    public ControllerException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new ControllerException with the specified detail message, error code, and cause.
     *
     * @param message the detail message
     * @param errorCode the error code
     * @param cause the cause
     */
    public ControllerException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Factory method for creating a not found exception.
     *
     * @param entityName the name of the entity that was not found
     * @param id the ID that was not found
     * @return a new ControllerException with NOT_FOUND code
     */
    public static ControllerException notFound(String entityName, Object id) {
        return new ControllerException(
            String.format("%s with ID %s not found", entityName, id),
            "NOT_FOUND"
        );
    }
    
    /**
     * Factory method for creating a validation error exception.
     *
     * @param message the validation error message
     * @return a new ControllerException with VALIDATION_ERROR code
     */
    public static ControllerException validationError(String message) {
        return new ControllerException(message, "VALIDATION_ERROR");
    }
    
    /**
     * Factory method for creating an invalid input exception.
     *
     * @param message the error message
     * @return a new ControllerException with INVALID_INPUT code
     */
    public static ControllerException invalidInput(String message) {
        return new ControllerException(message, "INVALID_INPUT");
    }
    
    /**
     * Factory method for creating a conflict exception.
     *
     * @param message the conflict message
     * @return a new ControllerException with CONFLICT code
     */
    public static ControllerException conflict(String message) {
        return new ControllerException(message, "CONFLICT");
    }
}
