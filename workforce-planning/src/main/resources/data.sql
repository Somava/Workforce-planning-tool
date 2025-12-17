INSERT INTO job_roles (id, name) VALUES
(1, 'Developer'),
(2, 'Tester'),
(3, 'Designer'),
(4, 'Data Engineer'),
(5, 'Project Manager'),
(6, 'Resource Planner'),
(7, 'DevOps Engineer'),
(8, 'System Administrator'),
(9, 'Software Architect'),
(10, 'Business Analyst')
ON CONFLICT (id) DO NOTHING;

INSERT INTO skills (id, name) VALUES
(1, 'Java'),
(2, 'Spring Boot'),
(3, 'React'),
(4, 'Python'),
(5, 'SQL'),
(6, 'Docker'),
(7, 'AWS'),
(8, 'Node.js'),
(9, 'UI/UX Design'),
(10, 'Data Modeling')
ON CONFLICT (id) DO NOTHING;

INSERT INTO certifications (id, name) VALUES
(1, 'AWS Solutions Architect'),
(2, 'Scrum Master'),
(3, 'Oracle Java Professional'),
(4, 'Certified Data Engineer'),
(5, 'PMP Certification')
ON CONFLICT (id) DO NOTHING;

INSERT INTO languages (id, name) VALUES
(1, 'English'),
(2, 'German'),
(3, 'Spanish'),
(4, 'French')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (
    id, employee_id, first_name, last_name, department, supervisor_id,
    primary_location, contract_type, working_time_model, emergency_contact,
    availability_start, availability_end, job_role_id, project_preferences, interests
) VALUES
(1, 'EMP001', 'Alice', 'Müller', 'IT', NULL, 'Frankfurt', 'FULL_TIME', '40h/week', '123-456', NULL, NULL, 5, 'Cloud Projects', 'Hiking'),
(2, 'EMP002', 'Bob', 'Schmidt', 'IT', 1, 'Frankfurt', 'FULL_TIME', '40h/week', '234-567', NULL, NULL, 1, 'Backend Systems', 'Gaming'),
(3, 'EMP003', 'Carla', 'Weber', 'QA', 1, 'Frankfurt', 'PART_TIME', '20h/week', '345-678', NULL, NULL, 2, 'Test Automation', 'Cooking'),
(4, 'EMP004', 'David', 'Khan', 'Design', 1, 'Frankfurt', 'FULL_TIME', '40h/week', '456-789', NULL, NULL, 3, 'UI/UX Projects', 'Art'),
(5, 'EMP005', 'Emily', 'Patel', 'Data', 1, 'Frankfurt', 'FULL_TIME', '40h/week', '567-890', NULL, NULL, 4, 'ML/Data', 'Reading')
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_skills (id, employee_id, skill_id, experience_level) VALUES
(1, 2, 1, 'Senior'),      -- Bob: Java
(2, 2, 2, 'Intermediate'), -- Bob: Spring Boot
(3, 3, 5, 'Intermediate'), -- Carla: SQL
(4, 5, 4, 'Senior'),      -- Emily: Python
(5, 5, 5, 'Senior'),      -- Emily: SQL
(6, 5, 7, 'Intermediate') -- Emily: AWS
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_certifications (id, employee_id, certification_id, issuer, date_obtained, valid_until)
VALUES
(1, 5, 1, 'AWS', '2023-05-10', NULL),
(2, 2, 3, 'Oracle', '2022-10-15', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_languages (id, employee_id, language_id, proficiency_level) VALUES
(1, 1, 1, 'C1'),
(2, 2, 1, 'B2'),
(3, 2, 2, 'B1'),
(4, 3, 1, 'C1'),
(5, 5, 1, 'C2'),
(6, 5, 2, 'B2')
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name) VALUES
(1, 'PROJECT_MANAGER'),
(2, 'DEPARTMENT_HEAD'),
(3, 'RESOURCE_PLANNER'),
(4, 'EMPLOYEE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, password_hash, employee_id) VALUES
(1, 'alice@company.com', 'hashed_pw_here', 1),
(2, 'bob@company.com',   'hashed_pw_here', 2),
(3, 'carla@company.com', 'hashed_pw_here', 3),
(4, 'david@company.com', 'hashed_pw_here', 4),
(5, 'emily@company.com', 'hashed_pw_here', 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2),   -- Alice = Department Head
(1, 1),   -- Alice also PM
(2, 4),   -- Bob = Employee
(3, 4),   -- Carla = Employee
(4, 4),   -- David = Employee
(5, 4),   -- Emily = Employee
(1, 3)   -- Alice = Resource Planner (for testing all functions)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO projects (
    id, name, description, task_description, start_date, end_date, location, links, status, published, created_at, updated_at
) VALUES
(1, 'Cloud Migration', 'Move systems to AWS', 'Full separation, VPC design, IAM', '2024-01-01', '2024-06-01', 'Frankfurt', NULL, 'ACTIVE', TRUE, now(), now()),
(2, 'Mobile App', 'New app for clients', 'Design UI, build Android/iOS app', '2024-02-01', '2024-09-01', 'Frankfurt', NULL, 'PLANNED', FALSE, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO staffing_requests (
    id, title, project_id, job_role_id, availability_hours_per_week,
    period_start, period_end, status, remarks, created_by_employee_id, created_at
) VALUES
(1, 'Senior Java Backend Developer', 1, 1, 40, '2024-01-10', '2024-05-30', 'PUBLISHED', 'Must know Spring Boot', 1, now()),
(2, 'QA Tester', 2, 2, 20, '2024-02-10', '2024-08-15', 'DRAFT', NULL, 1, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO staffing_request_skills (id, staffing_request_id, skill_id, required_level) VALUES
(1, 1, 1, 'Senior'),      -- Java
(2, 1, 2, 'Intermediate'),-- Spring Boot
(3, 2, 5, 'Intermediate')-- SQL for QA role
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_applications (
    id, employee_id, staffing_request_id, status, applied_at
) VALUES
(1, 2, 1, 'APPLIED', now()),   -- Bob applies for Java role
(2, 5, 1, 'APPLIED', now())   -- Emily applies too
ON CONFLICT (id) DO NOTHING;

INSERT INTO assignments (
    id, employee_id, staffing_request_id, project_id, status,
    period_start, period_end, performance_rating, feedback, created_at, created_by_employee_id
) VALUES
(1, 2, 1, 1, 'ACTIVE', '2024-01-15', '2024-06-01', NULL, NULL, now(), 1)
ON CONFLICT (id) DO NOTHING;


-- Reset sequences so auto-increment works for new records
SELECT setval(pg_get_serial_sequence('employees', 'id'), (SELECT MAX(id) FROM employees));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('projects', 'id'), (SELECT MAX(id) FROM projects));
SELECT setval(pg_get_serial_sequence('job_roles', 'id'), (SELECT MAX(id) FROM job_roles));
SELECT setval(pg_get_serial_sequence('staffing_requests', 'id'), (SELECT MAX(id) FROM staffing_requests));
