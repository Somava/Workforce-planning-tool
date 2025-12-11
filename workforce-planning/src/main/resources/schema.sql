
--------------------------------------------------
-- JOB ROLES (Developer, Tester, Designer, etc.)
--------------------------------------------------
CREATE TABLE job_roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

--------------------------------------------------
-- EMPLOYEES
--------------------------------------------------
CREATE TABLE employees (
    id                      BIGSERIAL PRIMARY KEY,
    employee_id             VARCHAR(100) UNIQUE,         -- company HR ID

    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,

    department              VARCHAR(150),                -- can normalize later
    supervisor_id           BIGINT,                      -- FK to employees.id

    primary_location        VARCHAR(150),

    contract_type           VARCHAR(50),                 -- maps to ContractType enum in Java
    working_time_model      VARCHAR(100),                -- e.g. "40h/week"

    emergency_contact       VARCHAR(255),

    availability_start      DATE,
    availability_end        DATE,

    job_role_id             BIGINT,                      -- FK to job_roles.id

    project_preferences     TEXT,
    interests               TEXT,

    CONSTRAINT fk_employee_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES employees(id),

    CONSTRAINT fk_employee_job_role
        FOREIGN KEY (job_role_id) REFERENCES job_roles(id)
);

--------------------------------------------------
-- SKILLS
--------------------------------------------------
CREATE TABLE skills (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL UNIQUE
);

--------------------------------------------------
-- EMPLOYEE_SKILLS (many-to-many with experience level per skill)
--------------------------------------------------
CREATE TABLE employee_skills (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    skill_id            BIGINT NOT NULL,
    experience_level    VARCHAR(100),  -- e.g. "Junior", "Intermediate", "Senior"

    CONSTRAINT fk_emp_skill_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emp_skill_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emp_skill UNIQUE (employee_id, skill_id)
);

--------------------------------------------------
-- CERTIFICATIONS
--------------------------------------------------
CREATE TABLE certifications (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE
);

--------------------------------------------------
-- EMPLOYEE_CERTIFICATIONS
--------------------------------------------------
CREATE TABLE employee_certifications (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    certification_id    BIGINT NOT NULL,
    issuer              VARCHAR(255),
    date_obtained       DATE,
    valid_until         DATE,

    CONSTRAINT fk_emp_cert_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emp_cert_cert
        FOREIGN KEY (certification_id) REFERENCES certifications(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emp_cert UNIQUE (employee_id, certification_id)
);

--------------------------------------------------
-- LANGUAGES
--------------------------------------------------
CREATE TABLE languages (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE  -- could be "DE", "EN" or "German"
);

--------------------------------------------------
-- EMPLOYEE_LANGUAGES
--------------------------------------------------
CREATE TABLE employee_languages (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    language_id         BIGINT NOT NULL,
    proficiency_level   VARCHAR(50),      -- e.g. "B2", "C1", "Native"

    CONSTRAINT fk_emp_lang_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_emp_lang_language
        FOREIGN KEY (language_id) REFERENCES languages(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_emp_lang UNIQUE (employee_id, language_id)
);

--------------------------------------------------
-- ROLES (system personas: PROJECT_MANAGER, EMPLOYEE, etc.)
--------------------------------------------------
CREATE TABLE roles (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

--------------------------------------------------
-- USERS (login accounts, linked optionally to employees)
--------------------------------------------------
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,

    employee_id     BIGINT NOT NULL UNIQUE, 

    CONSTRAINT fk_user_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
);

--------------------------------------------------
-- USER_ROLES (many-to-many between users and roles)
--------------------------------------------------
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
);

--------------------------------------------------
-- PROJECTS
--------------------------------------------------
CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,               -- short/medium description
    task_description    TEXT,               -- detailed tasks

    start_date          DATE,
    end_date            DATE,

    location            VARCHAR(200),
    links               TEXT,               -- URLs, comma-separated or markdown

    status              VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    published           BOOLEAN NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

--------------------------------------------------
-- STAFFING REQUESTS
--------------------------------------------------
CREATE TABLE staffing_requests (
    id                          BIGSERIAL PRIMARY KEY,

    title                       VARCHAR(200) NOT NULL,
    description                 TEXT,      
    project_id                  BIGINT NOT NULL,
    job_role_id                 BIGINT,          -- FK to job_roles

    availability_hours_per_week INTEGER,         -- requested hours/week

    period_start                DATE,           -- request period from
    period_end                  DATE,           -- request period to

    status                      VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    remarks                     TEXT,

    created_by_employee_id      BIGINT,         -- who created it (PM)
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at                TIMESTAMPTZ,

    CONSTRAINT fk_staffreq_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_staffreq_jobrole
        FOREIGN KEY (job_role_id) REFERENCES job_roles(id),

    CONSTRAINT fk_staffreq_creator
        FOREIGN KEY (created_by_employee_id) REFERENCES employees(id)
);

--------------------------------------------------
-- STAFFING REQUEST SKILLS (REQ -> SKILL + LEVEL)
--------------------------------------------------
CREATE TABLE staffing_request_skills (
    id                      BIGSERIAL PRIMARY KEY,
    staffing_request_id     BIGINT NOT NULL,
    skill_id                BIGINT NOT NULL,
    required_level          VARCHAR(100),  -- e.g. "Junior", "Senior"

    CONSTRAINT fk_staffreqskill_request
        FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_staffreqskill_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_staffreq_skill UNIQUE (staffing_request_id, skill_id)
);

--------------------------------------------------
-- ASSIGNMENTS (EMPLOYEE <-> REQUEST/PROJECT)
--------------------------------------------------
CREATE TABLE assignments (
    id                      BIGSERIAL PRIMARY KEY,

    employee_id             BIGINT NOT NULL,    -- the one who is assigned
    staffing_request_id     BIGINT NOT NULL,
    project_id              BIGINT,

    status                  VARCHAR(50) NOT NULL,  -- 'WAITING_APPROVAL', 'APPROVED' 'REJECTED', 'ACTIVE', 'COMPLETED'

    period_start            DATE,
    period_end              DATE,

    performance_rating      SMALLINT,
    feedback                TEXT,

    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    created_by_employee_id  BIGINT NOT NULL,    -- the planner or PM who created the assignment

    CONSTRAINT fk_assignment_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_assignment_request
        FOREIGN KEY (staffing_request_id) REFERENCES staffing_requests(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_assignment_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_assignment_created_by
        FOREIGN KEY (created_by_employee_id) REFERENCES employees(id)
        ON DELETE SET NULL,


    CONSTRAINT chk_rating_range
        CHECK (performance_rating IS NULL OR (performance_rating BETWEEN 1 AND 5)),

    -- One employee should not have duplicate assignments for the same request
    CONSTRAINT uq_assignment_emp_request
        UNIQUE (employee_id, staffing_request_id)
);

--------------------------------------------------
-- EMPLOYEE APPLICATIONS (EMPLOYEE -> STAFFING REQUEST)
--------------------------------------------------
CREATE TABLE employee_applications (
    id                      BIGSERIAL PRIMARY KEY,

    employee_id             BIGINT NOT NULL,       -- who applied
    staffing_request_id     BIGINT NOT NULL,       -- what they applied for

    status                  VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    applied_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    decision_at             TIMESTAMPTZ,           -- when planner/PM took action
    decision_by_employee_id BIGINT,                -- WHO made the decision
    comment                 TEXT,                  -- optional remark


    CONSTRAINT fk_app_employee
        FOREIGN KEY (employee_id) 
        REFERENCES employees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_app_request
        FOREIGN KEY (staffing_request_id) 
        REFERENCES staffing_requests(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_app_decision_by
        FOREIGN KEY (decision_by_employee_id)
        REFERENCES employees(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_app_emp_request
        UNIQUE (employee_id, staffing_request_id)
);
