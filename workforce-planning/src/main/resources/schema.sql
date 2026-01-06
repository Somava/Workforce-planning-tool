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

-- JOB ROLES (Developer, Tester, etc.)
CREATE TABLE job_roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

-- ROLES (system personas: PROJECT_MANAGER, EMPLOYEE, etc.)
CREATE TABLE roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

-- CERTIFICATIONS
CREATE TABLE certifications (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE
);

-- LANGUAGES
CREATE TABLE languages (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);


--------------------------------------------------
-- 2) CORE TABLES
--------------------------------------------------

-- PROJECTS
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
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- DEPARTMENTS (FK to users added later to avoid circular deps)
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    department_head_user_id BIGINT NULL
);

-- EMPLOYEES (refactored)
CREATE TABLE employees (
    id                       BIGSERIAL PRIMARY KEY,
    employee_id              VARCHAR(100) UNIQUE,         -- company HR ID

    first_name               VARCHAR(100) NOT NULL,
    last_name                VARCHAR(100) NOT NULL,

    supervisor_id            BIGINT NULL,                 -- FK to employees.id
    primary_location         VARCHAR(150),

    contract_type            VARCHAR(50),                 -- maps to ContractType enum
    emergency_contact        VARCHAR(255),

    availability_start       DATE,
    availability_end         DATE,

    job_role_id              BIGINT NULL,                 -- FK to job_roles
    department_id            BIGINT NULL,                 -- FK to departments
    default_role_id          BIGINT NULL,                 -- FK to roles

    skills                   JSONB,                       -- new
    total_hours_per_week     INTEGER,
    remaining_hours_per_week INTEGER,

    project_preferences      TEXT,
    interests                TEXT,

    CONSTRAINT fk_employee_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES employees(id),

    CONSTRAINT fk_employee_job_role
        FOREIGN KEY (job_role_id) REFERENCES job_roles(id),

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id) REFERENCES departments(id),

    CONSTRAINT fk_employee_default_role
        FOREIGN KEY (default_role_id) REFERENCES roles(id)
);

--------------------------------------------------
-- 3) EXTERNAL EMPLOYEES (no FK to staffing_requests yet)
--------------------------------------------------

CREATE TABLE external_employees (
    id BIGSERIAL PRIMARY KEY,
    external_employee_id VARCHAR(150) NOT NULL,
    provider VARCHAR(150) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,

    skills JSONB,

    staffing_request_id BIGINT NULL,   -- FK added later (after staffing_requests exists)
    project_id BIGINT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_external_employee UNIQUE (provider, external_employee_id),

    CONSTRAINT fk_ext_emp_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE SET NULL
);

--------------------------------------------------
-- 4) USERS (login accounts)
--    IMPORTANT: do NOT reference external_employees inside CREATE TABLE,
--    because we add that FK after external_employees exists.
--------------------------------------------------

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    employee_id BIGINT UNIQUE,
    external_employee_id BIGINT UNIQUE,

    CONSTRAINT fk_user_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE RESTRICT,

    -- Exactly one must be present
    CONSTRAINT chk_user_employee_xor_external
        CHECK (
            (employee_id IS NOT NULL AND external_employee_id IS NULL)
         OR (employee_id IS NULL AND external_employee_id IS NOT NULL)
        )
);

-- Now connect users.external_employee_id -> external_employees.id (safe now)
ALTER TABLE users
ADD CONSTRAINT fk_user_external_employee
    FOREIGN KEY (external_employee_id)
    REFERENCES external_employees(id)
    ON DELETE RESTRICT;

-- Now connect departments.department_head_user_id -> users.id (safe now)
ALTER TABLE departments
ADD CONSTRAINT fk_department_head_user
    FOREIGN KEY (department_head_user_id)
    REFERENCES users(id)
    ON DELETE SET NULL;


--------------------------------------------------
-- 5) MANY-TO-MANY / DETAIL TABLES
--------------------------------------------------

-- EMPLOYEE_CERTIFICATIONS
CREATE TABLE employee_certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    certification_id BIGINT NOT NULL,
    issuer VARCHAR(255),
    date_obtained DATE,
    valid_until DATE,

    CONSTRAINT fk_emp_cert_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emp_cert_cert
        FOREIGN KEY (certification_id) REFERENCES certifications(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emp_cert UNIQUE (employee_id, certification_id)
);

-- EMPLOYEE_LANGUAGES
CREATE TABLE employee_languages (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    proficiency_level VARCHAR(50),

    CONSTRAINT fk_emp_lang_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emp_lang_language
        FOREIGN KEY (language_id) REFERENCES languages(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emp_lang UNIQUE (employee_id, language_id)
);

-- USER_ROLES
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
);


--------------------------------------------------
-- 6) STAFFING (refactored)
--------------------------------------------------

-- STAFFING REQUESTS (request_id is the PK)
CREATE TABLE staffing_requests (
    request_id BIGSERIAL PRIMARY KEY,

    -- Optional "normal" id (keep only if you actually need it)
    id BIGINT,

    title VARCHAR(200) NOT NULL,
    description TEXT,

    project_id BIGINT NOT NULL,

    availability_hours_per_week INTEGER,

    project_start_date DATE,
    project_end_date DATE,

    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

    created_by_employee_id BIGINT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- new fields
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

    CONSTRAINT fk_staffreq_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_staffreq_creator
        FOREIGN KEY (created_by_employee_id) REFERENCES employees(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_staffreq_department
        FOREIGN KEY (department_id) REFERENCES departments(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_staffreq_assigned_user
        FOREIGN KEY (assigned_user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

-- Now we can connect external_employees.staffing_request_id -> staffing_requests.request_id
ALTER TABLE external_employees
ADD CONSTRAINT fk_ext_emp_request
    FOREIGN KEY (staffing_request_id)
    REFERENCES staffing_requests(request_id)
    ON DELETE SET NULL;


--------------------------------------------------
-- 7) ASSIGNMENTS
--------------------------------------------------

CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    staffing_request_id BIGINT NULL,     -- nullable because ON DELETE SET NULL
    project_id BIGINT NULL,

    status VARCHAR(50) NOT NULL,

    period_start DATE,
    period_end DATE,

    performance_rating SMALLINT,
    feedback TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    created_by_employee_id BIGINT NULL,  -- nullable because ON DELETE SET NULL

    CONSTRAINT fk_assignment_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_assignment_request
        FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(request_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_assignment_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_assignment_created_by
        FOREIGN KEY (created_by_employee_id) REFERENCES employees(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_rating_range
        CHECK (performance_rating IS NULL OR (performance_rating BETWEEN 1 AND 5))
);

-- Enforce unique employee+request only when request is not null
CREATE UNIQUE INDEX uq_assignment_emp_request_notnull
ON assignments(employee_id, staffing_request_id)
WHERE staffing_request_id IS NOT NULL;


--------------------------------------------------
-- 8) EMPLOYEE APPLICATIONS
--------------------------------------------------

CREATE TABLE employee_applications (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    staffing_request_id BIGINT NOT NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    decision_at TIMESTAMPTZ,
    decision_by_employee_id BIGINT NULL,
    comment TEXT,

    CONSTRAINT fk_app_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_app_request
        FOREIGN KEY (staffing_request_id)
        REFERENCES staffing_requests(request_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_app_decision_by
        FOREIGN KEY (decision_by_employee_id)
        REFERENCES employees(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_app_emp_request
        UNIQUE (employee_id, staffing_request_id)
);
