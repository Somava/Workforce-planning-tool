-- 1. Job Roles
INSERT INTO job_roles (id, name) VALUES
(1, 'Developer'), (2, 'Tester'), (3, 'Designer'), (4, 'Data Engineer'), (5, 'Project Manager'),
(6, 'Resource Planner'), (7, 'DevOps Engineer'), (8, 'System Administrator'), (9, 'Software Architect'), (10, 'Business Analyst')
ON CONFLICT (id) DO NOTHING;

-- 2. Departments (REQUIRED for the Foreign Key in employees)
INSERT INTO departments (id, name) VALUES
(1, 'IT'), (2, 'QA'), (3, 'Design'), (4, 'Data')
ON CONFLICT (id) DO NOTHING;

-- 3. Skills
INSERT INTO skills (id, name) VALUES
(1, 'Java'), (2, 'Spring Boot'), (3, 'React'), (4, 'Python'), (5, 'SQL')
ON CONFLICT (id) DO NOTHING;

-- 4. Employees
-- Removed 'working_time_model' and 'department' (renamed to department_id) to match Employee.java
INSERT INTO employees (
    id, employee_id, first_name, last_name, department_id, supervisor_id,
    primary_location, contract_type, emergency_contact,
    availability_start, availability_end, job_role_id, project_preferences, interests
) VALUES
(1, 'EMP001', 'Alice', 'Müller', 1, NULL, 'Frankfurt', 'FULL_TIME', '123-456', NULL, NULL, 5, 'Cloud Projects', 'Hiking'),
(2, 'EMP002', 'Bob', 'Schmidt', 1, 1, 'Frankfurt', 'FULL_TIME', '234-567', NULL, NULL, 1, 'Backend Systems', 'Gaming'),
(3, 'EMP003', 'Carla', 'Weber', 2, 1, 'Frankfurt', 'PART_TIME', '345-678', NULL, NULL, 2, 'Test Automation', 'Cooking'),
(4, 'EMP004', 'David', 'Khan', 3, 1, 'Frankfurt', 'FULL_TIME', '456-789', NULL, NULL, 3, 'UI/UX Projects', 'Art'),
(5, 'EMP005', 'Emily', 'Patel', 4, 1, 'Frankfurt', 'FULL_TIME', '567-890', NULL, NULL, 4, 'ML/Data', 'Reading')
ON CONFLICT (id) DO NOTHING;

-- 5. Roles (System Permissions)
INSERT INTO roles (id, name) VALUES
(1, 'PROJECT_MANAGER'), (2, 'DEPARTMENT_HEAD'), (3, 'RESOURCE_PLANNER'), (4, 'EMPLOYEE')
ON CONFLICT (id) DO NOTHING;

-- 6. Users
INSERT INTO users (id, email, password_hash, employee_id) VALUES
(1, 'alice@company.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.7uXCf1O', 1),
(2, 'bob@company.com',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.7uXCf1O', 2)
ON CONFLICT (id) DO NOTHING;

-- 7. User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2), (1, 1), (1, 3), (2, 4)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Reset sequences
SELECT setval(pg_get_serial_sequence('employees', 'id'), (SELECT MAX(id) FROM employees));
SELECT setval(pg_get_serial_sequence('departments', 'id'), (SELECT MAX(id) FROM departments));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));