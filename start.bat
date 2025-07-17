@echo off
setlocal enabledelayedexpansion

:: Set application title
title Wellness Management System

:: Set Java home - modify if Java is not in system PATH
set JAVA_HOME=java

:: Set application home directory
set APP_HOME=%~dp0

:: Set JVM options
set JAVA_OPTS=-Xmx1024m -Xms256m -Dfile.encoding=UTF-8

:: Set classpath
set CLASSPATH=%APP_HOME%target\wellness-management-system-1.0-SNAPSHOT.jar;%APP_HOME%target\lib\*

:: Create logs directory if it doesn't exist
if not exist "%APPDATA%\WellnessManagementSystem\logs" mkdir "%APPDATA%\WellnessManagementSystem\logs"

:: Start the application
%JAVA_HOME% %JAVA_OPTS% -cp "%CLASSPATH%" com.wellness.WellnessApp %*

:: Check if the application started successfully
if %ERRORLEVEL% neq 0 (
    echo Failed to start Wellness Management System
    pause
    exit /b %ERRORLEVEL%
)
