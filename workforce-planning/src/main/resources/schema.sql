--------------------------------------------------
-- CLEAN RESET (for dev/demo only)
--------------------------------------------------
DROP TABLE IF EXISTS employee_applications CASCADE;
DROP TABLE IF EXISTS assignments CASCADE;
DROP TABLE IF EXISTS staffing_requests CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS employee_languages CASCADE;
DROP TABLE IF EXISTS employee_certifications CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS external_employees CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS languages CASCADE;
DROP TABLE IF EXISTS certifications CASCADE;
DROP TABLE IF EXISTS job_roles CASCADE;

--------------------------------------------------
-- 1) LOOKUP TABLES
--------------------------------------------------
CREATE TABLE job_roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE certifications (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE languages (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

--------------------------------------------------
-- 2) CORE INFRASTRUCTURE
--------------------------------------------------
CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    task_description    TEXT,
    start_date          DATE,
    end_date            DATE,
    location            VARCHAR(200),
    links               TEXT,
    status              VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    published           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    manager_user_id     BIGINT NULL
);
-- Note: No UNIQUE on name, UNIQUE on department_head_user_id
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL, 
    project_id BIGINT NOT NULL,
    department_head_user_id BIGINT NULL UNIQUE, 
    resource_planner_user_id BIGINT NULL UNIQUE,
    CONSTRAINT fk_dept_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE employees (
    id                       BIGSERIAL PRIMARY KEY,
    email                    VARCHAR(150) UNIQUE,
    employee_id              VARCHAR(100) UNIQUE,
    first_name               VARCHAR(100) NOT NULL,
    last_name                VARCHAR(100) NOT NULL,
    supervisor_id            BIGINT NULL,
    primary_location         VARCHAR(150),
    contract_type            VARCHAR(50),
    experience_years         INTEGER,
    wage_per_hour NUMERIC(10,2),
    emergency_contact        VARCHAR(255),
    availability_start       DATE,
    availability_end         DATE,
    matching_availability     VARCHAR(50) DEFAULT 'AVAILABLE',  -- AVAILABLE OR RESERVED
    job_role_id              BIGINT NULL,
    department_id            BIGINT NULL, 
    default_role_id          BIGINT NULL,
    skills                   JSONB,
    total_hours_per_week     INTEGER,
    remaining_hours_per_week INTEGER,
    performance_rating DOUBLE PRECISION, 
    project_preferences      TEXT,
    interests                TEXT,
    CONSTRAINT fk_employee_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles(id),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employee_default_role FOREIGN KEY (default_role_id) REFERENCES roles(id),
    CONSTRAINT fk_employee_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees(id)
);

--------------------------------------------------
-- 3) EXTERNAL & AUTH
--------------------------------------------------
-- Must be created before 'users' table because 'users' references it
CREATE TABLE external_employees (
    id BIGSERIAL PRIMARY KEY,
    external_employee_id VARCHAR(150) NOT NULL,
    provider VARCHAR(150) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    skills JSONB,
    experience_years INTEGER,
    wage_per_hour NUMERIC(10,2),
    staffing_request_id BIGINT NULL, -- Will be linked later via ALTER
    project_id BIGINT NULL,
    status VARCHAR,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_employee UNIQUE (provider, external_employee_id),
    CONSTRAINT fk_ext_emp_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    employee_id BIGINT UNIQUE,
    external_employee_id BIGINT UNIQUE,
    CONSTRAINT fk_user_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_external_employee FOREIGN KEY (external_employee_id) REFERENCES external_employees(id) ON DELETE RESTRICT,
    CONSTRAINT chk_user_employee_xor_external CHECK (
        (employee_id IS NOT NULL AND external_employee_id IS NULL)
     OR (employee_id IS NULL AND external_employee_id IS NOT NULL)
    )
);

-- Late binding for the circular dependency between departments and users
ALTER TABLE departments 
    ADD CONSTRAINT fk_department_head_user 
        FOREIGN KEY (department_head_user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE projects
  ADD CONSTRAINT fk_project_manager_user
  FOREIGN KEY (manager_user_id) REFERENCES users(id)
  ON DELETE SET NULL;


------------------------------------------------
-- 4) MANY-TO-MANY / DETAIL TABLES
--------------------------------------------------
CREATE TABLE employee_certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    certification_id BIGINT NOT NULL,
    issuer VARCHAR(255),
    date_obtained DATE,
    valid_until DATE,
    CONSTRAINT fk_emp_cert_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_cert_cert FOREIGN KEY (certification_id) REFERENCES certifications(id) ON DELETE CASCADE,
    CONSTRAINT uq_emp_cert UNIQUE (employee_id, certification_id)
);

CREATE TABLE employee_languages (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    proficiency_level VARCHAR(50),
    CONSTRAINT fk_emp_lang_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_lang_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE,
    CONSTRAINT uq_emp_lang UNIQUE (employee_id, language_id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

--------------------------------------------------
-- 5) STAFFING
--------------------------------------------------
CREATE TABLE staffing_requests (
    request_id BIGSERIAL PRIMARY KEY,
    id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    project_id BIGINT NOT NULL,
    job_role_id BIGINT NULL,
    availability_hours_per_week INTEGER,
    project_start_date DATE,
    project_end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by_employee_id BIGINT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    project_name VARCHAR(200),
    department_id BIGINT NULL,
    wage_per_hour NUMERIC(10,2),
    required_skills JSONB,
    project_context TEXT,
    project_location VARCHAR(200),
    work_location VARCHAR(200),
    process_instance_key BIGINT,
    assigned_user_id BIGINT NULL,
    experience_years INTEGER,
    validation_error TEXT,
    rejection_reason TEXT,
    rejection_type TEXT,

    CONSTRAINT fk_staffreq_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_staffreq_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles(id),
    CONSTRAINT fk_staffreq_creator FOREIGN KEY (created_by_employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_staffreq_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_staffreq_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Fix for external employee link to staffing request
ALTER TABLE external_employees ADD CONSTRAINT fk_ext_emp_request FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(request_id) ON DELETE SET NULL;

--------------------------------------------------
-- 6) ASSIGNMENTS & APPLICATIONS
--------------------------------------------------
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    staffing_request_id BIGINT NULL,
    project_id BIGINT NULL,
    status VARCHAR(50) NOT NULL,
    period_start DATE,
    period_end DATE,
    performance_rating SMALLINT,
    feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_employee_id BIGINT NULL,
    CONSTRAINT fk_assignment_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_request FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(request_id) ON DELETE SET NULL,
    CONSTRAINT fk_assignment_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_assignment_created_by FOREIGN KEY (created_by_employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT chk_rating_range CHECK (performance_rating IS NULL OR (performance_rating BETWEEN 1 AND 5))
);

CREATE UNIQUE INDEX uq_assignment_emp_request_notnull ON assignments(employee_id, staffing_request_id) WHERE staffing_request_id IS NOT NULL;

CREATE TABLE employee_applications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    staffing_request_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    decision_at TIMESTAMPTZ,
    decision_by_employee_id BIGINT NULL,
    comment TEXT,
    CONSTRAINT fk_app_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_request FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_decision_by FOREIGN KEY (decision_by_employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT uq_app_emp_request UNIQUE (employee_id, staffing_request_id)
);

-- 1. Ensure columns exist without unique constraints
-- 2. If the constraint was already created by Hibernate, drop it:
ALTER TABLE departments DROP CONSTRAINT IF EXISTS departments_department_head_user_id_key;
ALTER TABLE departments DROP CONSTRAINT IF EXISTS departments_resource_planner_user_id_key;

-- 3. Fix the external_employees "created_at" error from your logs
-- Give it a default value so existing rows aren't NULL
ALTER TABLE external_employees ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;