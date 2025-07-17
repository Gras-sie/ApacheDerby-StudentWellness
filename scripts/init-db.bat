@echo off
setlocal enabledelayedexpansion

:: Set paths
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..\
set DERBY_HOME=%PROJECT_DIR%lib\derby
set DERBY_LIB=%DERBY_HOME%\lib\derby.jar;%DERBY_HOME%\lib\derbytools.jar
set DB_DIR=%PROJECT_DIR%data\wellnessDB
set SQL_SCRIPT=%SCRIPT_DIR%init-db.sql

:: Create database directory if it doesn't exist
if not exist "%DB_DIR%" (
    echo Creating database directory: %DB_DIR%
    mkdir "%DB_DIR%"
)

echo Initializing Wellness Management System database...

:: Run the SQL script using ij tool
java -cp "%DERBY_LIB%" org.apache.derby.tools.ij "%SQL_SCRIPT%"

if %ERRORLEVEL% equ 0 (
    echo Database initialized successfully!
) else (
    echo Error initializing database. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)
