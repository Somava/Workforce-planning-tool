--------------------------------------------------
-- 1) LOOKUP TABLES
--------------------------------------------------
INSERT INTO job_roles (id, name) VALUES 
(1, 'Software Engineer'), (2, 'Project Manager'), (3, 'DevOps Specialist'), 
(4, 'QA Engineer'), (5, 'Data Scientist'), (6, 'UI/UX Designer'),
(7, 'Cybersecurity Analyst'), (8, 'Cloud Architect') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'), (2, 'ROLE_DEPT_HEAD'), (3, 'ROLE_MANAGER'), (4, 'ROLE_EMPLOYEE') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO certifications (id, name) VALUES 
(1, 'AWS Certified Solutions Architect'), (2, 'Java SE 17 Professional'), (3, 'Scrum Master (PSM I)') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO languages (id, name) VALUES 
(1, 'English'), (2, 'German'), (3, 'Spanish') 
ON CONFLICT (id) DO NOTHING;

--------------------------------------------------
-- 2) PROJECTS & DEPARTMENTS
--------------------------------------------------
INSERT INTO projects (id, name, description, status, start_date, end_date, location, published) VALUES 
-- 'IN_PROGRESS' changed to 'PLANNED' to match your Java Enum exactly
(1, 'Cloud Migration 2026', 'Moving legacy infrastructure to AWS.', 'PLANNED', '2026-01-01', '2026-12-31', 'Frankfurt', true),
(2, 'AI Customer Portal', 'LLM-based support system.', 'PLANNED', '2026-03-01', '2027-02-28', 'Remote', true),
(3, 'Legacy ERP Shutdown', 'Decommissioning old local servers.', 'COMPLETED', '2025-01-01', '2025-11-30', 'Frankfurt', true),
(4, 'Mobile App Alpha', 'Internal testing phase for mobile portal.', 'PLANNED', '2026-05-01', '2026-12-01', 'Berlin', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO departments (id, name) VALUES 
(1, 'Information Technology'), 
(2, 'Research & Development'),
(3, 'Human Resources') 
ON CONFLICT (id) DO NOTHING;

--------------------------------------------------
-- 3) EMPLOYEES
--------------------------------------------------
INSERT INTO employees (id, employee_id, first_name, last_name, primary_location, job_role_id, department_id, total_hours_per_week, remaining_hours_per_week, skills) VALUES 
(1, 'E-101', 'Alice', 'Schmidt', 'Frankfurt', 2, 1, 40, 40, '["Management", "Strategy"]'),
(2, 'E-102', 'Bob', 'Müller', 'Frankfurt', 2, 1, 40, 40, '["Agile", "Budgeting"]'),
(3, 'E-103', 'Charlie', 'Wagner', 'Remote', 1, 1, 40, 0, '["Java", "Spring", "Postgres"]'),
(4, 'E-104', 'Diana', 'Prince', 'Berlin', 1, 2, 40, 40, '["Python", "React"]'),
(5, 'E-105', 'Eve', 'Curie', 'Berlin', 2, 3, 40, 40, '["Recruiting", "Onboarding"]'),
(6, 'E-106', 'Frank', 'Castle', 'Frankfurt', 3, 1, 40, 10, '["AWS", "Terraform", "Kubernetes"]'),
(7, 'E-107', 'Grace', 'Hopper', 'Remote', 6, 2, 35, 15, '["Figma", "Adobe XD", "CSS"]')
ON CONFLICT (id) DO NOTHING;

UPDATE employees SET supervisor_id = 1 WHERE id IN (2, 3, 4, 5, 6, 7);

--------------------------------------------------
-- 4) USERS (Auth Layer)
--------------------------------------------------
INSERT INTO users (id, email, password_hash, employee_id) VALUES 
(1, 'alice@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 1),
(2, 'bob@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 2),
(3, 'charlie@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 3),
(4, 'diana@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 4),
(6, 'eve@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 5),
(7, 'frank@frauas.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 6)
ON CONFLICT (id) DO NOTHING;

UPDATE departments SET department_head_user_id = 1 WHERE id = 1;
UPDATE departments SET department_head_user_id = 6 WHERE id = 3;

INSERT INTO user_roles (user_id, role_id) VALUES 
(1,2), (2,3), (3,4), (4,4), (6,2), (7,4) 
ON CONFLICT DO NOTHING;

--------------------------------------------------
-- 5) STAFFING REQUESTS (Matching RequestStatus Enum)
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
-- 'CLOSED' changed to 'CANCELLED' to match RequestStatus.java
(6, 'Network Security Audit', 'Final audit for ERP shutdown.', 3, 1, 7, 'CANCELLED', 1, 110.00, '["Security", "Audit"]', 6, 40)
ON CONFLICT (request_id) DO NOTHING;

--------------------------------------------------
-- 6) EXTERNAL EMPLOYEES & USERS
--------------------------------------------------
INSERT INTO external_employees (id, external_employee_id, provider, first_name, last_name, staffing_request_id, project_id, skills) VALUES 
(1, 'EXT-001', 'FreelanceHub', 'John', 'Doe', 1, 1, '["Java", "Microservices"]') ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, password_hash, external_employee_id) 
VALUES (5, 'john.doe@freelance.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 1) ON CONFLICT (id) DO NOTHING;

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