--------------------------------------------------
-- 1) LOOKUP TABLES
--------------------------------------------------


INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_RESOURCE_PLNR'), (2, 'ROLE_DEPT_HEAD'), (3, 'ROLE_MANAGER'), (4, 'ROLE_EMPLOYEE') 
ON CONFLICT (id) DO NOTHING;


INSERT INTO languages (id, name) VALUES 
(1, 'English'), (2, 'German'), (3, 'Spanish') 
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, description, status, start_date, end_date, location, manager_user_id) VALUES 
(1, 'Project Skyfall: AWS Migration', 'Migrating core on-premise banking services to a multi-region AWS architecture for high availability.', 'Active', '2026-01-01', '2032-12-31', 'Munich', NULL),
(2, 'NeuralCare: AI Support Portal', 'Development of a Generative AI-driven customer service interface to automate Tier-1 technical support.', 'Active', '2026-03-01', '2033-02-28', 'Darmstadt', NULL),
(3, 'Legacy ERP Sunset Phase 1', 'Final data archiving and decommissioning of the SAP R/3 legacy environment and physical server hardware.', 'Active', '2025-01-01', '2034-11-30', 'Frankfurt', NULL),
(4, 'WorkForce Go: Mobile Alpha', 'Internal testing and rollout of the React Native employee portal for mobile expense and shift management.', 'Active', '2026-05-01', '2032-12-01', 'Berlin', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO departments (id, name) VALUES 
(1, 'IT Backend'), (2, 'IT Frontend'), (3, 'Finance & Management')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (
    id, email, employee_id, first_name, last_name, 
    supervisor_id, primary_location, contract_type, 
    experience_years, wage_per_hour, emergency_contact, 
    availability_start, availability_end, matching_availability,
    department_id, default_role_id, 
    skills, total_hours_per_week, 
    performance_rating, project_preferences, interests
) VALUES 
-- 1. Manager for all projects
(1, 'alice@frauas.de', 'E-101', 'Alice', 'Schmidt', NULL, 'Frankfurt', 'FULL_TIME', 15, 39.90, '+49-15510440423', '2026-01-01', '2030-12-31', 'AVAILABLE', 3 , 3, 
'["Project Management", "Agile", "Scrum", "Stakeholder Management", "Resource Allocation", "Jira", "Confluence", "ERP Systems"]'::jsonb
, 40, 4.6, 'All Projects', 'Leadership'),

-- 2. Department Heads
(2, 'bob@frauas.de', 'E-102', 'Bob', 'Müller', 1, 'Frankfurt', 'FULL_TIME', 12, 39.50, '+49-15510440428', '2026-01-01', '2030-12-31', 'AVAILABLE', 1, 2, 
'["IT Strategy", "Java", "Spring Boot", "Camunda", "PostgreSQL", "Docker", "Kubernetes", "AWS", "CI/CD"]'::jsonb, 40, 4.2, 'IT Infrastructure', 'Tech'),
(3, 'charlie@frauas.de', 'E-103', 'Charlie', 'Wagner', 1, 'Munich', 'FULL_TIME', 14, 39.50, '+49-15510440476', '2026-01-01', '2030-12-31', 'AVAILABLE', 2, 2, 
'["Frontend Architecture", "React.js", "TypeScript", "Redux", "Material UI", "Figma", "Agile", "Stakeholder Management"]'::jsonb,
 40, 3.9, 'R&D Innovation', 'Research'),
(4, 'diana@frauas.de', 'E-104', 'Diana', 'Prince', 1, 'Berlin', 'FULL_TIME', 10, 32.00, '+49-15510440873', '2026-01-01', '2030-12-31', 'AVAILABLE', 3, 2, 
'["HR Strategy", "Budgeting & Forecasting", "Financial Reporting", "Resource Allocation", "Project Management", "Agile", "Stakeholder Management", "SAP"]'::jsonb,
 40, 4.1, 'HR Strategy', 'People'),

-- 3. Resource Planners
(5, 'eve@frauas.de', 'E-105', 'Eve', 'Curie', 1, 'Berlin', 'FULL_TIME', 8, 29.00, '+49-15510440142', '2026-01-01', '2030-12-31', 'AVAILABLE', 1, 1, 
'["Resource Allocation", "Capacity Planning", "Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes", "CI/CD"]'::jsonb,
 40, 3.7, 'IT Planning', 'Organization'),
(6, 'frank@frauas.de', 'E-106', 'Frank', 'Castle', 1, 'Frankfurt', 'FULL_TIME', 9, 36.00, '+49-15510440408', '2026-01-01', '2030-12-31', 'AVAILABLE', 2, 1, 
'["Resource Allocation", "Capacity Planning", "React.js", "TypeScript", "Agile", "Scrum", "Jira"]'::jsonb, 40, 4.4, 'R&D Scheduling', 'Defense'),
(7, 'hopper@frauas.de', 'E-107', 'Grace', 'Hopper', 1, 'Darmstadt', 'FULL_TIME', 20, 38.50, '+49-15510440919', '2026-01-01', '2030-12-31', 'AVAILABLE', 3, 1, 
'["Resource Allocation", "Capacity Planning", "Budgeting & Forecasting", "Payroll Administration", "Microsoft Excel (Advanced)", "Stakeholder Management"]'::jsonb,
 40, 3.8, 'HR Logistics', 'Coding'),

-- 8-11: Backend Specialists
(8, 'wagner@frauas.de', 'E-108', 'Marcus', 'Wagner', 1, 'Frankfurt', 'FULL_TIME', 10, 38.00, '+49-15510440409', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4, 
'["Java","Spring Boot","REST API","PostgreSQL","Docker"]'::jsonb
, 40, 4.0, 'Cloud Migration', 'Gaming'), -- FULL TIME

(9, 'fischer@frauas.de', 'E-109', 'Elena', 'Fischer', 1, 'Frankfurt', 'PART_TIME', 7, 27.00, '+49-15510440451', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4, 
'["Java","Spring Boot","PostgreSQL","Camunda","CI/CD"]'::jsonb
, 20, 3.6, 'Frontend UI', 'Art'), -- PART TIME

(10, 'weber@frauas.de', 'E-110', 'Thomas', 'Weber', 1, 'Frankfurt', 'FULL_TIME', 8, 29.00, '+49-15510440003', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4, 
'["Java","Spring Boot","Kafka","SQL","Docker"]'::jsonb
, 40, 4.3, 'DevOps', 'Cycling'), -- FULL TIME

(11, 'becker@frauas.de', 'E-111', 'Sarah', 'Becker', 1, 'Berlin', 'PART_TIME', 5, 26.00, '+49-15510440411', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4, 
'["Java","Spring Boot","MySQL","REST API","Kubernetes"]'::jsonb
, 20, 3.5, 'QA Automation', 'Swimming'), -- PART TIME

-- 12-15: Frontend Specialists
(12, 'hoffmann@frauas.de', 'E-112', 'Lukas', 'Hoffmann', 1, 'Darmstadt', 'FULL_TIME', 6, 29.00, '+49-15510443654', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4, 
'["React.js","TypeScript","HTML5","CSS","Material UI"]'::jsonb
, 40, 4.1, 'AI Models', 'Chess'),

(13, 'schulz@frauas.de', 'E-113', 'Miriam', 'Schulz', 1, 'Frankfurt', 'PART_TIME', 4, 32.00, '+49-15510440467', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4, 
'["React.js","JavaScript","Redux","Axios","Jest"]'::jsonb
, 20, 3.9, 'Data Analysis', 'Reading'),

(14, 'koch@frauas.de', 'E-114', 'Julian', 'Koch', 1, 'Munich', 'FULL_TIME', 5, 36.00, '+49-15510440711', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4, 
'["React.js","TypeScript","Redux","Material UI","Axios"]'::jsonb
, 40, 4.5, 'Privacy Compliance', 'Running'),

(15, 'bauer@frauas.de', 'E-115', 'Sophia', 'Bauer', 1, 'Frankfurt', 'PART_TIME', 6, 35.50, '+49-15510440486', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4, 
'["React.js","TypeScript","Context API","HTML5","CSS"]'::jsonb
, 20, 3.4, 'Core Logic', 'Hiking'),

-- 16-19: Finance and Management Specialists
(16, 'richter@frauas.de', 'E-116', 'David', 'Richter', 1, 'Frankfurt', 'FULL_TIME', 3, 21.00, '+49-15510440216', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4, 
'["Budgeting & Forecasting","Financial Reporting","Microsoft Excel (Advanced)","Profit & Loss Analysis","SAP"]'::jsonb
, 40, 4.0, 'Talent Acquisition', 'Cooking'),

(17, 'wolf@frauas.de', 'E-117', 'Hannah', 'Wolf', 1, 'Berlin', 'PART_TIME', 2, 28.00, '+49-15510440931', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4, 
'["Talent Acquisition","HR","Stakeholder Management","Payroll Administration","ERP Systems"]'::jsonb
, 20, 3.8, 'IT Support', 'Music'),

(18, 'klein@frauas.de', 'E-118', 'Simon', 'Klein', 1, 'Berlin', 'FULL_TIME', 4, 33.00, '+49-15510440721', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4, 
'["Resource Allocation","Project Management","Agile","Stakeholder Management","Microsoft Excel (Advanced)"]'::jsonb
, 40, 4.2, 'Benchmarking', 'Gardening'),

(19, 'neumann@frauas.de', 'E-119', 'Laura', 'Neumann', 1, 'Darmstadt', 'PART_TIME', 3, 34.00, '+49-15510440240', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4, 
'["Project Management","Agile","Scrum","Jira","Confluence"]'::jsonb
, 20, 3.6, 'HR Operations', 'Yoga'),

-- IT / Infra (helps request 2: Networking+Linux, request 7: Troubleshooting, request 5: Cisco+Security)
(20, 'meier@frauas.de', 'E-120', 'Jonas', 'Meier', 1, 'Munich', 'FULL_TIME', 6, 30.00, '+49-15510000020',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Java","Spring Boot","AWS","Docker","CI/CD"]'::jsonb
, 40, 4.0, 'Skyfall Infra', 'Systems'),

(21, 'zimmer@frauas.de', 'E-121', 'Nina', 'Zimmer', 1, 'Berlin', 'PART_TIME', 3, 24.00, '+49-15510000021',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Java","Spring Boot","Azure","PostgreSQL","CI/CD"]'::jsonb
, 20, 3.7, 'Mobile Support', 'Music'),

(22, 'hartmann@frauas.de', 'E-122', 'Felix', 'Hartmann', 1, 'Frankfurt', 'FULL_TIME', 7, 37.00, '+49-15510000022',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Java","Spring Boot","Flowable","REST API","PostgreSQL"]'::jsonb
, 40, 4.2, 'Legacy Security', 'Security'),

(23, 'kruger@frauas.de', 'E-123', 'Lena', 'Kruger', 1, 'Frankfurt', 'PART_TIME', 4, 26.00, '+49-15510000023',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Jenkins", "Docker", "CI/CD"]'::jsonb, 20, 3.9, 'Skyfall CI/CD', 'Automation'),

(24, 'schneider@frauas.de', 'E-124', 'Paul', 'Schneider', 1, 'Munich', 'FULL_TIME', 9, 35.00, '+49-15510000024',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
'["React.js","TypeScript","Redux","Jest","Material UI"]'::jsonb
, 40, 4.4, 'Cloud Platform', 'DevOps'),

(25, 'lang@frauas.de', 'E-125', 'Sven', 'Lang', 1, 'Munich', 'PART_TIME', 6, 34.00, '+49-15510000025',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
'["Project Management","Agile","Scrum","Stakeholder Management","Resource Allocation"]'::jsonb
, 20, 4.1, 'Skyfall Backend', 'Coding'),

(26, 'hoff@frauas.de', 'E-126', 'Mara', 'Hoff', 1, 'Darmstadt', 'PART_TIME', 4, 28.00, '+49-15510000026',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Python","Django","PostgreSQL","REST API","Docker"]'::jsonb
, 20, 3.8, 'NeuralCare UI', 'Design'),

(27, 'brandt@frauas.de', 'E-127', 'Tim', 'Brandt', 1, 'Frankfurt', 'FULL_TIME', 5, 33.00, '+49-15510000027',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
'["React.js","TypeScript","Redux","Figma","Material UI"]'::jsonb
, 40, 4.0, 'Systems Integration', 'Integration'),

(28, 'richter2@frauas.de', 'E-128', 'Klara', 'Richter', 1, 'Darmstadt', 'PART_TIME', 4, 29.00, '+49-15510000028',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
'["Project Management","Agile","Scrum","Stakeholder Management","Resource Allocation"]'::jsonb
, 20, 4.2, 'NeuralCare NLP', 'AI'),

(29, 'seidel@frauas.de', 'E-129', 'Armin', 'Seidel', 1, 'Darmstadt', 'FULL_TIME', 5, 25.00, '+49-15510000029',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Java","Spring Boot","Camunda","Kafka","PostgreSQL"]'::jsonb
, 40, 4.0, 'Model Training', 'AI'),

(30, 'wirth@frauas.de', 'E-130', 'Tobias', 'Wirth', 1, 'Berlin', 'FULL_TIME', 6, 27.00, '+49-15510000030',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
'["React.js","TypeScript","Context API","Material UI","Figma"]'::jsonb
, 40, 4.1, 'Data Integrity', 'Data'),

(31, 'konig@frauas.de', 'E-131', 'Julia', 'Konig', 1, 'Frankfurt', 'FULL_TIME', 4, 22.00, '+49-15510000031',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
'["Resource Allocation","Capacity Planning","Project Management","Microsoft Excel (Advanced)","Agile"]'::jsonb
, 40, 3.9, 'Comp Analysis', 'Finance'),

(32, 'reuter@frauas.de', 'E-132', 'Ben', 'Reuter', 1, 'Munich', 'FULL_TIME', 5, 28.00, '+49-15510000032',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
'["Docker","Kubernetes","CI/CD","AWS","Azure"]'::jsonb
, 40, 4.1, 'Hiring Drive', 'People'),

(33, 'maas@frauas.de', 'E-133', 'Lea', 'Maas', 1, 'Berlin', 'FULL_TIME', 4, 31.00, '+49-15510000033',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
'["React.js",".NET","REST API","Axios","TypeScript"]'::jsonb
, 40, 3.8, 'Payroll Sync', 'HR Tech'),

(34, 'falk@frauas.de', 'E-134', 'Nora', 'Falk', 1, 'Frankfurt', 'FULL_TIME', 5, 30.00, '+49-15510000034',
 '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
'["HR","Employee Relations","Conflict Resolution","Stakeholder Management","Compliance"]'::jsonb
, 40, 4.0, 'Compliance', 'Privacy'),

--------------------------------------------------
-- NEW: 60 MORE EMPLOYEES (20 per department)
-- Employees: 35-94
-- Users:     35-94
-- Roles:     (user_id, 4)
--------------------------------------------------
-- =========================
-- DEPT 1: IT BACKEND (20)
-- =========================
(35, 'noah.bauer@frauas.de', 'E-135', 'Noah', 'Bauer', 1, 'Hamburg', 'FULL_TIME', 6, 33.50, '+49-15520010035', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","REST API","PostgreSQL","Docker","CI/CD"]'::jsonb, 40, 4.1, 'Backend Services', 'Cycling'),
(36, 'mia.keller@frauas.de', 'E-136', 'Mia', 'Keller', 1, 'Dusseldorf', 'PART_TIME', 4, 27.50, '+49-15520010036', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","MySQL","SQL","Kafka"]'::jsonb, 20, 3.7, 'Messaging', 'Cooking'),
(37, 'liam.fischer@frauas.de', 'E-137', 'Liam', 'Fischer', 1, 'Frankfurt', 'FULL_TIME', 7, 35.00, '+49-15520010037', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Camunda","PostgreSQL","REST API"]'::jsonb, 40, 4.3, 'Workflow Automation', 'Chess'),
(38, 'emma.hoffmann@frauas.de', 'E-138', 'Emma', 'Hoffmann', 1, 'Berlin', 'PART_TIME', 3, 26.00, '+49-15520010038', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","FastAPI","REST API","PostgreSQL","Docker"]'::jsonb, 20, 3.6, 'API Development', 'Reading'),
(39, 'ben.schneider@frauas.de', 'E-139', 'Ben', 'Schneider', 1, 'Darmstadt', 'FULL_TIME', 8, 37.00, '+49-15520010039', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Kubernetes","Docker","CI/CD"]'::jsonb, 40, 4.4, 'Platform Engineering', 'Hiking'),
(40, 'sophia.wagner@frauas.de', 'E-140', 'Sophia', 'Wagner', 1, 'Hamburg', 'FULL_TIME', 5, 32.00, '+49-15520010040', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Flowable","SQL","PostgreSQL"]'::jsonb, 40, 4.0, 'Process Apps', 'Music'),
(41, 'lucas.meier@frauas.de', 'E-141', 'Lucas', 'Meier', 1, 'Dusseldorf', 'PART_TIME', 2, 24.50, '+49-15520010041', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","Django","PostgreSQL","REST API","CI/CD"]'::jsonb, 20, 3.5, 'Web Backend', 'Gaming'),
(42, 'amelia.zimmer@frauas.de', 'E-142', 'Amelia', 'Zimmer', 1, 'Frankfurt', 'FULL_TIME', 9, 38.50, '+49-15520010042', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Kafka","PostgreSQL","Docker"]'::jsonb, 40, 4.5, 'Event Streaming', 'Running'),
(43, 'ethan.lang@frauas.de', 'E-143', 'Ethan', 'Lang', 1, 'Berlin', 'PART_TIME', 4, 28.00, '+49-15520010043', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","REST API","MySQL","Docker"]'::jsonb, 20, 3.8, 'Microservices', 'Photography'),
(44, 'ava.konig@frauas.de', 'E-144', 'Ava', 'Konig', 1, 'Darmstadt', 'FULL_TIME', 6, 34.00, '+49-15520010044', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","PostgreSQL","SQL","AWS"]'::jsonb, 40, 4.2, 'Cloud Backend', 'Travel'),
(45, 'oliver.neumann@frauas.de', 'E-145', 'Oliver', 'Neumann', 1, 'Hamburg', 'PART_TIME', 5, 29.00, '+49-15520010045', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","FastAPI","Kafka","PostgreSQL","Docker"]'::jsonb, 20, 3.9, 'Realtime APIs', 'Cycling'),
(46, 'isabella.kruger@frauas.de', 'E-146', 'Isabella', 'Kruger', 1, 'Dusseldorf', 'FULL_TIME', 10, 39.00, '+49-15520010046', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Kubernetes","AWS","CI/CD"]'::jsonb, 40, 4.6, 'Cloud Platform', 'Skiing'),
(47, 'james.hartmann@frauas.de', 'E-147', 'James', 'Hartmann', 1, 'Frankfurt', 'FULL_TIME', 7, 36.00, '+49-15520010047', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","REST API","PostgreSQL","Camunda"]'::jsonb, 40, 4.3, 'BPM Integration', 'Robotics'),
(48, 'charlotte.becker@frauas.de', 'E-148', 'Charlotte', 'Becker', 1, 'Berlin', 'PART_TIME', 3, 25.50, '+49-15520010048', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","SQL","MySQL","CI/CD"]'::jsonb, 20, 3.6, 'Backend QA', 'Yoga'),
(49, 'henry.schulz@frauas.de', 'E-149', 'Henry', 'Schulz', 1, 'Darmstadt', 'FULL_TIME', 8, 37.50, '+49-15520010049', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","Django","PostgreSQL","Docker","Kubernetes"]'::jsonb, 40, 4.2, 'Scalable Services', 'Hiking'),
(50, 'ella.hoff@frauas.de', 'E-150', 'Ella', 'Hoff', 1, 'Hamburg', 'FULL_TIME', 6, 33.00, '+49-15520010050', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","REST API","PostgreSQL","Azure"]'::jsonb, 40, 4.1, 'Cloud APIs', 'Music'),
(51, 'william.brandt@frauas.de', 'E-151', 'William', 'Brandt', 1, 'Dusseldorf', 'PART_TIME', 4, 28.50, '+49-15520010051', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","Kafka","SQL","CI/CD"]'::jsonb, 20, 3.9, 'Integration', 'Reading'),
(52, 'grace.reuter@frauas.de', 'E-152', 'Grace', 'Reuter', 1, 'Frankfurt', 'FULL_TIME', 9, 38.00, '+49-15520010052', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","FastAPI","PostgreSQL","Docker","AWS"]'::jsonb, 40, 4.4, 'Cloud Services', 'Running'),
(53, 'sebastian.maas@frauas.de', 'E-153', 'Sebastian', 'Maas', 1, 'Berlin', 'FULL_TIME', 5, 31.50, '+49-15520010053', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Java","Spring Boot","PostgreSQL","Docker","Kubernetes"]'::jsonb, 40, 4.0, 'Containers', 'Gaming'),
(54, 'lily.falk@frauas.de', 'E-154', 'Lily', 'Falk', 1, 'Darmstadt', 'PART_TIME', 3, 26.50, '+49-15520010054', '2026-01-01', '2027-01-01', 'AVAILABLE', 1, 4,
 '["Python","Django","REST API","MySQL","Docker"]'::jsonb, 20, 3.7, 'Web Apps', 'Cooking'),
-- =========================
-- DEPT 2: IT FRONTEND (20)
-- =========================
(55, 'leo.meyer@frauas.de', 'E-155', 'Leo', 'Meyer', 1, 'Hamburg', 'FULL_TIME', 5, 31.00, '+49-15520010055', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Material UI","Axios"]'::jsonb, 40, 4.0, 'Frontend UI', 'Design'),
(56, 'nina.weiss@frauas.de', 'E-156', 'Nina', 'Weiss', 1, 'Dusseldorf', 'PART_TIME', 3, 26.00, '+49-15520010056', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","HTML5","CSS","Context API"]'::jsonb, 20, 3.6, 'UI Components', 'Art'),
(57, 'max.schubert@frauas.de', 'E-157', 'Max', 'Schubert', 1, 'Frankfurt', 'FULL_TIME', 6, 33.00, '+49-15520010057', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Jest","Axios"]'::jsonb, 40, 4.2, 'Testing', 'Gaming'),
(58, 'hannah.vogel@frauas.de', 'E-158', 'Hannah', 'Vogel', 1, 'Berlin', 'PART_TIME', 2, 24.00, '+49-15520010058', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","Material UI","HTML5","CSS"]'::jsonb, 20, 3.5, 'UI Delivery', 'Yoga'),
(59, 'felix.hahn@frauas.de', 'E-159', 'Felix', 'Hahn', 1, 'Darmstadt', 'FULL_TIME', 7, 34.50, '+49-15520010059', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Context API","Axios","Figma"]'::jsonb, 40, 4.3, 'Design Systems', 'Photography'),
(60, 'sara.koch@frauas.de', 'E-160', 'Sara', 'Koch', 1, 'Hamburg', 'FULL_TIME', 4, 29.00, '+49-15520010060', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","HTML5","CSS","Jest"]'::jsonb, 40, 3.9, 'Frontend Testing', 'Reading'),
(61, 'jonas.wolf@frauas.de', 'E-161', 'Jonas', 'Wolf', 1, 'Dusseldorf', 'PART_TIME', 3, 26.50, '+49-15520010061', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","Redux","Axios","Material UI"]'::jsonb, 20, 3.7, 'State Mgmt', 'Music'),
(62, 'lara.benz@frauas.de', 'E-162', 'Lara', 'Benz', 1, 'Frankfurt', 'FULL_TIME', 8, 36.00, '+49-15520010062', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Figma","Material UI"]'::jsonb, 40, 4.4, 'UX/UI', 'Travel'),
(63, 'tim.kraus@frauas.de', 'E-163', 'Tim', 'Kraus', 1, 'Berlin', 'PART_TIME', 2, 24.50, '+49-15520010063', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","HTML5","CSS","Axios"]'::jsonb, 20, 3.5, 'Web UI', 'Cooking'),
(64, 'paula.schmitt@frauas.de', 'E-164', 'Paula', 'Schmitt', 1, 'Darmstadt', 'FULL_TIME', 6, 33.50, '+49-15520010064', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Material UI","Context API","Jest"]'::jsonb, 40, 4.1, 'UI Quality', 'Hiking'),
(65, 'daniel.franke@frauas.de', 'E-165', 'Daniel', 'Franke', 1, 'Hamburg', 'FULL_TIME', 5, 32.00, '+49-15520010065', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","HTML5","CSS"]'::jsonb, 40, 4.0, 'Frontend Core', 'Cycling'),
(66, 'marie.huber@frauas.de', 'E-166', 'Marie', 'Huber', 1, 'Dusseldorf', 'PART_TIME', 4, 27.00, '+49-15520010066', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","Context API","Material UI","Figma"]'::jsonb, 20, 3.8, 'Design', 'Art'),
(67, 'julian.winter@frauas.de', 'E-167', 'Julian', 'Winter', 1, 'Frankfurt', 'FULL_TIME', 7, 35.50, '+49-15520010067', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Axios","Jest"]'::jsonb, 40, 4.3, 'Frontend Testing', 'Gaming'),
(68, 'lea.klein@frauas.de', 'E-168', 'Lea', 'Klein', 1, 'Berlin', 'FULL_TIME', 6, 34.00, '+49-15520010068', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Material UI","Redux","Figma"]'::jsonb, 40, 4.2, 'UI Systems', 'Photography'),
(69, 'moritz.sauer@frauas.de', 'E-169', 'Moritz', 'Sauer', 1, 'Darmstadt', 'PART_TIME', 3, 25.50, '+49-15520010069', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","HTML5","CSS","Jest"]'::jsonb, 20, 3.6, 'Component Dev', 'Music'),
(70, 'emilia.peters@frauas.de', 'E-170', 'Emilia', 'Peters', 1, 'Hamburg', 'FULL_TIME', 8, 36.50, '+49-15520010070', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Material UI","Jest"]'::jsonb, 40, 4.5, 'Frontend Lead', 'Travel'),
(71, 'tobias.lang@frauas.de', 'E-171', 'Tobias', 'Lang', 1, 'Dusseldorf', 'FULL_TIME', 5, 32.50, '+49-15520010071', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Context API","Axios","HTML5"]'::jsonb, 40, 4.0, 'App UI', 'Hiking'),
(72, 'anna.simon@frauas.de', 'E-172', 'Anna', 'Simon', 1, 'Frankfurt', 'PART_TIME', 2, 24.00, '+49-15520010072', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","JavaScript","Material UI","CSS","Axios"]'::jsonb, 20, 3.5, 'UI Support', 'Yoga'),
(73, 'philipp.haas@frauas.de', 'E-173', 'Philipp', 'Haas', 1, 'Berlin', 'FULL_TIME', 7, 35.00, '+49-15520010073', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js","TypeScript","Redux","Material UI","Figma"]'::jsonb, 40, 4.2, 'Design Systems', 'Photography'),
(74, 'clara.schwarz@frauas.de', 'E-174', 'Clara', 'Schwarz', 1, 'Darmstadt', 'PART_TIME', 4, 27.50, '+49-15520010074', '2026-01-01', '2027-01-01', 'AVAILABLE', 2, 4,
 '["React.js",".NET","TypeScript","Axios","Material UI"]'::jsonb, 20, 3.9, 'Integration UI', 'Reading'),
-- =========================
-- DEPT 3: FINANCE & MANAGEMENT (20)
-- =========================
(75, 'paul.richter@frauas.de', 'E-175', 'Paul', 'Richter', 1, 'Hamburg', 'FULL_TIME', 6, 32.00, '+49-15520010075', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Project Management","Agile","Scrum","Stakeholder Management","Microsoft Excel (Advanced)"]'::jsonb, 40, 4.1, 'Operations', 'Leadership'),
(76, 'sophie.neumann@frauas.de', 'E-176', 'Sophie', 'Neumann', 1, 'Dusseldorf', 'PART_TIME', 4, 27.00, '+49-15520010076', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Budgeting & Forecasting","Financial Reporting","Profit & Loss Analysis","SAP","ERP Systems"]'::jsonb, 20, 3.8, 'Finance', 'Cooking'),
(77, 'daniel.mayer@frauas.de', 'E-177', 'Daniel', 'Mayer', 1, 'Frankfurt', 'FULL_TIME', 7, 34.00, '+49-15520010077', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Talent Acquisition","HR","Stakeholder Management","Conflict Resolution","Project Management"]'::jsonb, 40, 4.2, 'People Ops', 'Music'),
(78, 'lara.schmidt@frauas.de', 'E-178', 'Lara', 'Schmidt', 1, 'Berlin', 'PART_TIME', 3, 26.50, '+49-15520010078', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Payroll Administration","ERP Systems","Microsoft Excel (Advanced)","Financial Reporting","HR"]'::jsonb, 20, 3.7, 'Payroll', 'Yoga'),
(79, 'felix.hoffmann2@frauas.de', 'E-179', 'Felix', 'Hoffmann', 1, 'Darmstadt', 'FULL_TIME', 8, 36.50, '+49-15520010079', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Resource Allocation","Project Management","Agile","Stakeholder Management","Jira"]'::jsonb, 40, 4.3, 'Workforce Planning', 'Hiking'),
(80, 'marie.wagner2@frauas.de', 'E-180', 'Marie', 'Wagner', 1, 'Hamburg', 'FULL_TIME', 5, 30.50, '+49-15520010080', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Financial Reporting","Budgeting & Forecasting","Microsoft Excel (Advanced)","Profit & Loss Analysis","Stakeholder Management"]'::jsonb, 40, 4.0, 'Reporting', 'Reading'),
(81, 'jonas.keller@frauas.de', 'E-181', 'Jonas', 'Keller', 1, 'Dusseldorf', 'PART_TIME', 2, 24.50, '+49-15520010081', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["HR","Employee Relations","Conflict Resolution","Stakeholder Management","Agile"]'::jsonb, 20, 3.6, 'Employee Relations', 'Travel'),
(82, 'emma.schulz2@frauas.de', 'E-182', 'Emma', 'Schulz', 1, 'Frankfurt', 'FULL_TIME', 9, 37.00, '+49-15520010082', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["SAP","ERP Systems","Budgeting & Forecasting","Financial Reporting","Project Management"]'::jsonb, 40, 4.4, 'ERP Finance', 'Cycling'),
(83, 'ben.becker2@frauas.de', 'E-183', 'Ben', 'Becker', 1, 'Berlin', 'PART_TIME', 4, 27.50, '+49-15520010083', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Talent Acquisition","HR","Project Management","Stakeholder Management","Microsoft Excel (Advanced)"]'::jsonb, 20, 3.9, 'Hiring', 'Cooking'),
(84, 'sara.lang2@frauas.de', 'E-184', 'Sara', 'Lang', 1, 'Darmstadt', 'FULL_TIME', 6, 33.50, '+49-15520010084', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Jira","Confluence","Agile","Scrum","Stakeholder Management"]'::jsonb, 40, 4.1, 'PMO', 'Music'),
(85, 'oliver.huber@frauas.de', 'E-185', 'Oliver', 'Huber', 1, 'Hamburg', 'PART_TIME', 3, 25.50, '+49-15520010085', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Budgeting & Forecasting","Financial Reporting","Microsoft Excel (Advanced)","SAP","Profit & Loss Analysis"]'::jsonb, 20, 3.7, 'Finance Ops', 'Reading'),
(86, 'mia.schneider2@frauas.de', 'E-186', 'Mia', 'Schneider', 1, 'Dusseldorf', 'FULL_TIME', 7, 34.50, '+49-15520010086', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Resource Allocation","Project Management","Agile","Stakeholder Management","Microsoft Excel (Advanced)"]'::jsonb, 40, 4.2, 'Planning', 'Hiking'),
(87, 'liam.konig2@frauas.de', 'E-187', 'Liam', 'Konig', 1, 'Frankfurt', 'FULL_TIME', 5, 31.50, '+49-15520010087', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["HR","Payroll Administration","ERP Systems","Stakeholder Management","Conflict Resolution"]'::jsonb, 40, 4.0, 'HR Ops', 'Yoga'),
(88, 'ava.neumann2@frauas.de', 'E-188', 'Ava', 'Neumann', 1, 'Berlin', 'PART_TIME', 2, 24.00, '+49-15520010088', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Talent Acquisition","HR","Stakeholder Management","Agile","Scrum"]'::jsonb, 20, 3.6, 'Recruiting', 'Travel'),
(89, 'ethan.meier2@frauas.de', 'E-189', 'Ethan', 'Meier', 1, 'Darmstadt', 'FULL_TIME', 8, 36.00, '+49-15520010089', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["SAP","ERP Systems","Financial Reporting","Stakeholder Management","Microsoft Excel (Advanced)"]'::jsonb, 40, 4.3, 'Reporting', 'Cycling'),
(90, 'charlotte.fischer2@frauas.de', 'E-190', 'Charlotte', 'Fischer', 1, 'Hamburg', 'FULL_TIME', 6, 33.00, '+49-15520010090', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Project Management","Stakeholder Management","Agile","Scrum","Confluence"]'::jsonb, 40, 4.1, 'Delivery', 'Music'),
(91, 'henry.wagner2@frauas.de', 'E-191', 'Henry', 'Wagner', 1, 'Dusseldorf', 'PART_TIME', 4, 27.00, '+49-15520010091', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Budgeting & Forecasting","Microsoft Excel (Advanced)","Profit & Loss Analysis","Financial Reporting","Stakeholder Management"]'::jsonb, 20, 3.8, 'Finance', 'Cooking'),
(92, 'isabella.hartmann2@frauas.de', 'E-192', 'Isabella', 'Hartmann', 1, 'Frankfurt', 'FULL_TIME', 9, 37.50, '+49-15520010092', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["HR","Employee Relations","Conflict Resolution","Stakeholder Management","Project Management"]'::jsonb, 40, 4.4, 'People Ops', 'Reading'),
(93, 'james.kruger2@frauas.de', 'E-193', 'James', 'Kruger', 1, 'Berlin', 'FULL_TIME', 7, 35.00, '+49-15520010093', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Resource Allocation","Project Management","Agile","Scrum","Jira"]'::jsonb, 40, 4.2, 'Workforce Planning', 'Hiking'),
(94, 'lily.zimmer2@frauas.de', 'E-194', 'Lily', 'Zimmer', 1, 'Darmstadt', 'PART_TIME', 3, 25.50, '+49-15520010094', '2026-01-01', '2027-01-01', 'AVAILABLE', 3, 4,
 '["Payroll Administration","HR","ERP Systems","Microsoft Excel (Advanced)","Stakeholder Management"]'::jsonb, 20, 3.7, 'HR Admin', 'Yoga')
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
(34, 1, 'B2'),(37, 1, 'A2'),(39, 1, 'A1'),(40, 1, 'C2'),(43, 1, 'C1'),(44, 1, 'C2'),(47, 1, 'A2'),(49, 1, 'B2'),(50, 1, 'B1'),
(54, 1, 'B2'),(55, 1, 'C2'),(56, 1, 'A2'),(59, 1, 'B2'),(65, 1, 'B1'),(64, 1, 'A2'),(62, 1, 'C2'),(69, 1, 'A1'),(68, 1, 'C2'),
(70, 1, 'C1'),(74, 1, 'A2'),(75, 1, 'B2'),(76, 1, 'C1'),(78, 1, 'B2'),(80, 1, 'A2'),(82, 1, 'B2'),(83, 1, 'C2'),(84, 1, 'B2'),
(85, 1, 'C2'),(88, 1, 'A2'),(90, 1, 'C2'),(92, 1, 'B1'),(93, 1, 'A1'),(94, 1, 'C1'),(61, 1, 'A1'),(45, 1, 'C1'),(48, 1, 'B2')
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
(30,2,'B1'),(31,2,'B1'),(32,2,'B2'),(33,2,'B1'),(34,2,'B2'),(36, 1, 'C2'),(39, 1, 'A2'),(40, 1, 'B2'),(42, 1, 'A2'),(44, 1, 'C1'),
(46, 1, 'B1'),(47, 1, 'C2'),(49, 1, 'A1'),(50, 1, 'C1'),(52, 1, 'C2'),(53, 1, 'A1'),(55, 1, 'C2'),(57, 1, 'C2'),(58, 1, 'C1'),
(60, 1, 'C1'),(63, 1, 'B2'),(64, 1, 'C1'),(66, 1, 'B2'),(68, 1, 'A2'),(69, 1, 'B2'),(71, 1, 'A2'),(73, 1, 'C1'),(75, 1, 'B2'),
(76, 1, 'B2'),(78, 1, 'C2'),(79, 1, 'C1'),(81, 1, 'B2'),(83, 1, 'A2'),(84, 1, 'A1'),(86, 1, 'C2'),(87, 1, 'C1'),(88, 1, 'B2'),
(92, 1, 'A2'),(93, 1, 'B2'),(94, 1, 'C2'),(91, 1, 'A1'),(41, 1, 'B2'),(35, 1, 'A1')
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
(34, 3, 'A2'),(37, 1, 'C2'),(39, 1, 'B2'),(43, 1, 'A2'),(44, 1, 'B1'),(49, 1, 'C1'),(54, 1, 'A1'),(57, 1, 'B2'),(59, 1, 'B2'),
(63, 1, 'B2'),(66, 1, 'A2'),(67, 1, 'B2'),(72, 1, 'C2'),(75, 1, 'B2'),(77, 1, 'C1'),(81, 1, 'A2'),(87, 1, 'C2'),(89, 1, 'B1'),
(92, 1, 'C2'),(94, 1, 'A1'),(51, 1, 'A1'),(35, 1, 'B1')
ON CONFLICT DO NOTHING;



INSERT INTO users (id, email, password_hash, employee_id) VALUES 
(1, 'alice@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 1),
(2, 'bob@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 2),
(3, 'charlie@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 3),
(4, 'diana@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 4),
(5, 'eve@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 5),
(6, 'frank@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 6),
-- Auth for Heads
(7, 'hopper@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 7),
(8, 'wagner@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 8),
(9, 'fischer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 9),
(10, 'weber@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 10),
(11, 'becker@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 11),
(12, 'hoffmann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 12),
(13, 'schulz@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 13),
(14, 'koch@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 14),
(15, 'bauer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 15),
(16, 'richter@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 16),
(17, 'wolf@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 17),
(18, 'klein@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 18),
(19, 'neumann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 19),
(20, 'meier@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 20),
(21, 'zimmer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 21),
(22, 'hartmann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 22),
(23, 'kruger@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 23),
(24, 'schneider@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 24),
(25, 'lang@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 25),
(26, 'hoff@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 26),
(27, 'brandt@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 27),
(28, 'richter2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 28),
(29, 'seidel@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 29),
(30, 'wirth@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 30),
(31, 'konig@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 31),
(32, 'reuter@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 32),
(33, 'maas@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 33),
(34, 'falk@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 34)
ON CONFLICT (id) DO NOTHING;



UPDATE projects SET manager_user_id = 1;

INSERT INTO project_departments (project_id, department_id, department_head_user_id, resource_planner_user_id) VALUES 
(1,1,2,5), (1,2,3,6), (1,3,4,7), (2,1,2,5), (2,2,3,6), (2,3,4,7), (3,1,2,5), (3,2,3,6), (3,3,4,7), (4,1,2,5), (4,2,3,6), (4,3,4,7);

-- Assign Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1,3), (2,2), (3,2), (4,2),(5,1), (6,1), (7,1) 
ON CONFLICT DO NOTHING;

-- NEW: Automated role assignment for the 12 unique heads

INSERT INTO user_roles (user_id, role_id)
SELECT id, 4 FROM users WHERE id BETWEEN 8 AND 34
ON CONFLICT DO NOTHING;

--------------------------------------------------
-- USERS FOR NEW EMPLOYEES (IDs 35 - 94)
-- (use same password hash as existing demo users)
--------------------------------------------------
INSERT INTO users (id, email, password_hash, employee_id) VALUES
(35, 'noah.bauer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 35),
(36, 'mia.keller@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 36),
(37, 'liam.fischer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 37),
(38, 'emma.hoffmann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 38),
(39, 'ben.schneider@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 39),
(40, 'sophia.wagner@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 40),
(41, 'lucas.meier@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 41),
(42, 'amelia.zimmer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 42),
(43, 'ethan.lang@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 43),
(44, 'ava.konig@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 44),
(45, 'oliver.neumann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 45),
(46, 'isabella.kruger@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 46),
(47, 'james.hartmann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 47),
(48, 'charlotte.becker@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 48),
(49, 'henry.schulz@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 49),
(50, 'ella.hoff@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 50),
(51, 'william.brandt@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 51),
(52, 'grace.reuter@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 52),
(53, 'sebastian.maas@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 53),
(54, 'lily.falk@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 54),

(55, 'leo.meyer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 55),
(56, 'nina.weiss@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 56),
(57, 'max.schubert@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 57),
(58, 'hannah.vogel@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 58),
(59, 'felix.hahn@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 59),
(60, 'sara.koch@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 60),
(61, 'jonas.wolf@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 61),
(62, 'lara.benz@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 62),
(63, 'tim.kraus@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 63),
(64, 'paula.schmitt@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 64),
(65, 'daniel.franke@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 65),
(66, 'marie.huber@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 66),
(67, 'julian.winter@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 67),
(68, 'lea.klein@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 68),
(69, 'moritz.sauer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 69),
(70, 'emilia.peters@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 70),
(71, 'tobias.lang@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 71),
(72, 'anna.simon@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 72),
(73, 'philipp.haas@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 73),
(74, 'clara.schwarz@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 74),

(75, 'paul.richter@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 75),
(76, 'sophie.neumann@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 76),
(77, 'daniel.mayer@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 77),
(78, 'lara.schmidt@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 78),
(79, 'felix.hoffmann2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 79),
(80, 'marie.wagner2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 80),
(81, 'jonas.keller@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 81),
(82, 'emma.schulz2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 82),
(83, 'ben.becker2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 83),
(84, 'sara.lang2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 84),
(85, 'oliver.huber@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 85),
(86, 'mia.schneider2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 86),
(87, 'liam.konig2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 87),
(88, 'ava.neumann2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 88),
(89, 'ethan.meier2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 89),
(90, 'charlotte.fischer2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 90),
(91, 'henry.wagner2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 91),
(92, 'isabella.hartmann2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 92),
(93, 'james.kruger2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 93),
(94, 'lily.zimmer2@frauas.de', '$2a$10$d2rY0xMlb.VyYqJjYmkF2.Fs62XP9zsUSal40cclJT64ozaB8bCbC', 94)
ON CONFLICT (id) DO NOTHING;

--------------------------------------------------
-- USER_ROLES FOR NEW USERS (ROLE_EMPLOYEE = 4)
--------------------------------------------------
INSERT INTO user_roles (user_id, role_id) VALUES
(35,4),(36,4),(37,4),(38,4),(39,4),(40,4),(41,4),(42,4),(43,4),(44,4),
(45,4),(46,4),(47,4),(48,4),(49,4),(50,4),(51,4),(52,4),(53,4),(54,4),
(55,4),(56,4),(57,4),(58,4),(59,4),(60,4),(61,4),(62,4),(63,4),(64,4),
(65,4),(66,4),(67,4),(68,4),(69,4),(70,4),(71,4),(72,4),(73,4),(74,4),
(75,4),(76,4),(77,4),(78,4),(79,4),(80,4),(81,4),(82,4),(83,4),(84,4),
(85,4),(86,4),(87,4),(88,4),(89,4),(90,4),(91,4),(92,4),(93,4),(94,4)
ON CONFLICT DO NOTHING;

INSERT INTO staffing_requests (
    request_id, title, description, project_id, project_name, project_location, 
    department_id, status, created_by_employee_id, wage_per_hour, 
    required_skills, experience_years, availability_hours_per_week,
    work_location, project_start_date, project_end_date, project_context
) VALUES 
--------------------------------------------------------------------------------
-- DEPT 1: IT BACKEND (10 Requests)
--------------------------------------------------------------------------------
(1, 'Senior Java Developer', 'Core backend service development.', 1, 'Project Skyfall', 'Munich', 1, 'APPROVED', 1, 39.50, '["Java", "Spring Boot", "PostgreSQL"]'::jsonb, 5, 40, 'Onsite', '2026-02-01', '2026-12-31', 'Cloud Migration'),
(2, 'Workflow Automation Expert', 'Camunda implementation for business logic.', 1, 'Project Skyfall', 'Munich', 1, 'APPROVED', 1, 37.00, '["Java", "Camunda", "PostgreSQL"]'::jsonb, 4, 20, 'Remote', '2026-02-01', '2026-06-01', 'Process Automation'),
(3, 'Cloud Infrastructure Eng', 'Kubernetes and Docker orchestration.', 3, 'Legacy ERP Sunset', 'Frankfurt', 1, 'APPROVED', 1, 35.50, '["Kubernetes", "Docker", "CI/CD"]'::jsonb, 6, 40, 'Onsite', '2026-03-01', '2026-12-31', 'Infrastructure'),
(4, 'Python Backend Dev', 'FastAPI/Django integration.', 2, 'NeuralCare Portal', 'Darmstadt', 1, 'PENDING_APPROVAL', 1, 32.00, '["Python", "FastAPI", "PostgreSQL"]'::jsonb, 3, 40, 'Remote', '2026-04-01', '2026-12-31', 'AI Services'),
(5, 'Event Streaming Specialist', 'Kafka cluster setup.', 1, 'Project Skyfall', 'Munich', 1, 'PENDING_APPROVAL', 1, 38.00, '["Kafka", "Java", "Docker"]'::jsonb, 5, 40, 'Remote', '2026-03-01', '2026-12-31', 'Real-time Data'),
(6, 'Database Architect', 'PostgreSQL optimization.', 3, 'Legacy ERP Sunset', 'Frankfurt', 1, 'PENDING_APPROVAL', 1, 34.00, '["PostgreSQL", "SQL", "Java"]'::jsonb, 7, 40, 'Onsite', '2026-04-01', '2026-12-31', 'Data Migration'),
(7, 'Security Backend Eng', 'Firewall and API security logic.', 1, 'Project Skyfall', 'Munich', 1, 'PENDING_APPROVAL', 1, 40.00, '["Java", "Spring Boot", "REST API"]'::jsonb, 5, 40, 'Onsite', '2026-03-01', '2026-09-01', 'Security Hardening'),
(8, 'Junior Microservices Dev', 'Supporting REST API development.', 4, 'WorkForce Go', 'Berlin', 1, 'PENDING_APPROVAL', 1, 25.00, '["Java", "MySQL", "REST API"]'::jsonb, 2, 40, 'Remote', '2026-05-01', '2026-12-01', 'Mobile Backend'),
(9, 'DevOps Pipeline Lead', 'Jenkins and CI/CD automation.', 2, 'NeuralCare Portal', 'Darmstadt', 1, 'PENDING_APPROVAL', 1, 36.00, '["CI/CD", "Jenkins", "Docker"]'::jsonb, 4, 40, 'Remote', '2026-04-01', '2026-11-01', 'Delivery Automation'),
(10, 'Scale Engineer', 'Cloud platform scaling with Azure.', 1, 'Project Skyfall', 'Munich', 1, 'PENDING_APPROVAL', 1, 39.00, '["Azure", "Java", "Kubernetes"]'::jsonb, 6, 40, 'Onsite', '2026-06-01', '2026-12-31', 'Cloud Expansion'),

--------------------------------------------------------------------------------
-- DEPT 2: IT FRONTEND (10 Requests)
--------------------------------------------------------------------------------
(11, 'Lead React Architect', 'Main UI Architecture design.', 2, 'NeuralCare Portal', 'Darmstadt', 2, 'APPROVED', 1, 36.50, '["React.js", "TypeScript", "Redux"]'::jsonb, 7, 40, 'Onsite', '2026-04-01', '2026-12-31', 'AI Support Portal'),
(12, 'UX Designer', 'Figma to React implementation.', 4, 'WorkForce Go', 'Berlin', 2, 'APPROVED', 1, 34.00, '["Figma", "React.js", "HTML5"]'::jsonb, 4, 20, 'Remote', '2026-02-01', '2026-11-01', 'Mobile Alpha'),
(13, 'Frontend Testing Eng', 'Jest and UI Quality Assurance.', 2, 'NeuralCare Portal', 'Darmstadt', 2, 'APPROVED', 1, 31.00, '["React.js", "Jest", "TypeScript"]'::jsonb, 3, 40, 'Onsite', '2026-03-15', '2026-12-15', 'Quality Control'),
(14, 'UI State Manager', 'Redux/Context API optimization.', 4, 'WorkForce Go', 'Berlin', 2, 'PENDING_APPROVAL', 1, 35.00, '["Redux", "Context API", "React.js"]'::jsonb, 5, 40, 'Remote', '2026-05-01', '2026-12-01', 'Mobile Performance'),
(15, 'Interface Integrator', 'Axios and API connectivity.', 2, 'NeuralCare Portal', 'Darmstadt', 2, 'PENDING_APPROVAL', 1, 33.00, '["React.js", "Axios", "TypeScript"]'::jsonb, 4, 40, 'Onsite', '2026-04-01', '2026-10-01', 'System Bridge'),
(16, 'Mobile UI Specialist', 'Responsive design for mobile.', 4, 'WorkForce Go', 'Berlin', 2, 'PENDING_APPROVAL', 1, 32.00, '["React.js", "HTML5", "CSS"]'::jsonb, 3, 40, 'Remote', '2026-03-01', '2026-12-01', 'Mobile UI'),
(17, 'Material UI Developer', 'Component library implementation.', 2, 'NeuralCare Portal', 'Darmstadt', 2, 'PENDING_APPROVAL', 1, 30.00, '["Material UI", "React.js", "TypeScript"]'::jsonb, 3, 40, 'Onsite', '2026-05-01', '2026-12-31', 'Design System'),
(18, 'Frontend Graphics Eng', 'UI rendering optimization.', 4, 'WorkForce Go', 'Berlin', 2, 'PENDING_APPROVAL', 1, 38.00, '["React.js", "TypeScript", "Redux"]'::jsonb, 5, 40, 'Onsite', '2026-02-01', '2026-11-01', 'Visual Engine'),
(19, 'Web Accessibility Lead', 'Compliance and screen reader support.', 2, 'NeuralCare Portal', 'Darmstadt', 2, 'PENDING_APPROVAL', 1, 34.00, '["HTML5", "CSS", "React.js"]'::jsonb, 4, 20, 'Remote', '2026-04-01', '2026-10-31', 'Accessibility'),
(20, 'Junior Frontend Dev', 'General UI support and bug fixes.', 4, 'WorkForce Go', 'Berlin', 2, 'PENDING_APPROVAL', 1, 26.00, '["React.js", "JavaScript", "HTML5"]'::jsonb, 1, 40, 'Onsite', '2026-06-01', '2026-12-01', 'Support'),

--------------------------------------------------------------------------------
-- DEPT 3: FINANCE & MANAGEMENT (10 Requests)
--------------------------------------------------------------------------------
(21, 'Project Manager', 'Agile project delivery.', 4, 'WorkForce Go', 'Berlin', 3, 'APPROVED', 1, 38.50, '["Project Management", "Agile", "Scrum"]'::jsonb, 5, 40, 'Remote', '2026-01-10', '2026-12-31', 'Mobile Rollout'),
(22, 'Financial Analyst', 'Budgeting and ERP Management.', 3, 'Legacy ERP Sunset', 'Frankfurt', 3, 'APPROVED', 1, 35.00, '["Budgeting & Forecasting", "SAP", "ERP Systems"]'::jsonb, 6, 40, 'Onsite', '2026-05-01', '2026-12-01', 'Financial Sunsetting'),
(23, 'Resource Planner', 'Capacity and workforce planning.', 1, 'Project Skyfall', 'Munich', 3, 'APPROVED', 1, 36.50, '["Resource Allocation", "Capacity Planning", "Jira"]'::jsonb, 4, 40, 'Remote', '2026-02-01', '2026-12-31', 'Resource Optimization'),
(24, 'HR Payroll Expert', 'Payroll and HRIS management.', 4, 'WorkForce Go', 'Berlin', 3, 'PENDING_APPROVAL', 1, 32.00, '["Payroll Administration", "HR", "ERP Systems"]'::jsonb, 3, 20, 'Remote', '2026-02-01', '2026-11-01', 'Administrative Support'),
(25, 'Stakeholder Manager', 'Managing cross-department relations.', 2, 'NeuralCare Portal', 'Darmstadt', 3, 'PENDING_APPROVAL', 1, 40.00, '["Stakeholder Management", "Agile", "Confluence"]'::jsonb, 8, 20, 'Onsite', '2026-04-01', '2026-10-01', 'Portal Strategy'),
(26, 'Compliance Officer', 'Internal financial audits.', 3, 'Legacy ERP Sunset', 'Frankfurt', 3, 'PENDING_APPROVAL', 1, 37.00, '["Financial Reporting", "Profit & Loss Analysis", "SAP"]'::jsonb, 5, 40, 'Onsite', '2026-06-01', '2026-12-31', 'Risk Management'),
(27, 'Operations Lead', 'Workflow management for R&D.', 2, 'NeuralCare Portal', 'Darmstadt', 3, 'PENDING_APPROVAL', 1, 39.00, '["Project Management", "Agile", "Stakeholder Management"]'::jsonb, 6, 40, 'Onsite', '2026-03-01', '2026-12-01', 'Ops Sync'),
(28, 'Recruitment Specialist', 'Technical hiring drive.', 1, 'Project Skyfall', 'Munich', 3, 'PENDING_APPROVAL', 1, 33.00, '["Talent Acquisition", "HR", "Agile"]'::jsonb, 3, 40, 'Remote', '2026-04-01', '2026-10-01', 'Hiring Plan'),
(29, 'Budget Controller', 'Tracking project expenditures.', 4, 'WorkForce Go', 'Berlin', 3, 'PENDING_APPROVAL', 1, 31.00, '["Microsoft Excel (Advanced)", "Financial Reporting", "SAP"]'::jsonb, 4, 40, 'Remote', '2026-05-01', '2026-12-31', 'Cost Tracking'),
(30, 'Conflict Mediator', 'Employee relations and mediation.', 2, 'NeuralCare Portal', 'Darmstadt', 3, 'PENDING_APPROVAL', 1, 34.00, '["Conflict Resolution", "Employee Relations", "HR"]'::jsonb, 5, 20, 'Onsite', '2026-03-01', '2026-09-01', 'People Culture')
ON CONFLICT (request_id) DO NOTHING;
--------------------------------------------------
-- 6) EXTERNAL EMPLOYEES & USERS
--------------------------------------------------
INSERT INTO external_employees (id, external_employee_id, provider, contract_id,first_name, last_name, staffing_request_id, project_id, skills) VALUES 
(1, 'EXT-001', 'FreelanceHub', 'CONTRACT-001', 'John', 'Doe', 1, 1, '["Java", "Microservices"]') ON CONFLICT (id) DO NOTHING;

-- Freelancer User ID set to 50 to avoid any conflict with Dept Heads
INSERT INTO users (id, email, password_hash, external_employee_id) 
VALUES (50, 'john.doe@freelance.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 1) ON CONFLICT (id) DO NOTHING;

--------------------------------------------------
-- 7) APPLICATIONS & ASSIGNMENTS
--------------------------------------------------
-- INSERT INTO employee_applications (employee_id, staffing_request_id, status, comment) 
-- VALUES (14, 1, 'APPLIED', 'I have extensive Kubernetes experience.') 
-- ON CONFLICT DO NOTHING;



--------------------------------------------------
-- 8) SEQUENCE SYNCHRONIZATION
--------------------------------------------------
SELECT setval(pg_get_serial_sequence('employees', 'id'), (SELECT MAX(id) FROM employees));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('projects', 'id'), (SELECT MAX(id) FROM projects));
SELECT setval(pg_get_serial_sequence('departments', 'id'), (SELECT MAX(id) FROM departments));
SELECT setval(pg_get_serial_sequence('staffing_requests', 'request_id'), (SELECT MAX(request_id) FROM staffing_requests));
SELECT setval(pg_get_serial_sequence('external_employees', 'id'), (SELECT MAX(id) FROM external_employees));
