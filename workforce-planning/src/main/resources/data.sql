--------------------------------------------------
-- 1) LOOKUP TABLES
--------------------------------------------------

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

INSERT INTO projects (id, name, description, status, start_date, end_date, location, published, manager_user_id) VALUES 
(1, 'Cloud Migration 2026', 'Moving legacy infrastructure to AWS.', 'PLANNED', '2026-01-01', '2026-12-31', 'Frankfurt', true, NULL),
(2, 'AI Customer Portal', 'LLM-based support system.', 'PLANNED', '2026-03-01', '2027-02-28', 'Remote', true, NULL),
(3, 'Legacy ERP Shutdown', 'Decommissioning old local servers.', 'COMPLETED', '2025-01-01', '2025-11-30', 'Frankfurt', true, NULL),
(4, 'Mobile App Alpha', 'Internal testing phase for mobile portal.', 'PLANNED', '2026-05-01', '2026-12-01', 'Berlin', false, Null)
ON CONFLICT (id) DO NOTHING;

INSERT INTO departments (id, name, project_id, department_head_user_id, resource_planner_user_id) VALUES 
-- Project 1 (Cloud)
(1, 'Information Technology', 1, NULL, NULL), (5, 'Research & Development', 1, NULL, NULL), (9, 'Human Resource', 1, NULL, NULL),
-- Project 2 (AI)
(2, 'Information Technology', 2, NULL, NULL), (6, 'Research & Development', 2, NULL, NULL), (10, 'Human Resource', 2, NULL, NULL),
-- Project 3 (Legacy)
(3, 'Information Technology', 3, NULL, NULL), (7, 'Research & Development', 3, NULL, NULL), (11, 'Human Resource', 3, NULL, NULL),
-- Project 4 (Mobile)
(4, 'Information Technology', 4, NULL, NULL), (8, 'Research & Development', 4, NULL, NULL), (12, 'Human Resource', 4, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, employee_id, first_name, last_name, primary_location, job_role_id, department_id, total_hours_per_week, remaining_hours_per_week, email) VALUES 
(1, 'E-101', 'Alice', 'Schmidt', 'Frankfurt', 3, 1, 40, 40, 'alice@frauas.de'),
(2, 'E-102', 'Bob', 'Müller', 'Frankfurt', 2, 1, 40, 40, 'bob@frauas.de'),
(3, 'E-103', 'Charlie', 'Wagner', 'Remote', 2, 5, 40, 0, 'charlie@frauas.de'),
(4, 'E-104', 'Diana', 'Prince', 'Berlin', 2, 9, 40, 40, 'diana@frauas.de'),
(5, 'E-105', 'Eve', 'Curie', 'Berlin', 1, 3, 40, 40, 'eve@frauas.de'),
(6, 'E-106', 'Frank', 'Castle', 'Frankfurt', 1, 1, 40, 10, 'frank@frauas.de'),
(7, 'E-107', 'Grace', 'Hopper', 'Remote', 1, 2, 35, 15, 'hopper.it@frauas.de'),
-- The 12 Dept Heads
(8, 'E-108', 'Marcus', 'Wagner', 'Frankfurt', 4, 1, 40, 40, 'wagner.it@frauas.de'), 
(9, 'E-109', 'Elena', 'Fischer', 'Frankfurt', 4, 2, 40, 40, 'fischer.rd@frauas.de'), 
(10, 'E-110', 'Thomas', 'Weber', 'Frankfurt', 4, 3, 40, 40, 'weber.hr@frauas.de'),
(11, 'E-111', 'Sarah', 'Becker', 'Remote', 4, 4, 40, 40, 'becker.it@frauas.de'), 
(12, 'E-112', 'Lukas', 'Hoffmann', 'Remote', 4, 5, 40, 40, 'hoffmann.rd@frauas.de'), 
(13, 'E-113', 'Miriam', 'Schulz', 'Remote', 4, 6, 40, 40, 'schulz.hr@frauas.de'),
(14, 'E-114', 'Julian', 'Koch', 'Frankfurt', 4, 7, 40, 40, 'koch.it@frauas.de'), 
(15, 'E-115', 'Sophia', 'Bauer', 'Frankfurt', 4, 8, 40, 40, 'bauer.rd@frauas.de'), 
(16, 'E-116', 'David', 'Richter', 'Frankfurt', 4, 9, 40, 40, 'richter.hr@frauas.de'),
(17, 'E-117', 'Hannah', 'Wolf', 'Berlin', 4, 10, 40, 40, 'wolf.it@frauas.de'), 
(18, 'E-118', 'Simon', 'Klein', 'Berlin', 4, 11, 40, 40, 'klein.rd@frauas.de'), 
(19, 'E-119', 'Laura', 'Neumann', 'Berlin', 4, 12, 40, 40, 'neumann.hr@frauas.de')
ON CONFLICT (id) DO NOTHING;

UPDATE employees SET supervisor_id = 1 WHERE id IN (2, 3, 4, 5, 6, 7);


INSERT INTO users (id, email, password_hash, employee_id) VALUES 
(1, 'alice@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 1),
(2, 'bob@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 2),
(3, 'charlie@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 3),
(4, 'diana@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 4),
(5, 'eve@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 5),
(6, 'frank@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 6),
-- Auth for Heads
(7, 'hopper.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 7),
(8, 'wagner.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 8),
(9, 'fischer.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 9),
(10, 'weber.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 10),
(11, 'becker.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 11),
(12, 'hoffmann.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 12),
(13, 'schulz.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 13),
(14, 'koch.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 14),
(15, 'bauer.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 15),
(16, 'richter.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 16),
(17, 'wolf.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 17),
(18, 'klein.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 18),
(19, 'neumann.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 19)
ON CONFLICT (id) DO NOTHING;


UPDATE departments SET department_head_user_id = 2, resource_planner_user_id = 5 WHERE id IN (1, 2, 3, 4); 
UPDATE departments SET department_head_user_id = 3, resource_planner_user_id = 6 WHERE id IN (5, 6, 7, 8);
UPDATE departments SET department_head_user_id = 4, resource_planner_user_id = 7 WHERE id IN (9, 10, 11, 12);

UPDATE projects SET manager_user_id = 1;

-- Assign Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1,3), (2,2), (3,2), (4,2),(5,1), (6,1), (7,1) 
ON CONFLICT DO NOTHING;

-- NEW: Automated role assignment for the 12 unique heads
INSERT INTO user_roles (user_id, role_id)
SELECT id, 4 FROM users WHERE id BETWEEN 8 AND 19
ON CONFLICT DO NOTHING;

--------------------------------------------------
-- 5) STAFFING REQUESTS
--------------------------------------------------
INSERT INTO staffing_requests (
    request_id, title, description, project_id, department_id, job_role_id,
    status, created_by_employee_id, wage_per_hour, required_skills, 
    experience_years, availability_hours_per_week,
    work_location, project_start_date, project_end_date
) VALUES 
-- Batch 1: 
(1, 'Backend Java Expert', 'Urgent need for Cloud Migration.', 1, 1, 1, 'PENDING_APPROVAL', 1, 39.50, '["Java", "AWS"]', 5, 40, 'Frankfurt', '2026-02-01', '2026-12-31'),
(2, 'Frontend React Help', 'Support for AI Portal UI.', 2, 2, 1, 'PENDING_APPROVAL', 1, 35.00, '["React", "TypeScript"]', 2, 20, 'Berlin', '2026-03-15', '2026-09-15'),
(3, 'Cloud Infrastructure Architect', 'Senior role for AWS Migration.', 1, 1, 8, 'PENDING_APPROVAL', 1, 40.00, '["AWS", "Terraform"]', 8, 40, 'Frankfurt', '2026-02-01', '2026-11-30'),
(4, 'Junior Data Analyst', 'Help with LLM data sets.', 2, 2, 5, 'PENDING_APPROVAL', 1, 32.00, '["Python", "Excel"]', 1, 40, 'Berlin', '2026-04-01', '2026-10-01'),
(5, 'HR System Specialist', 'Integration of new payroll API.', 4, 3, 1, 'APPROVED', 1, 38.00, '["API Integration", "HRIS"]', 4, 35, 'Munich', '2026-01-10', '2026-06-30'),

-- Batch 2: 
(6, 'Data Engineer', 'Building ETL pipelines for Project 1.', 1, 2, 5, 'PENDING_APPROVAL', 1, 38.00, '["Python", "Spark"]', 4, 40, 'Frankfurt', '2026-02-10', '2026-12-10'),
(7, 'Frontend Lead', 'Architecting the new React dashboard.', 2, 3, 1, 'PENDING_APPROVAL', 1, 40.00, '["React", "Redux"]', 7, 35, 'Berlin', '2026-03-20', '2026-11-20'),
(8, 'Cybersecurity Lead', 'Security hardening for database.', 3, 4, 6, 'PENDING_APPROVAL', 1, 40.00, '["Penetration Testing", "ISO27001"]', 8, 40, 'Wiesbaden', '2026-06-01', '2026-12-31'),
(9, 'Site Reliability Eng', 'Uptime monitoring for cloud portal.', 4, 2, 2, 'PENDING_APPROVAL', 1, 39.00, '["Prometheus", "Grafana"]', 5, 40, 'Munich', '2026-01-15', '2026-10-15'),
(10, 'Machine Learning Eng', 'Training models for AI support.', 1, 3, 5, 'PENDING_APPROVAL', 1, 40.00, '["PyTorch", "NLP"]', 4, 40, 'Frankfurt', '2026-04-01', '2026-12-31'),

-- Batch 3: 
(11, 'AI Model Trainer', 'Fine-tuning LLMs for customer support.', 1, 5, 5, 'PENDING_APPROVAL', 1, 39.00, '["Python", "PyTorch"]', 3, 40, 'Frankfurt', '2026-04-01', '2026-10-31'),
(12, 'R&D Lead', 'Overseeing AI Portal innovations.', 2, 6, 1, 'PENDING_APPROVAL', 1, 40.00, '["Management", "AI Ethics"]', 10, 35, 'Berlin', '2026-03-15', '2026-12-15'),
(13, 'Data Scientist', 'Analyzing legacy ERP data patterns.', 3, 7, 5, 'PENDING_APPROVAL', 1, 38.00, '["R", "SQL"]', 5, 40, 'Wiesbaden', '2026-05-01', '2026-12-01'),
(14, 'Graphics Engineer', 'UI optimization for Mobile Alpha.', 4, 8, 3, 'PENDING_APPROVAL', 1, 37.00, '["Metal", "OpenGL"]', 4, 40, 'Munich', '2026-02-01', '2026-11-01'),
(15, 'Quantum Research Assistant', 'Long-term infrastructure research.', 1, 5, 1, 'APPROVED', 1, 32.00, '["Quantum Computing"]', 2, 20, 'Frankfurt', '2026-06-01', '2026-12-31');

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
VALUES (14, 1, 'APPLIED', 'I have extensive Kubernetes experience.') 
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



