#!/bin/bash

# Set application title
echo -e "\033]0;Wellness Management System\007"

# Set Java home - modify if Java is not in system PATH
JAVA_CMD="java"

# Set application home directory
APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Set JVM options
JAVA_OPTS="-Xmx1024m -Xms256m -Dfile.encoding=UTF-8"

# Set classpath
CLASSPATH="$APP_HOME/target/wellness-management-system-1.0-SNAPSHOT.jar:$APP_HOME/target/lib/*"

# Create logs directory if it doesn't exist
mkdir -p "$HOME/.wellness/management/logs"

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if Java is installed
if ! command_exists "$JAVA_CMD"; then
    echo "Error: Java is not installed or not in the system PATH."
    echo "Please install Java 11 or higher and try again."
    exit 1
fi

# Start the application
"$JAVA_CMD" $JAVA_OPTS -cp "$CLASSPATH" com.wellness.WellnessApp "$@"

# Check if the application started successfully
if [ $? -ne 0 ]; then
    echo "Failed to start Wellness Management System"
    read -p "Press Enter to continue..."
    exit 1
fi
