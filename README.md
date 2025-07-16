# Wellness Management System

A Java Swing desktop application for managing student wellness appointments, counselors, and feedback using Apache Derby (JavaDB) database.

## Features

- **Appointment Management**: Schedule, view, update, and cancel appointments
- **Counselor Directory**: Manage counselor information and availability
- **Feedback System**: Collect and review student feedback
- **Database Backup/Restore**: Built-in utilities for data backup and recovery
- **Responsive UI**: User-friendly interface with tabbed navigation

## Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher
- NetBeans IDE (recommended)

## Getting Started

### Database Setup

The application uses an embedded Apache Derby database that is automatically created when you first run the application. The database files are stored in the project directory under `wellnessDB`.

### Building the Project

1. Clone the repository
2. Navigate to the project directory
3. Build the project using Maven:

   ```bash
   mvn clean install
   ```

### Running the Application

After building, you can run the application using:

```bash
java -jar target/wellness-management-system-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/wellness/
│   │   ├── controller/      # Application controllers
│   │   ├── model/           # Data models
│   │   ├── view/            # UI components
│   │   ├── dao/             # Data Access Objects
│   │   ├── util/            # Utility classes
│   │   └── WellnessApp.java # Main application class
│   └── resources/           # Resource files
└── test/                    # Test classes
```

## Database Schema

### Counselors

- `id` (INT, PRIMARY KEY)
- `name` (VARCHAR(100))
- `specialization` (VARCHAR(100))
- `availability` (VARCHAR(100))
- `email` (VARCHAR(100), UNIQUE)
- `phone` (VARCHAR(20))

### Appointments

- `id` (INT, PRIMARY KEY)
- `student_name` (VARCHAR(100))
- `counselor_id` (INT, FOREIGN KEY)
- `appointment_date` (DATE)
- `appointment_time` (TIME)
- `status` (VARCHAR(20))
- `notes` (CLOB)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Feedback

- `id` (INT, PRIMARY KEY)
- `student_name` (VARCHAR(100))
- `counselor_id` (INT, FOREIGN KEY)
- `rating` (INT, 1-5)
- `comments` (CLOB)
- `feedback_date` (TIMESTAMP)

## Testing

Run the test suite using Maven:

```bash
mvn test
```

## Backup and Restore

The application includes utilities for backing up and restoring the database:

```java
// To create a backup
DatabaseManager.backupDatabase("/path/to/backup/directory");

// To restore from a backup
DatabaseManager.restoreDatabase("/path/to/backup/directory");
```

## License

This project is licensed under the MIT License.
