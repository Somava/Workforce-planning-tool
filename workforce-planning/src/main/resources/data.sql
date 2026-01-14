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
(1, 'Project Skyfall: AWS Migration', 'Migrating core on-premise banking services to a multi-region AWS architecture for high availability.', 'Planned', '2026-01-01', '2032-12-31', 'Munich', true, NULL),
(2, 'NeuralCare: AI Support Portal', 'Development of a Generative AI-driven customer service interface to automate Tier-1 technical support.', 'Planned', '2026-03-01', '2033-02-28', 'Remote', true, NULL),
(3, 'Legacy ERP Sunset Phase 1', 'Final data archiving and decommissioning of the SAP R/3 legacy environment and physical server hardware.', 'Completed', '2025-01-01', '2034-11-30', 'Frankfurt', true, NULL),
(4, 'WorkForce Go: Mobile Alpha', 'Internal testing and rollout of the React Native employee portal for mobile expense and shift management.', 'Planned', '2026-05-01', '2032-12-01', 'Berlin', false, Null)
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
(6, 'E-106', 'Frank', 'Castle', 'Frankfurt', 1, 5, 40, 10, 'frank@frauas.de'),
(7, 'E-107', 'Grace', 'Hopper', 'Remote', 1, 9, 35, 15, 'hopper.it@frauas.de'),
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
--------------------------------------------------------------------------------
-- BATCH 1: INFORMATION TECHNOLOGY (Bob & Eve)
-- 5 Approved, 5 Pending Approval
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
-- BATCH 1: INFORMATION TECHNOLOGY (Bob & Eve)
--------------------------------------------------------------------------------
(1, 'Backend Java Expert', 'Cloud Migration help.', 1, 1, 1, 'APPROVED', 1, 39.50, '["Java", "AWS"]', 5, 40, 'Frankfurt', '2026-02-01', '2026-12-31'),
(2, 'IT Support Specialist', 'General infrastructure setup.', 1, 1, 1, 'APPROVED', 1, 30.25, '["Networking", "Linux"]', 3, 40, 'Frankfurt', '2026-02-01', '2026-06-01'),
(3, 'Cloud Architect', 'AWS Migration lead.', 1, 2, 8, 'APPROVED', 1, 55.50, '["AWS", "Terraform"]', 8, 40, 'Frankfurt', '2026-03-01', '2026-12-31'),
(4, 'DevOps Specialist', 'CI/CD pipeline automation.', 1, 2, 3, 'APPROVED', 1, 45.75, '["Jenkins", "Docker"]', 4, 40, 'Frankfurt', '2026-03-01', '2026-12-31'),
(5, 'Network Security Eng', 'Firewall configuration.', 3, 3, 7, 'APPROVED', 1, 48.00, '["Cisco", "Security"]', 5, 40, 'Frankfurt', '2026-01-15', '2026-08-15'),
(6, 'Database Admin', 'Legacy ERP data migration.', 3, 3, 1, 'PENDING_APPROVAL', 1, 42.50, '["PostgreSQL", "SQL"]', 6, 40, 'Frankfurt', '2026-04-01', '2026-12-01'),
(7, 'Junior IT Analyst', 'Help desk tier 1 support.', 4, 4, 1, 'PENDING_APPROVAL', 1, 25.00, '["Troubleshooting"]', 1, 40, 'Berlin', '2026-05-01', '2026-12-01'),
(8, 'Frontend React Help', 'Portal UI support.', 2, 1, 1, 'PENDING_APPROVAL', 1, 35.80, '["React", "TypeScript"]', 2, 20, 'Berlin', '2026-03-15', '2026-09-15'),
(9, 'IT Project Coordinator', 'Managing hardware rollout.', 4, 4, 2, 'PENDING_APPROVAL', 1, 38.00, '["Agile", "Planning"]', 3, 40, 'Berlin', '2026-05-01', '2026-12-01'),
(10, 'Systems Integrator', 'Middleware connectivity.', 2, 2, 1, 'PENDING_APPROVAL', 1, 40.20, '["API", "JSON"]', 4, 40, 'Remote', '2026-03-01', '2026-10-01'),

--------------------------------------------------------------------------------
-- BATCH 2: RESEARCH & DEVELOPMENT (Charlie & Frank)
--------------------------------------------------------------------------------
(11, 'AI Model Trainer', 'Fine-tuning LLMs.', 2, 5, 5, 'APPROVED', 1, 50.50, '["Python", "PyTorch"]', 3, 40, 'Frankfurt', '2026-04-01', '2026-10-31'),
(12, 'Quantum Research Lead', 'Long-term crypto research.', 1, 5, 1, 'APPROVED', 1, 65.00, '["Quantum Computing"]', 10, 20, 'Frankfurt', '2026-06-01', '2026-12-31'),
(13, 'Data Scientist', 'AI Portal predictive analytics.', 2, 6, 5, 'APPROVED', 1, 52.25, '["R", "SQL", "Math"]', 5, 40, 'Remote', '2026-03-15', '2026-12-15'),
(14, 'NLP Specialist', 'Natural language processing.', 2, 6, 5, 'APPROVED', 1, 58.00, '["NLP", "Transformers"]', 4, 40, 'Remote', '2026-03-15', '2026-12-15'),
(15, 'Hardware Architect', 'New server prototype design.', 3, 7, 1, 'APPROVED', 1, 60.50, '["VHDL", "FPGA"]', 7, 40, 'Frankfurt', '2026-05-01', '2026-12-01'),
(16, 'R&D Lab Assistant', 'Setting up test environments.', 3, 7, 4, 'PENDING_APPROVAL', 1, 28.00, '["Lab Safety", "Testing"]', 2, 40, 'Frankfurt', '2026-05-01', '2026-12-01'),
(17, 'Graphics Engineer', 'UI optimization for Mobile Alpha.', 4, 8, 3, 'PENDING_APPROVAL', 1, 45.40, '["Metal", "OpenGL"]', 4, 40, 'Munich', '2026-02-01', '2026-11-01'),
(18, 'Algorithm Engineer', 'Search algorithm optimization.', 4, 8, 1, 'PENDING_APPROVAL', 1, 48.00, '["Algorithms", "C++"]', 5, 40, 'Munich', '2026-03-01', '2026-12-01'),
(19, 'Machine Learning Eng', 'Training computer vision models.', 1, 5, 5, 'PENDING_APPROVAL', 1, 55.60, '["PyTorch", "OpenCV"]', 4, 40, 'Frankfurt', '2026-04-01', '2026-12-31'),
(20, 'Senior Statistician', 'Data validation for R&D.', 2, 6, 5, 'PENDING_APPROVAL', 1, 47.00, '["Statistics", "SAS"]', 6, 40, 'Remote', '2026-04-01', '2026-12-01'),

--------------------------------------------------------------------------------
-- BATCH 3: HUMAN RESOURCE (Diana & Grace)
--------------------------------------------------------------------------------
(21, 'HR System Specialist', 'Payroll API integration.', 4, 9, 1, 'APPROVED', 1, 38.50, '["API Integration", "HRIS"]', 4, 35, 'Munich', '2026-01-10', '2026-06-30'),
(22, 'Recruitment Lead', 'Tech hiring drive 2026.', 1, 9, 1, 'APPROVED', 1, 35.00, '["Recruiting", "Sourcing"]', 5, 40, 'Frankfurt', '2026-02-01', '2026-12-31'),
(23, 'Employee Relations', 'Conflict resolution for remote staff.', 2, 10, 1, 'APPROVED', 1, 32.40, '["Mediation", "Law"]', 4, 40, 'Berlin', '2026-03-15', '2026-09-15'),
(24, 'Training Coordinator', 'Upskilling for Cloud team.', 1, 10, 1, 'APPROVED', 1, 30.00, '["LMS", "Teaching"]', 3, 40, 'Frankfurt', '2026-03-01', '2026-12-31'),
(25, 'Compensation Analyst', 'Salary benchmarking project.', 3, 11, 5, 'APPROVED', 1, 40.50, '["Excel", "Data Analysis"]', 4, 40, 'Wiesbaden', '2026-05-01', '2026-12-01'),
(26, 'Benefits Specialist', 'New health insurance rollout.', 3, 11, 1, 'PENDING_APPROVAL', 1, 34.00, '["Benefits", "Insurance"]', 3, 40, 'Wiesbaden', '2026-06-01', '2026-12-31'),
(27, 'HR Generalist', 'Administrative support for Mobile.', 4, 12, 1, 'PENDING_APPROVAL', 1, 26.50, '["Admin", "Filing"]', 1, 40, 'Munich', '2026-02-01', '2026-11-01'),
(28, 'Diversity & Inclusion', 'Internal D&I workshop lead.', 2, 12, 1, 'PENDING_APPROVAL', 1, 40.00, '["Strategy", "Workshops"]', 6, 20, 'Berlin', '2026-04-01', '2026-10-01'),
(29, 'HR Data Privacy Officer', 'GDPR compliance audit.', 3, 10, 7, 'PENDING_APPROVAL', 1, 50.75, '["GDPR", "Privacy"]', 5, 40, 'Frankfurt', '2026-04-01', '2026-12-31'),
(30, 'Talent Acquisition', 'Sourcing UI/UX designers.', 2, 9, 1, 'PENDING_APPROVAL', 1, 33.00, '["Sourcing", "LinkedIn"]', 2, 40, 'Berlin', '2026-03-15', '2026-12-15')
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
-- INSERT INTO employee_applications (employee_id, staffing_request_id, status, comment) 
-- VALUES (14, 1, 'APPLIED', 'I have extensive Kubernetes experience.') 
-- ON CONFLICT DO NOTHING;

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



