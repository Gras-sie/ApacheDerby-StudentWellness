-- Database initialization script for Wellness Management System
-- This script creates the necessary tables and initial data

-- Enable autocommit
SET AUTOCOMMIT FALSE;

-- Create schema if it doesn't exist
CREATE SCHEMA wellness;

-- Set the schema
SET SCHEMA wellness;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS counselors;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'COUNSELOR', 'STUDENT')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Create counselors table
CREATE TABLE counselors (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT NOT NULL,
    specialization VARCHAR(100),
    bio CLOB,
    availability VARCHAR(255),
    phone VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create appointments table
CREATE TABLE appointments (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    student_id INT NOT NULL,
    counselor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    notes CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE CASCADE
);

-- Create feedback table
CREATE TABLE feedback (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    appointment_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- Create audit log table
CREATE TABLE audit_log (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT,
    old_value CLOB,
    new_value CLOB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_appointments_student ON appointments(student_id);
CREATE INDEX idx_appointments_counselor ON appointments(counselor_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_feedback_appointment ON feedback(appointment_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, full_name, email, role, is_active)
VALUES (
    'admin',
    -- bcrypt hash of 'admin123'
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH.3zPbY1FJksBSjJQnH1yN6rUBA/fq',
    'System Administrator',
    'admin@wellnessapp.com',
    'ADMIN',
    true
);

-- Insert sample counselor
INSERT INTO users (username, password_hash, full_name, email, role, is_active)
VALUES (
    'counselor1',
    -- bcrypt hash of 'counselor123'
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH.3zPbY1FJksBSjJQnH1yN6rUBA/fq',
    'Dr. Sarah Johnson',
    'sarah.johnson@wellnessapp.com',
    'COUNSELOR',
    true
);

-- Get the ID of the counselor user we just inserted
VALUES NEXT VALUE FOR wellness.USER_ID_SEQ;

-- Insert counselor details
INSERT INTO counselors (user_id, specialization, bio, availability, phone)
VALUES (
    IDENTITY_VAL_LOCAL(),
    'Mental Health Counselor',
    'Dr. Sarah Johnson has over 10 years of experience in mental health counseling with a focus on anxiety and stress management.',
    'Mon-Fri 9:00 AM - 5:00 PM',
    '+1234567890'
);

-- Insert sample student
INSERT INTO users (username, password_hash, full_name, email, role, is_active)
VALUES (
    'student1',
    -- bcrypt hash of 'student123'
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH.3zPbY1FJksBSjJQnH1yN6rUBA/fq',
    'John Doe',
    'john.doe@student.edu',
    'STUDENT',
    true
);

-- Commit all changes
COMMIT;
