-- Sample data for counselors
INSERT INTO counselors (name, specialization, availability, email, phone) 
VALUES 
('Dr. Sarah Johnson', 'Stress Management', 'Mon-Fri 9am-5pm', 'sarah.johnson@wellness.com', '123-456-7890'),
('Dr. Michael Chen', 'Anxiety Disorders', 'Tue-Thu 10am-6pm', 'michael.chen@wellness.com', '123-456-7891'),
('Dr. Emily Wilson', 'Depression', 'Mon-Wed-Fri 8am-4pm', 'emily.wilson@wellness.com', '123-456-7892');

-- Sample data for appointments
INSERT INTO appointments (student_name, counselor_id, appointment_date, appointment_time, status, notes)
VALUES
('John Doe', 1, CURRENT_DATE, '14:30:00', 'SCHEDULED', 'Initial consultation'),
('Jane Smith', 2, CURRENT_DATE + 1, '11:00:00', 'SCHEDULED', 'Follow-up session'),
('Alex Johnson', 1, CURRENT_DATE + 2, '15:45:00', 'PENDING', 'New patient');

-- Sample data for feedback
INSERT INTO feedback (student_name, counselor_id, rating, comments, is_anonymous)
VALUES
('Jane Smith', 1, 5, 'Excellent session, very helpful!', false),
('Anonymous', 2, 4, 'Good advice, but had to wait 15 minutes.', true),
('Alex Johnson', 1, 5, 'Very understanding and professional.', false);
