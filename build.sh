#!/bin/bash

# Set project directory
PROJECT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# Set Maven command (use mvnw if available, otherwise use system mvn)
if [ -f "$PROJECT_DIR/mvnw" ]; then
    MVN_CMD="$PROJECT_DIR/mvnw"
else
    MVN_CMD="mvn"
fi

echo "Building Wellness Management System..."
echo

# Clean and build the project
$MVN_CMD clean package

if [ $? -ne 0 ]; then
    echo "Error: Build failed. Please check the error messages above."
    read -p "Press Enter to continue..."
    exit 1
fi

echo
echo "Build completed successfully!"
echo

echo "Creating distribution package..."
echo

# Create distribution directory
DIST_DIR="$PROJECT_DIR/dist"
VERSION="1.0.0"
APP_NAME="wellness-management-system"
FULL_NAME="$APP_NAME-$VERSION"

if [ -d "$DIST_DIR" ]; then
    echo "Removing existing distribution directory..."
    rm -rf "$DIST_DIR"
fi

mkdir -p "$DIST_DIR/$FULL_NAME"

# Copy application files
echo "Copying application files..."
cp "$PROJECT_DIR/target/$APP_NAME-$VERSION-jar-with-dependencies.jar" "$DIST_DIR/$FULL_NAME/$APP_NAME.jar"
cp "$PROJECT_DIR/start.bat" "$DIST_DIR/$FULL_NAME/"
cp "$PROJECT_DIR/start.sh" "$DIST_DIR/$FULL_NAME/"
chmod +x "$DIST_DIR/$FULL_NAME/start.sh"

# Create config directory
mkdir -p "$DIST_DIR/$FULL_NAME/config"
cp "$PROJECT_DIR/src/main/resources/"*.properties "$DIST_DIR/$FULL_NAME/config/" 2>/dev/null || :

# Create data directory for database
mkdir -p "$DIST_DIR/$FULL_NAME/data"

# Copy documentation
mkdir -p "$DIST_DIR/$FULL_NAME/docs"
cp "$PROJECT_DIR/README.md" "$DIST_DIR/$FULL_NAME/docs/"
cp "$PROJECT_DIR/LICENSE" "$DIST_DIR/$FULL_NAME/docs/"

# Create tar.gz archive
echo "Creating TAR.GZ archive..."
cd "$DIST_DIR"
tar -czf "$FULL_NAME.tar.gz" "$FULL_NAME"

if [ $? -eq 0 ]; then
    echo
    echo "Distribution package created successfully: $DIST_DIR/$FULL_NAME.tar.gz"
    echo
    echo "To run the application:"
    echo "1. Extract the archive to your desired location"
    echo "2. Run './start.sh' (Linux/macOS) or 'start.bat' (Windows)"
    echo
else
    echo "Error creating TAR.GZ archive."
    read -p "Press Enter to continue..."
    exit 1
fi

read -p "Press Enter to continue..."
