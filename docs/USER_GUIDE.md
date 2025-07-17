# Wellness Management System - User Guide

## Table of Contents
1. [Installation](#installation)
2. [Getting Started](#getting-started)
3. [User Interface Overview](#user-interface-overview)
4. [Managing Appointments](#managing-appointments)
5. [Counselor Directory](#counselor-directory)
6. [Feedback System](#feedback-system)
7. [Backup and Restore](#backup-and-restore)
8. [Troubleshooting](#troubleshooting)

## Installation

### System Requirements
- Java 11 or higher
- Minimum 2GB RAM
- 500MB free disk space
- Screen resolution: 1024x768 or higher

### Windows Installation
1. Download the latest release ZIP file
2. Extract the contents to a folder of your choice (e.g., `C:\Program Files\WellnessManagementSystem`)
3. Double-click `start.bat` to launch the application
4. (Optional) Create a desktop shortcut to `start.bat`

### Linux/macOS Installation
1. Download the latest release TAR.GZ file
2. Extract the archive:
   ```bash
   tar -xzf wellness-management-system-1.0.0.tar.gz
   ```
3. Make the startup script executable:
   ```bash
   chmod +x wellness-management-system-1.0.0/start.sh
   ```
4. Run the application:
   ```bash
   ./wellness-management-system-1.0.0/start.sh
   ```

## Getting Started

### First Run
1. The application will create a default database in the `data` directory
2. Default login credentials:
   - Username: `admin`
   - Password: `admin123`

### Changing Your Password
1. Log in with your credentials
2. Go to `Settings > Change Password`
3. Enter your current password and set a new one
4. Click "Save Changes"

## User Interface Overview

### Main Dashboard
- **Appointments**: View and manage your scheduled appointments
- **Counselors**: Browse available counselors and their availability
- **Feedback**: Submit and view feedback about counseling sessions
- **Reports**: Generate reports on appointments and feedback
- **Settings**: Configure application preferences

### Navigation
- Use the sidebar to switch between different sections
- The status bar at the bottom shows important messages and system status
- The menu bar provides access to additional functions and settings

## Managing Appointments

### Scheduling an Appointment
1. Go to `Appointments > Schedule New`
2. Select a counselor from the list
3. Choose an available date and time
4. Add any notes or special requests
5. Click "Schedule Appointment"

### Viewing Appointments
- Upcoming appointments are shown on the dashboard
- Click on an appointment to view details
- Use the calendar view to see availability

### Canceling an Appointment
1. Go to `Appointments`
2. Select the appointment you want to cancel
3. Click "Cancel Appointment"
4. Confirm the cancellation

## Counselor Directory

### Viewing Counselor Profiles
1. Go to `Counselors`
2. Browse the list of available counselors
3. Click on a counselor to view their profile, including:
   - Specialization
   - Bio
   - Availability
   - Contact information

### Filtering Counselors
- Use the search bar to find counselors by name or specialization
- Filter by availability using the calendar
- Sort by rating or name

## Feedback System

### Submitting Feedback
1. Go to `Feedback > Submit Feedback`
2. Select the appointment from the list
3. Rate your experience (1-5 stars)
4. Add any comments or suggestions
5. Click "Submit Feedback"

### Viewing Feedback
- Go to `Feedback > View All`
- Browse through feedback entries
- Use filters to find specific feedback

## Backup and Restore

### Creating a Backup
1. Go to `File > Backup Now`
2. Wait for the backup to complete
3. A confirmation message will appear when done

### Restoring from Backup
1. Go to `File > Restore from Backup...`
2. Select a backup file from the file chooser
3. Confirm that you want to restore
4. The application will restart automatically

### Automatic Backups
- The application creates automatic backups daily
- Backups are stored in the `backups` directory
- You can configure backup settings in `File > Backup Settings`

## Troubleshooting

### Common Issues

#### Application Won't Start
- Ensure you have Java 11 or higher installed
- Check that you have sufficient disk space
- Verify file permissions in the installation directory

#### Database Connection Issues
- Make sure no other instance of the application is running
- Check that the database files in the `data` directory are not corrupted
- Verify that you have write permissions in the application directory

#### Backup/Restore Problems
- Ensure there is enough disk space for backups
- Verify that the backup files are not corrupted
- Check the application logs for detailed error messages

### Viewing Logs
Application logs are stored in:
- **Windows**: `%APPDATA%\WellnessManagementSystem\logs\application.log`
- **Linux/macOS**: `~/.wellness/management/logs/application.log`

## License
This software is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
