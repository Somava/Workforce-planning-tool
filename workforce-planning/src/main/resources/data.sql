--------------------------------------------------
-- 1) LOOKUP TABLES
--------------------------------------------------

INSERT INTO projects (id, name, description, status, start_date, end_date, location, published) VALUES 
(1, 'Cloud Migration 2026', 'Moving legacy infrastructure to AWS.', 'PLANNED', '2026-01-01', '2026-12-31', 'Frankfurt', true),
(2, 'AI Customer Portal', 'LLM-based support system.', 'PLANNED', '2026-03-01', '2027-02-28', 'Remote', true),
(3, 'Legacy ERP Shutdown', 'Decommissioning old local servers.', 'COMPLETED', '2025-01-01', '2025-11-30', 'Frankfurt', true),
(4, 'Mobile App Alpha', 'Internal testing phase for mobile portal.', 'PLANNED', '2026-05-01', '2026-12-01', 'Berlin', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO job_roles (id, name) VALUES 
(1, 'Software Engineer'), (2, 'Project Manager'), (3, 'DevOps Specialist'), 
(4, 'QA Engineer'), (5, 'Data Scientist'), (6, 'UI/UX Designer'),
(7, 'Cybersecurity Analyst'), (8, 'Cloud Architect') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_RESOURCE_PLNR'), (2, 'ROLE_DEPT_HEAD'), (3, 'ROLE_MANAGER'), (4, 'ROLE_EMPLOYEE') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO certifications (id, name) VALUES 
(1, 'AWS Certified Solutions Architect'), (2, 'Java SE 17 Professional'), (3, 'Scrum Master (PSM I)') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO languages (id, name) VALUES 
(1, 'English'), (2, 'German'), (3, 'Spanish') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO departments (id, name, project_id, department_head_user_id) VALUES 
-- Project 1 (Cloud)
(1, 'Information Technology', 1, NULL), (2, 'Research & Development', 1, NULL), (3, 'Human Resource', 1, NULL),
-- Project 2 (AI)
(4, 'Information Technology', 2, NULL), (5, 'Research & Development', 2, NULL), (6, 'Human Resource', 2, NULL),
-- Project 3 (Legacy)
(7, 'Information Technology', 3, NULL), (8, 'Research & Development', 3, NULL), (9, 'Human Resource', 3, NULL),
-- Project 4 (Mobile)
(10, 'Information Technology', 4, NULL), (11, 'Research & Development', 4, NULL), (12, 'Human Resource', 4, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, employee_id, first_name, last_name, primary_location, job_role_id, department_id, total_hours_per_week, remaining_hours_per_week) VALUES 
(1, 'E-101', 'Alice', 'Schmidt', 'Frankfurt', 3, 1, 40, 40),
(2, 'E-102', 'Bob', 'Müller', 'Frankfurt', 3, 1, 40, 40),
(3, 'E-103', 'Charlie', 'Wagner', 'Remote', 3, 1, 40, 0),
(4, 'E-104', 'Diana', 'Prince', 'Berlin', 3, 2, 40, 40),
(5, 'E-105', 'Eve', 'Curie', 'Berlin', 1, 3, 40, 40),
(6, 'E-106', 'Frank', 'Castle', 'Frankfurt', 1, 1, 40, 10),
(7, 'E-107', 'Grace', 'Hopper', 'Remote', 4, 2, 35, 15),
-- The 12 Dept Heads
(8, 'E-108', 'Marcus', 'Wagner', 'Frankfurt', 2, 1, 40, 40), 
(9, 'E-109', 'Elena', 'Fischer', 'Frankfurt', 2, 2, 40, 40), 
(10, 'E-110', 'Thomas', 'Weber', 'Frankfurt', 2, 3, 40, 40),
(11, 'E-111', 'Sarah', 'Becker', 'Remote', 2, 4, 40, 40), 
(12, 'E-112', 'Lukas', 'Hoffmann', 'Remote', 2, 5, 40, 40), 
(13, 'E-113', 'Miriam', 'Schulz', 'Remote', 2, 6, 40, 40),
(14, 'E-114', 'Julian', 'Koch', 'Frankfurt', 2, 7, 40, 40), 
(15, 'E-115', 'Sophia', 'Bauer', 'Frankfurt', 2, 8, 40, 40), 
(16, 'E-116', 'David', 'Richter', 'Frankfurt', 2, 9, 40, 40),
(17, 'E-117', 'Hannah', 'Wolf', 'Berlin', 2, 10, 40, 40), 
(18, 'E-118', 'Simon', 'Klein', 'Berlin', 2, 11, 40, 40), 
(19, 'E-119', 'Laura', 'Neumann', 'Berlin', 2, 12, 40, 40)
ON CONFLICT (id) DO NOTHING;

UPDATE employees SET supervisor_id = 1 WHERE id IN (2, 3, 4, 5, 6, 7);


INSERT INTO users (id, email, password_hash, employee_id) VALUES 
(1, 'alice@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 1),
(2, 'bob@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 2),
(3, 'charlie@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 3),
(4, 'diana@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 4),
(6, 'eve@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 5),
(7, 'frank@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 6),
-- Auth for Heads
(8, 'wagner.it@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 8),
(9, 'fischer.rd@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 9),
(10, 'weber.hr@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 10),
(11, 'becker.it@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 11),
(12, 'hoffmann.rd@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 12),
(13, 'schulz.hr@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 13),
(14, 'koch.it@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 14),
(15, 'bauer.rd@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 15),
(16, 'richter.hr@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 16),
(17, 'wolf.it@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 17),
(18, 'klein.rd@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 18),
(19, 'neumann.hr@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 19)
ON CONFLICT (id) DO NOTHING;

UPDATE departments SET department_head_user_id = 8 WHERE id = 1; 
UPDATE departments SET department_head_user_id = 9 WHERE id = 2;
UPDATE departments SET department_head_user_id = 10 WHERE id = 3; -- Marcus heads IT
UPDATE departments SET department_head_user_id = 11 WHERE id = 4;
UPDATE departments SET department_head_user_id = 12 WHERE id = 5; -- Marcus heads IT
UPDATE departments SET department_head_user_id = 13 WHERE id = 6;
UPDATE departments SET department_head_user_id = 14 WHERE id = 7; -- Marcus heads IT
UPDATE departments SET department_head_user_id = 15 WHERE id = 8;
UPDATE departments SET department_head_user_id = 16 WHERE id = 9; -- Marcus heads IT
UPDATE departments SET department_head_user_id = 17 WHERE id = 10;
UPDATE departments SET department_head_user_id = 18 WHERE id = 11; -- Marcus heads IT
UPDATE departments SET department_head_user_id = 19 WHERE id = 12;

-- Assign Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1,2), (2,3), (3,4), (4,4), (6,2), (7,4) 
ON CONFLICT DO NOTHING;

-- NEW: Automated role assignment for the 12 unique heads
INSERT INTO user_roles (user_id, role_id)
SELECT id, 2 FROM users WHERE id BETWEEN 8 AND 19
ON CONFLICT DO NOTHING;

--------------------------------------------------
-- 5) STAFFING REQUESTS
--------------------------------------------------
INSERT INTO staffing_requests (
    request_id, title, description, project_id, department_id, job_role_id,
    status, created_by_employee_id, wage_per_hour, required_skills, 
    experience_years, availability_hours_per_week
) VALUES 
(1, 'Backend Java Expert', 'Urgent need for Cloud Migration.', 1, 1, 1, 'SUBMITTED', 2, 95.00, '["Java", "AWS"]', 5, 40),
(2, 'Frontend React Help', 'Support for AI Portal UI.', 2, 2, 1, 'DRAFT', 2, 80.00, '["React", "TypeScript"]', 2, 20),
(3, 'Cloud Infrastructure Architect', 'Senior role for AWS Migration.', 1, 1, 8, 'PUBLISHED', 2, 125.00, '["AWS", "Terraform"]', 8, 40),
(4, 'Junior Data Analyst', 'Help with LLM data sets.', 2, 2, 5, 'DRAFT', 2, 45.00, '["Python", "Excel"]', 1, 40),
(5, 'HR System Specialist', 'Integration of new payroll API.', 4, 3, 1, 'SUBMITTED', 5, 75.00, '["API Integration", "HRIS"]', 4, 35),
(6, 'Network Security Audit', 'Final audit for ERP shutdown.', 3, 1, 7, 'CANCELLED', 1, 110.00, '["Security", "Audit"]', 6, 40)
ON CONFLICT (request_id) DO NOTHING;

--------------------------------------------------
-- 6) EXTERNAL EMPLOYEES & USERS
--------------------------------------------------
INSERT INTO external_employees (id, external_employee_id, provider, first_name, last_name, staffing_request_id, project_id, skills) VALUES 
(1, 'EXT-001', 'FreelanceHub', 'John', 'Doe', 1, 1, '["Java", "Microservices"]') ON CONFLICT (id) DO NOTHING;

-- Freelancer User ID set to 50 to avoid any conflict with Dept Heads
INSERT INTO users (id, email, password_hash, external_employee_id) 
VALUES (50, 'john.doe@freelance.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 1) ON CONFLICT (id) DO NOTHING;

--------------------------------------------------
-- 7) APPLICATIONS & ASSIGNMENTS
--------------------------------------------------
INSERT INTO employee_applications (employee_id, staffing_request_id, status, comment) 
VALUES (3, 1, 'APPLIED', 'I have 5 years of Java exp.'),
       (6, 3, 'APPLIED', 'I have extensive Kubernetes experience.') 
ON CONFLICT DO NOTHING;

INSERT INTO assignments (employee_id, project_id, status, period_start, period_end) 
VALUES (3, 1, 'ACTIVE', '2026-01-01', '2026-06-01'),
       (7, 2, 'ACTIVE', '2026-03-01', '2026-12-31')
ON CONFLICT DO NOTHING;

--------------------------------------------------
-- 8) SEQUENCE SYNCHRONIZATION
--------------------------------------------------
SELECT setval(pg_get_serial_sequence('employees', 'id'), (SELECT MAX(id) FROM employees));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('projects', 'id'), (SELECT MAX(id) FROM projects));
SELECT setval(pg_get_serial_sequence('departments', 'id'), (SELECT MAX(id) FROM departments));
SELECT setval(pg_get_serial_sequence('staffing_requests', 'request_id'), (SELECT MAX(request_id) FROM staffing_requests));
SELECT setval(pg_get_serial_sequence('external_employees', 'id'), (SELECT MAX(id) FROM external_employees));
SELECT setval(pg_get_serial_sequence('job_roles', 'id'), (SELECT MAX(id) FROM job_roles));