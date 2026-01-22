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
(2, 'NeuralCare: AI Support Portal', 'Development of a Generative AI-driven customer service interface to automate Tier-1 technical support.', 'Planned', '2026-03-01', '2033-02-28', 'Darmstadt', true, NULL),
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

INSERT INTO employees (
    id, email, employee_id, first_name, last_name, 
    supervisor_id, primary_location, contract_type, 
    experience_years, wage_per_hour, emergency_contact, 
    availability_start, availability_end, matching_availability, 
    job_role_id, department_id, default_role_id, 
    skills, total_hours_per_week, 
    performance_rating, project_preferences, interests
) VALUES 
-- 1. Manager for all projects
(1, 'alice@frauas.de', 'E-101', 'Alice', 'Schmidt', NULL, 'Frankfurt', 'FULL_TIME', 15, 39.90, '+49-15510440423', '2026-01-01', '2030-12-31', 'AVAILABLE', 3, 1, 3, 
'["Project Management", "Leadership"]'::jsonb, 40, 4.6, 'All Projects', 'Leadership'),

-- 2. Department Heads
(2, 'bob@frauas.de', 'E-102', 'Bob', 'Müller', 1, 'Frankfurt', 'FULL_TIME', 12, 39.50, '+49-15510440428', '2026-01-01', '2030-12-31', 'AVAILABLE', 2, 1, 2, 
'["IT Strategy", "Network Security"]'::jsonb, 40, 4.2, 'IT Infrastructure', 'Tech'),
(3, 'charlie@frauas.de', 'E-103', 'Charlie', 'Wagner', 1, 'Munich', 'FULL_TIME', 14, 39.50, '+49-15510440476', '2026-01-01', '2030-12-31', 'AVAILABLE', 2, 5, 2, 
'["Product R&D", "Agile"]'::jsonb, 40, 3.9, 'R&D Innovation', 'Research'),
(4, 'diana@frauas.de', 'E-104', 'Diana', 'Prince', 1, 'Berlin', 'FULL_TIME', 10, 32.00, '+49-15510440873', '2026-01-01', '2030-12-31', 'AVAILABLE', 2, 9, 2, 
'["HR Strategy", "Conflict Resolution"]'::jsonb, 40, 4.1, 'HR Strategy', 'People'),

-- 3. Resource Planners
(5, 'eve@frauas.de', 'E-105', 'Eve', 'Curie', 1, 'Berlin', 'FULL_TIME', 8, 29.00, '+49-15510440142', '2026-01-01', '2030-12-31', 'AVAILABLE', 1, 3, 1, 
'["Resource Allocation", "Capacity Planning"]'::jsonb, 40, 3.7, 'IT Planning', 'Organization'),
(6, 'frank@frauas.de', 'E-106', 'Frank', 'Castle', 1, 'Frankfurt', 'FULL_TIME', 9, 36.00, '+49-15510440408', '2026-01-01', '2030-12-31', 'AVAILABLE', 1, 5, 1, 
'["Strategic Planning", "Operations"]'::jsonb, 40, 4.4, 'R&D Scheduling', 'Defense'),
(7, 'hopper.it@frauas.de', 'E-107', 'Grace', 'Hopper', 1, 'Darmstadt', 'FULL_TIME', 20, 38.50, '+49-15510440919', '2026-01-01', '2030-12-31', 'AVAILABLE', 1, 9, 1, 
'["Systems Programming", "COBOL"]'::jsonb, 40, 3.8, 'HR Logistics', 'Coding'),

-- 8-11: IT Specialists
(8, 'wagner.it@frauas.de', 'E-108', 'Marcus', 'Wagner', 1, 'Frankfurt', 'FULL_TIME', 10, 38.00, '+49-15510440409', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 1, 4, 
'["Java", "Spring Boot", "AWS"]'::jsonb, 40, 4.0, 'Cloud Migration', 'Gaming'), -- FULL TIME

(9, 'fischer.rd@frauas.de', 'E-109', 'Elena', 'Fischer', 1, 'Frankfurt', 'PART_TIME', 7, 27.00, '+49-15510440451', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 5, 4, 
'["React", "TypeScript", "CSS"]'::jsonb, 20, 3.6, 'Frontend UI', 'Art'), -- PART TIME

(10, 'weber.hr@frauas.de', 'E-110', 'Thomas', 'Weber', 1, 'Frankfurt', 'FULL_TIME', 8, 29.00, '+49-15510440003', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 9, 4, 
'["Docker", "Kubernetes", "AWS"]'::jsonb, 40, 4.3, 'DevOps', 'Cycling'), -- FULL TIME

(11, 'becker.it@frauas.de', 'E-111', 'Sarah', 'Becker', 1, 'Berlin', 'PART_TIME', 5, 26.00, '+49-15510440411', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 4, 4, 
'["Selenium", "QA Testing", "Automation"]'::jsonb, 20, 3.5, 'QA Automation', 'Swimming'), -- PART TIME

-- 12-15: R&D Specialists
(12, 'hoffmann.rd@frauas.de', 'E-112', 'Lukas', 'Hoffmann', 1, 'Darmstadt', 'FULL_TIME', 6, 29.00, '+49-15510443654', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 5, 4, 
'["Python", "Machine Learning", "PyTorch"]'::jsonb, 40, 4.1, 'AI Models', 'Chess'),

(13, 'schulz.hr@frauas.de', 'E-113', 'Miriam', 'Schulz', 1, 'Frankfurt', 'PART_TIME', 4, 32.00, '+49-15510440467', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 9, 4, 
'["Data Analytics", "SQL", "PostgreSQL"]'::jsonb, 20, 3.9, 'Data Analysis', 'Reading'),

(14, 'koch.it@frauas.de', 'E-114', 'Julian', 'Koch', 1, 'Munich', 'FULL_TIME', 5, 36.00, '+49-15510440711', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 3, 4, 
'["Cybersecurity", "GDPR", "Risk Analysis"]'::jsonb, 40, 4.5, 'Privacy Compliance', 'Running'),

(15, 'bauer.rd@frauas.de', 'E-115', 'Sophia', 'Bauer', 1, 'Frankfurt', 'PART_TIME', 6, 35.50, '+49-15510440486', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 8, 4, 
'["C++", "Embedded Systems", "Linux"]'::jsonb, 20, 3.4, 'Core Logic', 'Hiking'),

-- 16-19: HR Specialists
(16, 'richter.hr@frauas.de', 'E-116', 'David', 'Richter', 1, 'Frankfurt', 'FULL_TIME', 3, 21.00, '+49-15510440216', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 9, 4, 
'["Recruiting", "Sourcing", "TypeScript"]'::jsonb, 40, 4.0, 'Talent Acquisition', 'Cooking'),

(17, 'wolf.it@frauas.de', 'E-117', 'Hannah', 'Wolf', 1, 'Berlin', 'PART_TIME', 2, 28.00, '+49-15510440931', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 1, 4, 
'["Technical Support", "Linux", "Troubleshooting"]'::jsonb, 20, 3.8, 'IT Support', 'Music'),

(18, 'klein.rd@frauas.de', 'E-118', 'Simon', 'Klein', 1, 'Berlin', 'FULL_TIME', 4, 33.00, '+49-15510440721', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 6, 4, 
'["Market Research", "Excel", "Algorithms"]'::jsonb, 40, 4.2, 'Benchmarking', 'Gardening'),

(19, 'neumann.hr@frauas.de', 'E-119', 'Laura', 'Neumann', 1, 'Darmstadt', 'PART_TIME', 3, 34.00, '+49-15510440240', '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 12, 4, 
'["Employee Relations", "Payroll", "HRIS"]'::jsonb, 20, 3.6, 'HR Operations', 'Yoga'),

-- IT / Infra (helps request 2: Networking+Linux, request 7: Troubleshooting, request 5: Cisco+Security)
(20, 'meier.it@frauas.de', 'E-120', 'Jonas', 'Meier', 1, 'Munich', 'FULL_TIME', 6, 30.00, '+49-15510000020',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 1, 4,
 '["Networking", "Linux", "Troubleshooting"]'::jsonb, 40, 4.0, 'Skyfall Infra', 'Systems'),

(21, 'zimmer.it@frauas.de', 'E-121', 'Nina', 'Zimmer', 1, 'Berlin', 'PART_TIME', 3, 24.00, '+49-15510000021',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 4, 4,
 '["Troubleshooting", "Technical Support", "Linux"]'::jsonb, 20, 3.7, 'Mobile Support', 'Music'),

(22, 'hartmann.it@frauas.de', 'E-122', 'Felix', 'Hartmann', 1, 'Frankfurt', 'FULL_TIME', 7, 37.00, '+49-15510000022',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 3, 4,
 '["Cisco", "Security", "Network Security"]'::jsonb, 40, 4.2, 'Legacy Security', 'Security'),

-- DevOps (helps request 4: Jenkins+Docker, also cloud/devops pool)
(23, 'kruger.it@frauas.de', 'E-123', 'Lena', 'Kruger', 1, 'Frankfurt', 'PART_TIME', 4, 26.00, '+49-15510000023',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 2, 4,
 '["Jenkins", "Docker", "CI/CD"]'::jsonb, 20, 3.9, 'Skyfall CI/CD', 'Automation'),

(24, 'schneider.it@frauas.de', 'E-124', 'Paul', 'Schneider', 1, 'Munich', 'FULL_TIME', 9, 35.00, '+49-15510000024',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 2, 4,
 '["Docker", "Kubernetes", "Jenkins"]'::jsonb, 40, 4.4, 'Cloud Platform', 'DevOps'),

-- Backend / Java (helps request 1: Java+AWS onsite Munich 20h; request 1 is onsite+20h so we add PART_TIME in Munich)
(25, 'lang.it@frauas.de', 'E-125', 'Sven', 'Lang', 1, 'Munich', 'PART_TIME', 6, 34.00, '+49-15510000025',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 1, 4,
 '["Java", "AWS", "Spring"]'::jsonb, 20, 4.1, 'Skyfall Backend', 'Coding'),

-- Frontend (helps request 8: React+TypeScript remote 20h)
(26, 'hoff.it@frauas.de', 'E-126', 'Mara', 'Hoff', 1, 'Darmstadt', 'PART_TIME', 4, 28.00, '+49-15510000026',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 2, 4,
 '["React", "TypeScript", "UI Testing"]'::jsonb, 20, 3.8, 'NeuralCare UI', 'Design'),

-- Integration (helps request 10: API+JSON remote 40h)
(27, 'brandt.it@frauas.de', 'E-127', 'Tim', 'Brandt', 1, 'Frankfurt', 'FULL_TIME', 5, 33.00, '+49-15510000027',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 2, 4,
 '["API", "JSON", "PostgreSQL"]'::jsonb, 40, 4.0, 'Systems Integration', 'Integration'),

-- R&D / AI (helps request 14: NLP+Transformers remote 20h; request 11 Python+PyTorch onsite Darmstadt 40h)
(28, 'richter.rd2@frauas.de', 'E-128', 'Klara', 'Richter', 1, 'Darmstadt', 'PART_TIME', 4, 29.00, '+49-15510000028',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 6, 4,
 '["NLP", "Transformers", "Python"]'::jsonb, 20, 4.2, 'NeuralCare NLP', 'AI'),

(29, 'seidel.rd@frauas.de', 'E-129', 'Armin', 'Seidel', 1, 'Darmstadt', 'FULL_TIME', 5, 25.00, '+49-15510000029',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 5, 4,
 '["Python", "PyTorch", "Machine Learning"]'::jsonb, 40, 4.0, 'Model Training', 'AI'),

-- Data / Stats (helps request 20: Statistics+SAS remote 40h; request 25 Excel+Data Analysis onsite Frankfurt 40h)
(30, 'wirth.rd@frauas.de', 'E-130', 'Tobias', 'Wirth', 1, 'Berlin', 'FULL_TIME', 6, 27.00, '+49-15510000030',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 6, 4,
 '["Statistics", "SAS", "Data Analytics"]'::jsonb, 40, 4.1, 'Data Integrity', 'Data'),

(31, 'konig.hr@frauas.de', 'E-131', 'Julia', 'Konig', 1, 'Frankfurt', 'FULL_TIME', 4, 22.00, '+49-15510000031',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 11, 4,
 '["Excel", "Data Analysis", "Benchmarking"]'::jsonb, 40, 3.9, 'Comp Analysis', 'Finance'),

-- HR (helps request 22 Recruiting+Sourcing remote 40h; request 21 API Integration + HRIS remote 40h)
(32, 'reuter.hr@frauas.de', 'E-132', 'Ben', 'Reuter', 1, 'Munich', 'FULL_TIME', 5, 28.00, '+49-15510000032',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 9, 4,
 '["Recruiting", "Sourcing", "LinkedIn"]'::jsonb, 40, 4.1, 'Hiring Drive', 'People'),

(33, 'maas.hr@frauas.de', 'E-133', 'Lea', 'Maas', 1, 'Berlin', 'FULL_TIME', 4, 31.00, '+49-15510000033',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 9, 4,
 '["API Integration", "HRIS", "JSON"]'::jsonb, 40, 3.8, 'Payroll Sync', 'HR Tech'),

-- Extra Security/Privacy (helps request 29 GDPR+Privacy onsite Frankfurt 40h by having Frankfurt-based privacy skill)
(34, 'falk.hr@frauas.de', 'E-134', 'Nora', 'Falk', 1, 'Frankfurt', 'FULL_TIME', 5, 30.00, '+49-15510000034',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 4, 10, 4,
 '["GDPR", "Privacy", "Risk Analysis"]'::jsonb, 40, 4.0, 'Compliance', 'Privacy')

ON CONFLICT (id) DO NOTHING;

UPDATE employees SET supervisor_id = 1 WHERE id IN (2, 3, 4, 5, 6, 7);
--------------------------------------------------
-- EMPLOYEE LANGUAGES (employee_languages join table)
--------------------------------------------------

-- Everyone knows English
INSERT INTO employee_languages (employee_id, language_id, proficiency_level) VALUES
(1, 1, 'C2'),
(2, 1, 'C1'),
(3, 1, 'C1'),
(4, 1, 'C1'),
(5, 1, 'C1'),
(6, 1, 'C1'),
(7, 1, 'C1'),
(8, 1, 'C1'),
(9, 1, 'B2'),
(10, 1, 'C1'),
(11, 1, 'B2'),
(12, 1, 'C1'),
(13, 1, 'B2'),
(14, 1, 'C1'),
(15, 1, 'B2'),
(16, 1, 'C1'),
(17, 1, 'B2'),
(18, 1, 'C1'),
(19, 1, 'B2'),
(20, 1, 'B2'),
(21, 1, 'B2'),
(22, 1, 'C1'),
(23, 1, 'B2'),
(24, 1, 'C1'),
(25, 1, 'C1'),
(26, 1, 'B2'),
(27, 1, 'B2'),
(28, 1, 'C1'),
(29, 1, 'C1'),
(30, 1, 'B2'),
(31, 1, 'B2'),
(32, 1, 'C1'),
(33, 1, 'B2'),
(34, 1, 'B2')
ON CONFLICT DO NOTHING;

-- Many employees also know German
INSERT INTO employee_languages (employee_id, language_id, proficiency_level) VALUES
(1, 2, 'C2'),
(2, 2, 'C2'),
(3, 2, 'B2'),
(4, 2, 'B2'),
(5, 2, 'B2'),
(6, 2, 'C1'),
(7, 2, 'B2'),
(8, 2, 'B2'),
(9, 2, 'B1'),
(10, 2, 'B2'),
(11, 2, 'B1'),
(12, 2, 'B2'),
(13, 2, 'B1'),
(14, 2, 'B2'),
(15, 2, 'B1'),
(16, 2, 'B2'),
(17, 2, 'B1'),
(18, 2, 'B2'),
(19, 2, 'B1'),
(20,2,'B2'),(21,2,'B1'),(22,2,'B2'),(23,2,'B1'),(24,2,'B2'),
(25,2,'B2'),(26,2,'B1'),(27,2,'B1'),(28,2,'B2'),(29,2,'B2'),
(30,2,'B1'),(31,2,'B1'),(32,2,'B2'),(33,2,'B1'),(34,2,'B2')
ON CONFLICT DO NOTHING;

-- Some employees also know Spanish
INSERT INTO employee_languages (employee_id, language_id, proficiency_level) VALUES
(3, 3, 'B1'),
(9, 3, 'A2'),
(12, 3, 'B1'),
(18, 3, 'A2'),
(22, 3, 'A2'),
(25, 3, 'B1'),
(28, 3, 'A2'),
(31, 3, 'B1'),
(34, 3, 'A2')
ON CONFLICT DO NOTHING;



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
(19, 'neumann.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 19),
(20, 'meier.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 20),
(21, 'zimmer.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 21),
(22, 'hartmann.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 22),
(23, 'kruger.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 23),
(24, 'schneider.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 24),
(25, 'lang.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 25),
(26, 'hoff.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 26),
(27, 'brandt.it@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 27),
(28, 'richter.rd2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 28),
(29, 'seidel.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 29),
(30, 'wirth.rd@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 30),
(31, 'konig.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 31),
(32, 'reuter.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 32),
(33, 'maas.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 33),
(34, 'falk.hr@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 34)
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
SELECT id, 4 FROM users WHERE id BETWEEN 8 AND 34
ON CONFLICT DO NOTHING;



INSERT INTO staffing_requests (
    request_id, title, description, project_id, project_name, project_location, 
    department_id, job_role_id, status, created_by_employee_id, wage_per_hour, 
    required_skills, experience_years, availability_hours_per_week,
    work_location, project_start_date, project_end_date, project_context
) VALUES 
--------------------------------------------------------------------------------
-- BATCH 1: INFORMATION TECHNOLOGY
--------------------------------------------------------------------------------
(1, 'Backend Java Expert', 'Cloud Migration help.', 1, 'Project Skyfall: AWS Migration', 'Munich', 1, 1, 'APPROVED', 1, 39.50, '["Java", "AWS"]'::jsonb, 5, 20, 'Onsite', '2026-02-01', '2026-12-31', 'Migration of core banking services'),
(2, 'IT Support Specialist', 'General infrastructure setup.', 1, 'Project Skyfall: AWS Migration', 'Munich', 1, 1, 'APPROVED', 1, 30.25, '["Networking", "Linux"]'::jsonb, 3, 40, 'Remote', '2026-02-01', '2026-06-01', 'Internal infra support'),
(3, 'Cloud Architect', 'AWS Migration lead.', 1, 'Project Skyfall: AWS Migration', 'Munich', 2, 8, 'APPROVED', 1, 35.50, '["AWS", "Terraform"]'::jsonb, 8, 40, 'Onsite', '2026-03-01', '2026-12-31', 'Architecture design'),
(4, 'DevOps Specialist', 'CI/CD pipeline automation.', 1, 'Project Skyfall: AWS Migration', 'Munich', 2, 3, 'APPROVED', 1, 25.75, '["Jenkins", "Docker"]'::jsonb, 4, 20, 'Remote', '2026-03-01', '2026-12-31', 'Automated deployment workflows'),
(5, 'Network Security Eng', 'Firewall configuration.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 3, 7, 'APPROVED', 1, 38.00, '["Cisco", "Security"]'::jsonb, 5, 40, 'Remote', '2026-01-15', '2026-08-15', 'Security hardening'),
(6, 'Database Admin', 'Legacy ERP data migration.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 3, 1, 'PENDING_APPROVAL', 1, 32.50, '["PostgreSQL", "SQL"]'::jsonb, 6, 40, 'Onsite', '2026-04-01', '2026-12-01', 'Data archiving project'),
(7, 'Junior IT Analyst', 'Help desk tier 1 support.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 4, 1, 'PENDING_APPROVAL', 1, 25.00, '["Troubleshooting"]'::jsonb, 1, 20, 'Onsite', '2026-05-01', '2026-12-01', 'Employee portal rollout'),
(8, 'Frontend React Help', 'Portal UI support.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 1, 1, 'PENDING_APPROVAL', 1, 35.80, '["React", "TypeScript"]'::jsonb, 2, 20, 'Remote', '2026-03-15', '2026-09-15', 'AI Interface development'),
(9, 'IT Project Coordinator', 'Managing hardware rollout.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 4, 2, 'PENDING_APPROVAL', 1, 38.00, '["Agile", "Planning"]'::jsonb, 3, 40, 'Remote', '2026-05-01', '2026-12-01', 'Mobile device management'),
(10, 'Systems Integrator', 'Middleware connectivity.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 2, 1, 'PENDING_APPROVAL', 1, 40.00, '["API", "JSON"]'::jsonb, 4, 40, 'Remote', '2026-03-01', '2026-10-01', 'System communication bridge'),

--------------------------------------------------------------------------------
-- BATCH 2: RESEARCH & DEVELOPMENT
--------------------------------------------------------------------------------
(11, 'AI Model Trainer', 'Fine-tuning LLMs.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 5, 5, 'APPROVED', 1, 20.50, '["Python", "PyTorch"]'::jsonb, 3, 40, 'Onsite', '2026-04-01', '2026-10-31', 'Generative AI training'),
(12, 'Quantum Research Lead', 'Long-term crypto research.', 1, 'Project Skyfall: AWS Migration', 'Munich', 5, 1, 'APPROVED', 1, 25.00, '["Quantum Computing"]'::jsonb, 10, 20, 'Onsite', '2026-06-01', '2026-12-31', 'Advanced security research'),
(13, 'Data Scientist', 'AI Portal predictive analytics.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 6, 5, 'APPROVED', 1, 22.25, '["R", "SQL", "Math"]'::jsonb, 5, 40, 'Onsite', '2026-03-15', '2026-12-15', 'Predictive modeling'),
(14, 'NLP Specialist', 'Natural language processing.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 6, 5, 'APPROVED', 1, 28.00, '["NLP", "Transformers"]'::jsonb, 4, 20, 'Remote', '2026-03-15', '2026-12-15', 'Text processing module'),
(15, 'Hardware Architect', 'New server prototype design.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 7, 1, 'APPROVED', 1, 30.50, '["VHDL", "FPGA"]'::jsonb, 7, 40, 'Onsite', '2026-05-01', '2026-12-01', 'Server decommissioning hardware analysis'),
(16, 'R&D Lab Assistant', 'Setting up test environments.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 7, 4, 'PENDING_APPROVAL', 1, 28.00, '["Lab Safety", "Testing"]'::jsonb, 2, 40, 'Remote', '2026-05-01', '2026-12-01', 'Physical environment setup'),
(17, 'Graphics Engineer', 'UI optimization for Mobile Alpha.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 8, 3, 'PENDING_APPROVAL', 1, 35.40, '["Metal", "OpenGL"]'::jsonb, 4, 40, 'Onsite', '2026-02-01', '2026-11-01', 'Mobile rendering engine'),
(18, 'Algorithm Engineer', 'Search algorithm optimization.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 8, 1, 'PENDING_APPROVAL', 1, 38.00, '["Algorithms", "C++"]'::jsonb, 5, 40, 'Onsite', '2026-03-01', '2026-12-01', 'Core search logic'),
(19, 'Machine Learning Eng', 'Training computer vision models.', 1, 'Project Skyfall: AWS Migration', 'Munich', 5, 5, 'PENDING_APPROVAL', 1, 35.60, '["PyTorch", "OpenCV"]'::jsonb, 4, 20, 'Onsite', '2026-04-01', '2026-12-31', 'Vision-based security'),
(20, 'Senior Statistician', 'Data validation for R&D.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 6, 5, 'PENDING_APPROVAL', 1, 27.00, '["Statistics", "SAS"]'::jsonb, 6, 40, 'Remote', '2026-04-01', '2026-12-01', 'Data integrity audit'),

--------------------------------------------------------------------------------
-- BATCH 3: HUMAN RESOURCE
--------------------------------------------------------------------------------
(21, 'HR System Specialist', 'Payroll API integration.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 9, 1, 'APPROVED', 1, 38.50, '["API Integration", "HRIS"]'::jsonb, 4, 40, 'Remote', '2026-01-10', '2026-06-30', 'Internal payroll sync'),
(22, 'Recruitment Lead', 'Tech hiring drive 2026.', 1, 'Project Skyfall: AWS Migration', 'Munich', 9, 1, 'APPROVED', 1, 35.00, '["Recruiting", "Sourcing"]'::jsonb, 5, 40, 'Remote', '2026-02-01', '2026-12-31', 'Cloud team expansion'),
(23, 'Employee Relations', 'Conflict resolution for remote staff.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 10, 1, 'APPROVED', 1, 32.40, '["Mediation", "Law"]'::jsonb, 4, 40, 'Onsite', '2026-03-15', '2026-09-15', 'Remote workforce culture'),
(24, 'Training Coordinator', 'Upskilling for Cloud team.', 1, 'Project Skyfall: AWS Migration', 'Munich', 10, 1, 'APPROVED', 1, 30.00, '["LMS", "Teaching"]'::jsonb, 3, 40, 'Onsite', '2026-03-01', '2026-12-31', 'Staff training initiative'),
(25, 'Compensation Analyst', 'Salary benchmarking project.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 11, 5, 'APPROVED', 1, 20.50, '["Excel", "Data Analysis"]'::jsonb, 4, 40, 'Onsite', '2026-05-01', '2026-12-01', 'Budget sunset analysis'),
(26, 'Benefits Specialist', 'New health insurance rollout.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 11, 1, 'PENDING_APPROVAL', 1, 34.00, '["Benefits", "Insurance"]'::jsonb, 3, 40, 'Remote', '2026-06-01', '2026-12-31', 'Contract migration'),
(27, 'HR Generalist', 'Administrative support for Mobile.', 4, 'WorkForce Go: Mobile Alpha', 'Berlin', 12, 1, 'PENDING_APPROVAL', 1, 26.50, '["Admin", "Filing"]'::jsonb, 1, 40, 'Remote', '2026-02-01', '2026-11-01', 'Administrative support'),
(28, 'Diversity & Inclusion', 'Internal D&I workshop lead.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 12, 1, 'PENDING_APPROVAL', 1, 40.00, '["Strategy", "Workshops"]'::jsonb, 6, 20, 'Onsite', '2026-04-01', '2026-10-01', 'Company-wide workshops'),
(29, 'HR Data Privacy Officer', 'GDPR compliance audit.', 3, 'Legacy ERP Sunset Phase 1', 'Frankfurt', 10, 7, 'PENDING_APPROVAL', 1, 20.75, '["GDPR", "Privacy"]'::jsonb, 5, 40, 'Onsite', '2026-04-01', '2026-12-31', 'Compliance and data safety'),
(30, 'Talent Acquisition', 'Sourcing UI/UX designers.', 2, 'NeuralCare: AI Support Portal', 'Darmstadt', 9, 1, 'PENDING_APPROVAL', 1, 33.00, '["Sourcing", "LinkedIn"]'::jsonb, 2, 40, 'Onsite', '2026-03-15', '2026-12-15', 'UI/UX team hiring')

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