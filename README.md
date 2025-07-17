# Student Wellness Management System

A comprehensive Java desktop application developed as a university project, implementing a robust student wellness management system using Java Swing for the frontend and Apache Derby (JavaDB) for the database. This application serves as both an academic project and a practical tool for managing student wellness services.

## 🎓 University Project Context

This application was developed as part of the PRG 381 course at Belgium Campus iTversity. It demonstrates proficiency in:
- Object-Oriented Programming (OOP) principles
- Database design and implementation with Apache Derby
- Desktop application development using Java Swing
- Software development best practices
- Version control with Git

## 🛠️ Technology Stack

### Core Technologies
- **Java 11+**: Primary programming language
- **Java Swing**: For the desktop GUI
- **Apache Derby (JavaDB)**: Embedded relational database
- **Maven**: Dependency management and build automation
- **JDBC**: Database connectivity

### Development Environment
- **IDE**: NetBeans IDE (Recommended)
- **Build Tool**: Apache Maven 3.6.0+
- **Version Control**: Git with GitHub

## 🚀 Key Features

### Core Requirements (University Project)
- **Appointment Management**
  - Schedule, view, update, and cancel appointments
  - Track appointment status (Scheduled, Completed, Cancelled, No-Show)
  - Filter and search appointments by date, student, or counselor

- **Counselor Management**
  - Maintain counselor profiles with contact information
  - Track counselor availability and specializations
  - View counselor schedules and appointment history

- **Student Management**
  - Maintain student records
  - Track appointment history and wellness notes
  - Generate student wellness reports

### Enhanced Features (Added Experience)
The following features were implemented beyond the core requirements to enhance the application and provide additional learning opportunities:

1. **Database Backup & Recovery**
   - Automated scheduled backups
   - Manual backup/restore functionality
   - Backup versioning and retention policies

2. **Advanced UI/UX**
   - Responsive layout with modern Swing components
   - Form validation and error handling
   - Context menus and keyboard shortcuts
   - Export functionality for reports

3. **System Administration**
   - Configuration management
   - System health monitoring
   - Logging and error tracking

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/wellness/
│   │   ├── controller/      # Application controllers (MVC)
│   │   ├── model/           # Data models and business logic
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business services
│   │   ├── util/            # Utility classes
│   │   └── view/            # UI components
│   └── resources/           # Configuration files and resources
│       ├── db/              # Database scripts
│       └── config/          # Application configuration
└── test/                    # Unit and integration tests
```

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6.0 or higher
- NetBeans IDE (recommended) or any Java IDE of your choice

### Database Configuration

The application uses an embedded Apache Derby database that is automatically initialized on first run. The database files are stored in the project directory under `wellnessDB`.

### Building the Project

#### Using Maven (Command Line)
```bash
# Clone the repository
git clone https://github.com/yourusername/student-wellness-system.git
cd student-wellness-system

# Build the project
mvn clean install

# Run the application
java -jar target/wellness-management-system-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### Using NetBeans IDE
1. Open the project in NetBeans
2. Right-click on the project and select "Clean and Build"
3. Right-click and select "Run" to start the application

### Testing the Build Scripts

For those interested in the build automation aspects, the project includes both `build.bat` and `build.sh` scripts that set up a Maven environment and build the project. These scripts are provided as a learning resource and can be tested as follows:

#### Windows (build.bat)
```cmd
build.bat
```

#### Linux/Mac (build.sh)
```bash
chmod +x build.sh
./build.sh
```

These scripts will:
1. Check for Java and Maven installations
2. Set up environment variables if needed
3. Execute a clean Maven build
4. Package the application into an executable JAR

## 📚 Documentation

### User Guide
See [USER_GUIDE.md](USER_GUIDE.md) for detailed instructions on using the application.

### Developer Guide
See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for information about the codebase, architecture, and contribution guidelines.

### API Documentation
JavaDoc documentation can be generated using:
```bash
mvn javadoc:javadoc
```

## 🧪 Testing the Application

The application includes unit tests that can be run using:
```bash
mvn test
```

## 📦 Deployment

The application is packaged as an executable JAR with all dependencies included. The build process generates:
- `target/wellness-management-system-1.0-SNAPSHOT.jar` (main JAR)
- `target/wellness-management-system-1.0-SNAPSHOT-jar-with-dependencies.jar` (standalone executable)

## 🔄 Version Control

This project uses Git for version control. The main branch contains the stable release, while development happens in feature branches.

## 📝 Note on Experimental Features

Several features in this project were implemented as learning exercises and go beyond the original university requirements. These include:
- Advanced database management utilities
- Automated backup systems
- Enhanced UI components
- Build automation scripts

These features were added to gain practical experience with professional software development practices and are fully functional, though they may contain experimental code that could be further refined in a production environment.
## 📂 Directory Structure Details

### Source Code Organization

```
com.wellness/
├── controller/      # Application controllers (MVC pattern)
│   ├── AppointmentController.java
│   ├── CounselorController.java
│   └── StudentController.java
├── model/           # Data models and business entities
│   ├── Appointment.java
│   ├── Counselor.java
│   ├── Student.java
│   └── Feedback.java
├── repository/      # Data access layer
│   ├── AppointmentRepository.java
│   └── CounselorRepository.java
├── service/         # Business logic layer
│   ├── AppointmentService.java
│   └── CounselorService.java
├── util/            # Utility classes
│   ├── DatabaseBackupUtil.java
│   └── ConfigManager.java
└── view/            # UI components
    ├── dialogs/     # Dialog windows
    ├── panels/      # Main application panels
    └── MainFrame.java
```

### Resource Files

```
resources/
├── db/
│   ├── schema.sql      # Database schema
│   └── test-data.sql   # Sample data
├── config/
│   └── app.properties  # Application configuration
└── icons/              # Application icons
```
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

The application includes built-in backup and restore functionality to protect your data.

### Manual Backup
1. Go to `File > Backup Now` to create an immediate backup
2. Backups are stored in the `backups` directory in the application's home folder
3. Each backup is a timestamped zip file containing the database and configuration

### Automatic Backups
- Automatic backups are enabled by default
- Backups are created daily at midnight
- The last 30 days of backups are retained
- Backup settings can be configured in `File > Backup Settings`

### Restoring from Backup
1. Go to `File > Restore from Backup...`
2. Select a backup file from the file chooser
3. The application will restart automatically after restoration

## Deployment

### Prerequisites
- Java 11 or higher JRE/JDK
- At least 500MB of free disk space
- Minimum 2GB RAM recommended

### Windows Installation
1. Download the latest release zip file
2. Extract to a directory of your choice (e.g., `C:\Program Files\WellnessManagementSystem`)
3. Run `start.bat` to launch the application
4. (Optional) Create a desktop shortcut to `start.bat`

### Linux/macOS Installation
1. Download the latest release zip file
2. Extract to a directory of your choice (e.g., `/opt/wellness-management-system`)
3. Make the startup script executable:
   ```bash
   chmod +x /opt/wellness-management-system/start.sh
   ```
4. Run the application:
   ```bash
   /opt/wellness-management-system/start.sh
   ```
   
### Configuration
Application settings can be modified in `config/application.properties`:

```properties
# Database configuration
db.url=jdbc:derby:wellnessDB;create=true
db.username=app
db.password=app

# Backup settings
backup.enabled=true
backup.interval.hours=24
backup.retention.days=30

# UI Settings
ui.theme=light
ui.font.size=14
```

## Troubleshooting

### Common Issues

#### Database Connection Errors
- Ensure no other instance of the application is running
- Verify write permissions in the application directory
- Check disk space availability

#### Backup/Restore Failures
- Verify sufficient disk space for backup operations
- Ensure the application has write permissions in the backup directory
- Check application logs for detailed error messages

### Logs
Application logs are stored in:
- Windows: `%APPDATA%\WellnessManagementSystem\logs\application.log`
- Linux/macOS: `~/.wellness/management/logs/application.log`

## Support
For assistance, please contact marius-jnr@outlook.com

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

The application includes utilities for backing up and restoring the database:

```java
// To create a backup
DatabaseManager.backupDatabase("/path/to/backup/directory");

// To restore from a backup
DatabaseManager.restoreDatabase("/path/to/backup/directory");
```

## License

This project is licensed under the MIT License.
