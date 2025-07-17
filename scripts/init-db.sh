#!/bin/bash

# Set paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/.."
DERBY_HOME="$PROJECT_DIR/lib/derby"
DERBY_LIB="$DERBY_HOME/lib/derby.jar:$DERBY_HOME/lib/derbytools.jar"
DB_DIR="$PROJECT_DIR/data/wellnessDB"
SQL_SCRIPT="$SCRIPT_DIR/init-db.sql"

# Create database directory if it doesn't exist
if [ ! -d "$DB_DIR" ]; then
    echo "Creating database directory: $DB_DIR"
    mkdir -p "$DB_DIR"
fi

echo "Initializing Wellness Management System database..."

# Run the SQL script using ij tool
java -cp "$DERBY_LIB" org.apache.derby.tools.ij "$SQL_SCRIPT"

if [ $? -eq 0 ]; then
    echo "Database initialized successfully!"
else
    echo "Error initializing database. Please check the error messages above."
    read -p "Press Enter to continue..."
    exit 1
fi
