-- =========================
-- EMPLOYEES
-- =========================

CREATE TABLE employees (
    id                     BIGSERIAL PRIMARY KEY,
    employee_id            VARCHAR(50) UNIQUE,
    first_name             VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,

    department             VARCHAR(100),
    org_unit               VARCHAR(100),
    primary_location       VARCHAR(100),

    role                   VARCHAR(100),       -- e.g. "Developer", "Consultant"
    contract_type          VARCHAR(30),        -- e.g. "FULL_TIME", "PART_TIME"
    working_hours_per_week INT,

    emergency_contact      VARCHAR(255),

    project_preferences    TEXT,
    interests              TEXT,

    availability_start     DATE,
    availability_end       DATE,

    planned_absences_note  TEXT,

    supervisor_id          BIGINT,
    CONSTRAINT fk_employee_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES employees (id)
);

-- =========================
-- SKILLS
-- =========================

CREATE TABLE skills (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,  -- e.g. "Java", "Python", "German B2"
    category    VARCHAR(50),                   -- "Technical", "Language", ...
    description TEXT
);

-- =========================
-- EMPLOYEE_SKILLS
-- =========================

CREATE TABLE employee_skills (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    skill_id            BIGINT NOT NULL,
    skill_level         VARCHAR(20),   -- e.g. "JUNIOR", "MID", "SENIOR"
    years_of_experience INT,
    certification       VARCHAR(255),

    CONSTRAINT fk_emp_skill_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_emp_skill_skill
        FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT uq_employee_skill UNIQUE (employee_id, skill_id)
);

-- =========================
-- PROJECTS
-- =========================

CREATE TABLE projects (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    description          TEXT,
    task_description     TEXT,
    start_date           DATE,
    end_date             DATE,
    status               VARCHAR(30),      -- "OPEN", "STAFFING", "ACTIVE", "COMPLETED"
    location             VARCHAR(100),
    links                TEXT,

    project_manager_id   BIGINT,
    CONSTRAINT fk_project_manager
        FOREIGN KEY (project_manager_id) REFERENCES employees (id)
);

-- =========================
-- STAFFING_REQUIREMENTS
-- =========================

CREATE TABLE staffing_requirements (
    id                        BIGSERIAL PRIMARY KEY,
    project_id                BIGINT NOT NULL,
    required_role             VARCHAR(100) NOT NULL,
    number_of_employees       INT NOT NULL,
    capacity_hours_per_week   INT NOT NULL,
    priority                  INT,
    notes                     TEXT,

    CONSTRAINT fk_requirement_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

-- =========================
-- STAFFING_REQUIREMENT_SKILLS
-- =========================

CREATE TABLE staffing_requirement_skills (
    staffing_requirement_id BIGINT NOT NULL,
    skill_id                BIGINT NOT NULL,

    PRIMARY KEY (staffing_requirement_id, skill_id),

    CONSTRAINT fk_req_skill_requirement
        FOREIGN KEY (staffing_requirement_id)
        REFERENCES staffing_requirements (id),

    CONSTRAINT fk_req_skill_skill
        FOREIGN KEY (skill_id)
        REFERENCES skills (id)
);

-- =========================
-- PROJECT_ASSIGNMENTS
-- =========================

CREATE TABLE project_assignments (
    id                      BIGSERIAL PRIMARY KEY,
    project_id              BIGINT NOT NULL,
    employee_id             BIGINT NOT NULL,
    staffing_requirement_id BIGINT,

    role_in_project         VARCHAR(100),
    start_date              DATE,
    end_date                DATE,
    effort_hours_per_week   INT,

    status                  VARCHAR(30),     -- "PLANNED", "CONFIRMED", "ACTIVE", "COMPLETED"
    project_priority        INT,
    performance_rating      INT,
    feedback                TEXT,

    CONSTRAINT fk_assignment_project
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_assignment_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_assignment_requirement
        FOREIGN KEY (staffing_requirement_id)
        REFERENCES staffing_requirements (id)
);
