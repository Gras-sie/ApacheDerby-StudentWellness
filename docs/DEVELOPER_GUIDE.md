# Wellness Management System - Developer Guide

## Table of Contents
1. [Project Structure](#project-structure)
2. [Development Setup](#development-setup)
3. [Build System](#build-system)
4. [Database Schema](#database-schema)
5. [Backup and Restore System](#backup-and-restore-system)
6. [Configuration Management](#configuration-management)
7. [Testing](#testing)
8. [Deployment](#deployment)
9. [Code Style](#code-style)
10. [Troubleshooting](#troubleshooting)

## Project Structure

```
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
│       ├── config/          # Configuration files
│       └── icons/           # Application icons
├── test/                    # Test classes
│   └── java/com/wellness/
└── assembly/                # Assembly descriptors

docs/                        # Documentation
scripts/                     # Database and utility scripts
target/                      # Build output directory
data/                        # Database files
backups/                     # Backup files
```

## Development Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6.0 or higher
- Git
- IDE (IntelliJ IDEA)

### Setting Up the Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/wellness-management-system.git
   cd wellness-management-system
   ```

2. Import the project into your IDE as a Maven project

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn exec:java -Dexec.mainClass="com.wellness.WellnessApp"
   ```

## Build System

The project uses Maven for dependency management and building. Key plugins include:

- **maven-compiler-plugin**: Compiles Java sources
- **maven-shade-plugin**: Creates an executable JAR with dependencies
- **maven-assembly-plugin**: Creates distribution packages
- **maven-surefire-plugin**: Runs unit tests

### Common Maven Commands

- Build the project: `mvn clean install`
- Run tests: `mvn test`
- Create distribution package: `mvn package`
- Run the application: `mvn exec:java -Dexec.mainClass="com.wellness.WellnessApp"`

## Database Schema

The application uses an embedded Apache Derby database. The schema is defined in `scripts/init-db.sql`.

### Key Tables

- **users**: User accounts and authentication
- **counselors**: Counselor profiles and availability
- **appointments**: Scheduled counseling sessions
- **feedback**: Session feedback and ratings
- **audit_log**: System audit trail

### Database Connection

Database connection settings are configured in `config/application.properties`:

```properties
# Database configuration
db.url=jdbc:derby:data/wellnessDB;create=true
db.username=app
db.password=app
```

## Backup and Restore System

The backup system is implemented in `DatabaseBackupUtil` and provides:
- Full database backups
- Scheduled automatic backups
- Manual backup/restore functionality
- Backup retention policies

### Key Classes

- `DatabaseBackupUtil`: Core backup/restore functionality
- `BackupScheduler`: Manages scheduled backups
- `AppConfig`: Manages backup configuration

## Configuration Management

Application settings are managed by the `AppConfig` class, which loads properties from:
1. Default properties (built into the JAR)
2. User-specific properties (`config/application.properties`)

### Configuration Properties

```properties
# Database configuration
db.url=jdbc:derby:data/wellnessDB;create=true
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

## Testing

The project includes unit tests in the `src/test` directory. Tests are run using JUnit 5.

### Running Tests

```bash
mvn test
```

### Test Coverage

To generate a test coverage report:

```bash
mvn jacoco:report
```

The report will be available at `target/site/jacoco/index.html`.

## Deployment

### Creating a Release

1. Update the version in `pom.xml`
2. Update `CHANGELOG.md`
3. Create a Git tag: `git tag -a v1.0.0 -m "Version 1.0.0"`
4. Push the tag: `git push origin v1.0.0`
5. Create a GitHub release with the distribution package

### Distribution Packages

- **Windows**: `wellness-management-system-1.0.0.zip`
- **Linux/macOS**: `wellness-management-system-1.0.0.tar.gz`

## Code Style

The project follows the Google Java Style Guide with some modifications.

### Code Formatting

- Use 4 spaces for indentation
- Maximum line length: 100 characters
- Braces on the same line for control statements
- One variable per declaration

### Naming Conventions

- Classes: `PascalCase`
- Methods: `camelCase`
- Variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

## Troubleshooting

### Common Issues

#### Database Connection Issues
- Ensure the database is not locked by another process
- Verify the database directory has proper permissions
- Check the database logs in `data/derby.log`

#### Build Failures
- Ensure all dependencies are downloaded
- Check for version conflicts in `pom.xml`
- Clean the Maven cache if needed: `mvn dependency:purge-local-repository`

#### UI Issues
- Check the Java version (requires Java 11+)
- Verify all resource files are included in the classpath
- Check the application logs for errors

### Debugging

To enable debug logging, modify `logback.xml`:

```xml
<logger name="com.wellness" level="debug" />
```

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Create a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
