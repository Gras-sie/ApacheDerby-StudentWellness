@echo off
setlocal enabledelayedexpansion

:: Set project directory
set PROJECT_DIR=%~dp0

:: Set Maven command (use mvnw if available, otherwise use system mvn)
if exist "%PROJECT_DIR%mvnw.cmd" (
    set MVN_CMD="%PROJECT_DIR%mvnw.cmd"
) else (
    set MVN_CMD=mvn
)

echo Building Wellness Management System...
echo.

:: Clean and build the project
%MVN_CMD% clean package

if %ERRORLEVEL% neq 0 (
    echo Error: Build failed. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build completed successfully!
echo.

echo Creating distribution package...
echo.

:: Create distribution directory
set DIST_DIR=%PROJECT_DIR%dist
set VERSION=1.0.0
set APP_NAME=wellness-management-system
set FULL_NAME=%APP_NAME%-%VERSION%

if exist "%DIST_DIR%" (
    echo Removing existing distribution directory...
    rmdir /s /q "%DIST_DIR%"
)

mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\%FULL_NAME%"

:: Copy application files
echo Copying application files...
copy "%PROJECT_DIR%target\%APP_NAME%-%VERSION%-jar-with-dependencies.jar" "%DIST_DIR%\%FULL_NAME%\%APP_NAME%.jar"
copy "%PROJECT_DIR%start.bat" "%DIST_DIR%\%FULL_NAME%\"
copy "%PROJECT_DIR%start.sh" "%DIST_DIR%\%FULL_NAME%\"

:: Create config directory
mkdir "%DIST_DIR%\%FULL_NAME%\config"
copy "%PROJECT_DIR%src\main\resources\*.properties" "%DIST_DIR%\%FULL_NAME%\config\"

:: Create data directory for database
mkdir "%DIST_DIR%\%FULL_NAME%\data"

:: Copy documentation
mkdir "%DIST_DIR%\%FULL_NAME%\docs"
copy "%PROJECT_DIR%README.md" "%DIST_DIR%\%FULL_NAME%\docs\"
copy "%PROJECT_DIR%LICENSE" "%DIST_DIR%\%FULL_NAME%\docs\"

:: Create zip archive
echo Creating ZIP archive...
cd /d "%DIST_DIR%"
powershell -Command "Compress-Archive -Path '%FULL_NAME%' -DestinationPath '%FULL_NAME%.zip' -Force"

if %ERRORLEVEL% equ 0 (
    echo.
    echo Distribution package created successfully: %DIST_DIR%\%FULL_NAME%.zip
    echo.
    echo To run the application:
    echo 1. Extract the ZIP file to your desired location
    echo 2. Run 'start.bat' (Windows) or 'start.sh' (Linux/macOS)
    echo.
) else (
    echo Error creating ZIP archive.
    pause
    exit /b %ERRORLEVEL%
)

pause
