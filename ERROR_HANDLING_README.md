# Error Handling and Validation Framework

This document provides an overview of the error handling and validation framework implemented for the Student Wellness application.

## Table of Contents

1. [Exception Hierarchy](#exception-hierarchy)
2. [Validation Framework](#validation-framework)
3. [Error Display Components](#error-display-components)
4. [Global Exception Handling](#global-exception-handling)
5. [Usage Examples](#usage-examples)
6. [Best Practices](#best-practices)

## Exception Hierarchy

The framework provides a set of custom exceptions that extend `AppException`:

- `DatabaseException`: For database-related errors
- `ValidationException`: For input validation errors (supports field-level errors)
- `BusinessLogicException`: For business rule violations
- `ConnectionException`: For network/database connectivity issues

## Validation Framework

### Built-in Validators

1. **EmailValidator**: Validates email addresses
2. **PhoneValidator**: Validates phone numbers with international support
3. **DateValidator**: Validates dates and date ranges
4. **TimeValidator**: Validates time values and time slots
5. **RatingValidator**: Validates ratings (e.g., 1-5 scale)

### Validation Utils

The `ValidationUtils` class provides helper methods for form validation:

- `formValidator()`: Creates a form validator
- `bindValidator()`: Binds a validator to a text field
- `validateForm()`: Validates all fields in a form
- `required()`: Creates a required field validator

## Error Display Components

### ErrorDialog

A dialog for displaying error messages with details:

```java
ErrorDialog dialog = new ErrorDialog(parentComponent, messages);
dialog.showError(exception);
```

### ValidationPanel

Displays validation errors for form fields:

```java
ValidationPanel validationPanel = new ValidationPanel();
// Add to your form...

// Set field validation
validationPanel.setFieldValidation("username", "Username is required");
validationPanel.setFieldValidation("email", "Invalid email format");

// Clear all validations
validationPanel.clearAll();
```

### StatusBar

Displays application status and connection state:

```java
StatusBar statusBar = new StatusBar();
// Add to your window...

// Update status
statusBar.setStatus("Operation completed");
statusBar.setSuccess("Data saved successfully");
statusBar.setError("Failed to save data");
statusBar.setWarning("Some fields need attention");

// Update connection status
statusBar.setConnectionStatus(true); // Connected
statusBar.setConnectionStatus(false); // Disconnected
statusBar.setConnecting(); // Connecting...
```

### ToastNotification

Displays temporary notifications:

```java
// Show different types of toasts
ToastNotification.success("Operation completed successfully");
ToastNotification.error("An error occurred");
ToastNotification.warning("Please check your input");
ToastNotification.info("New message received");

// Custom toast
ToastNotification.show("Custom message", 5000, new Color(100, 100, 200));
```

## Global Exception Handling

The `GlobalExceptionHandler` catches all uncaught exceptions:

```java
// Initialize in your application startup
GlobalExceptionHandler.initialize(
    mainFrame, // Parent component
    ResourceBundle.getBundle("messages") // Optional: for localized messages
);

// Schedule a task with retry logic
GlobalExceptionHandler.withRetry(
    () -> {
        // Your code that might fail
        return someResult;
    },
    3, // Max retries
    1000 // Delay between retries in ms
).thenAccept(result -> {
    // Handle success
}).exceptionally(throwable -> {
    // Handle failure after all retries
    return null;
});
```

## Usage Examples

### Form Validation Example

```java
// Create a form validator
ValidationUtils.FormValidator<MyFormData> validator = ValidationUtils.formValidator()
    .withField("username", 
        ValidationUtils.required().withMessage("Username is required"),
        form -> form.getUsername())
    .withField("email",
        new EmailValidator()
            .withCustomMessage("Please enter a valid email address"),
        form -> form.getEmail())
    .withField("phone",
        new PhoneValidator()
            .withMinLength(10)
            .withMaxLength(15),
        form -> form.getPhone())
    .withValidationPanel(validationPanel);

// Validate the form
boolean isValid = validator.validate(formData);
if (isValid) {
    // Proceed with form submission
}
```

### Real-time Field Validation

```java
// Bind a validator to a text field
JTextField emailField = new JTextField();
JLabel errorLabel = new JLabel();

emailField.getDocument().addDocumentListener((SimpleDocumentListener) e -> 
    ValidationUtils.bindValidator(
        emailField,
        new EmailValidator(),
        errorLabel,
        "Email Address"
    ).run()
);
```

## Best Practices

1. **Use Specific Exceptions**: Throw the most specific exception type that fits the error condition.
2. **Provide User-Friendly Messages**: Always include user-friendly messages in exceptions.
3. **Log Technical Details**: Log technical details for debugging while showing user-friendly messages.
4. **Handle Exceptions at the Right Level**: Catch exceptions at the level where you can handle them meaningfully.
5. **Use Validation for Input**: Validate all user input as early as possible.
6. **Provide Clear Feedback**: Use the appropriate UI components to provide clear feedback to users.
7. **Implement Retry Logic**: For transient errors, implement retry logic using `GlobalExceptionHandler.withRetry()`.
8. **Internationalization**: Use resource bundles for all user-facing messages.

## Dependencies

- Java 8 or higher
- Swing (included in Java SE)

## License

This framework is part of the Student Wellness application and is licensed under the [MIT License](LICENSE).
