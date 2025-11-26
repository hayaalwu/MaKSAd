DROP DATABASE IF EXISTS maksad;
CREATE DATABASE maksad CHARACTER SET utf8mb4;
USE maksad;

-- ===================== MAIN USERS TABLE =====================
CREATE TABLE maksad_users (
    admin_id       INT NULL,
    organizer_id   INT NULL,
    volunteer_id   INT NULL,

    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    phone          VARCHAR(30),
    password       VARCHAR(255) NOT NULL,
    gender         VARCHAR(10),
    date_of_birth  DATE,
    preferred_type VARCHAR(50),
    interests      JSON,
    skills         JSON,
    role           VARCHAR(50) NOT NULL,

    UNIQUE KEY uq_user_links (admin_id, organizer_id, volunteer_id)
);


-- ===================== ADMINS =====================
DROP TABLE IF EXISTS admins;
CREATE TABLE admins (
    admin_id       INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    is_super_admin TINYINT(1) NOT NULL DEFAULT 0,
    role_title     VARCHAR(100),
    last_login     DATETIME
);

-- ===================== ORGANIZERS =====================
DROP TABLE IF EXISTS organizers;
CREATE TABLE organizers (
    organizer_id INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    phone        VARCHAR(30),
    active       TINYINT(1) NOT NULL DEFAULT 1,
    role         VARCHAR(100)
);

-- ===================== VOLUNTEERS =====================
DROP TABLE IF EXISTS volunteers;
CREATE TABLE volunteers (
    volunteer_id   INT AUTO_INCREMENT PRIMARY KEY,
    volunteer_name VARCHAR(100) NOT NULL,
    total_hours    DOUBLE NOT NULL DEFAULT 0
);

-- ===================== EVENTS =====================
DROP TABLE IF EXISTS events;
CREATE TABLE events (
    event_id      INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    organizer_id  INT NOT NULL,
    category      VARCHAR(50) NOT NULL,
    location      VARCHAR(150) NOT NULL,
    volunteers    INT DEFAULT 0 CHECK (volunteers <= 100),
    event_date    DATE NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    description   TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT uq_event_name UNIQUE (name),
    CONSTRAINT fk_event_org 
        FOREIGN KEY (organizer_id) 
        REFERENCES organizers(organizer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===================== VOLUNTEER PARTICIPATIONS =====================
DROP TABLE IF EXISTS volunteer_participations;
CREATE TABLE volunteer_participations (
    volunteer_id   INT NOT NULL,
    volunteer_name VARCHAR(100) NOT NULL,
    event_name     VARCHAR(150) NOT NULL,
    event_date     DATE NOT NULL,
    role           VARCHAR(100),
    check_in       DATETIME,
    check_out      DATETIME,
    hours          DOUBLE,
    status         ENUM('PRESENT','ABSENT','CANCELED','UNSET') NOT NULL,

    CONSTRAINT fk_vp_volunteer
        FOREIGN KEY (volunteer_id)
        REFERENCES volunteers(volunteer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_vp_event
        FOREIGN KEY (event_name)
        REFERENCES events(name)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===================== CERTIFICATES =====================
DROP TABLE IF EXISTS certificates;
CREATE TABLE certificates (
    certificate_id INT AUTO_INCREMENT PRIMARY KEY,
    volunteer_id   INT NOT NULL,
    event_name     VARCHAR(150) NOT NULL,
    issue_date     DATE NOT NULL,
    hours_earned   DOUBLE NOT NULL,

    CONSTRAINT fk_cert_volunteer
        FOREIGN KEY (volunteer_id)
        REFERENCES volunteers(volunteer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_cert_event
        FOREIGN KEY (event_name)
        REFERENCES events(name)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===================== REPORTS =====================
DROP TABLE IF EXISTS reports;
CREATE TABLE reports (
    report_id      INT AUTO_INCREMENT PRIMARY KEY,
    report_type    VARCHAR(50) NOT NULL,
    report_title   VARCHAR(200) NOT NULL,
    date_generated DATETIME NOT NULL,
    generated_by   INT NOT NULL,
    CONSTRAINT fk_reports_admin
        FOREIGN KEY (generated_by)
        REFERENCES admins(admin_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===================== AUTO_INCREMENT =====================
ALTER TABLE volunteers  AUTO_INCREMENT = 224000001;
ALTER TABLE organizers  AUTO_INCREMENT = 225000001;
ALTER TABLE admins      AUTO_INCREMENT = 226000001;

-- ===================== FOREIGN KEYS =====================
ALTER TABLE maksad_users
    ADD CONSTRAINT fk_mu_admin
        FOREIGN KEY (admin_id)
        REFERENCES admins(admin_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    ADD CONSTRAINT fk_mu_organizer
        FOREIGN KEY (organizer_id)
        REFERENCES organizers(organizer_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    ADD CONSTRAINT fk_mu_volunteer
        FOREIGN KEY (volunteer_id)
        REFERENCES volunteers(volunteer_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;


-- ==========================================================
--               ORGANIZERS  (مع استبدال Haya)
-- ==========================================================

-- Organizer 1 (NEW instead of Haya)
INSERT INTO organizers (name, email, password, phone, active, role)
VALUES ('Rasha AlSaud', 'temp', 'RashaPW123', '0551111111', 1, 'Event Schedule Lead');
SET @oid = LAST_INSERT_ID();
UPDATE organizers SET email = CONCAT(@oid, '@maksad.org.sa') WHERE organizer_id = @oid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    NULL, @oid, NULL,
    'Rasha AlSaud', CONCAT(@oid, '@maksad.org.sa'),
    '0551111111', 'RashaPW123', 'Female', 'Organizer'
);

-- Organizer 2
INSERT INTO organizers (name, email, password, phone, active, role)
VALUES ('Jood Al Turki', 'temp', 'JoodPW123', '0552222222', 1, 'Event Planning Lead');
SET @oid = LAST_INSERT_ID();
UPDATE organizers SET email = CONCAT(@oid, '@maksad.org.sa') WHERE organizer_id = @oid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    NULL, @oid, NULL,
    'Jood Al Turki', CONCAT(@oid, '@maksad.org.sa'),
    '0552222222', 'JoodPW123', 'Female', 'Organizer'
);

-- Organizer 3
INSERT INTO organizers (name, email, password, phone, active, role)
VALUES ('Aljawharah Alghudiyan', 'temp', 'AljaPW123', '0553333333', 1, 'Media Lead');
SET @oid = LAST_INSERT_ID();
UPDATE organizers SET email = CONCAT(@oid, '@maksad.org.sa') WHERE organizer_id = @oid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    NULL, @oid, NULL,
    'Aljawharah Alghudiyan', CONCAT(@oid, '@maksad.org.sa'),
    '0553333333', 'AljaPW123', 'Female', 'Organizer'
);

-- Organizer 4
INSERT INTO organizers (name, email, password, phone, active, role)
VALUES ('Ahmad AlOtaibi', 'temp', 'AhmadPW123', '0554444444', 1, 'Volunteer Lead');
SET @oid = LAST_INSERT_ID();
UPDATE organizers SET email = CONCAT(@oid, '@maksad.org.sa') WHERE organizer_id = @oid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    NULL, @oid, NULL,
    'Ahmad AlOtaibi', CONCAT(@oid, '@maksad.org.sa'),
    '0554444444', 'AhmadPW123', 'Male', 'Organizer'
);

-- Organizer 5
INSERT INTO organizers (name, email, password, phone, active, role)
VALUES ('Abeer AlSubaie', 'temp', 'AbeerPW123', '0555555555', 1, 'Guest Relations Coordinator');
SET @oid = LAST_INSERT_ID();
UPDATE organizers SET email = CONCAT(@oid, '@maksad.org.sa') WHERE organizer_id = @oid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    NULL, @oid, NULL,
    'Abeer AlSubaie', CONCAT(@oid, '@maksad.org.sa'),
    '0555555555', 'AbeerPW123', 'Female', 'Organizer'
);



-- ==========================================================
--                      ADMINS
--          (Haya moved here + passwords updated)
-- ==========================================================

-- Admin 1
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Maha Asiri', 'temp', 'MahaPW123', 1, 'System Admin');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Maha Asiri', CONCAT(@aid, '@maksad.org.sa'),
    NULL, 'MahaPW123', 'Female', 'Admin'
);

-- Admin 2 (Khawlah)
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Khawlah Abaalkhail', 'temp', 'KhawlahPW123', 0, 'Operations Admin');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Khawlah Abaalkhail', CONCAT(@aid, '@maksad.org.sa'),
    NULL, 'KhawlahPW123', 'Female', 'Admin'
);

-- Admin 3 (Haya moved here)
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Haya Alwuhaibi', 'temp', 'HayaPW123', 0, 'Operations Admin');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Haya Alwuhaibi', CONCAT(@aid, '@maksad.org.sa'),
    '0551111111', 'HayaPW123', 'Female', 'Admin'
);

-- Admin 4
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Lama Aljalal', 'temp', 'LamaPW123', 0, 'Content Supervisor');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Lama Aljalal', CONCAT(@aid, '@maksad.org.sa'),
    NULL, 'LamaPW123', 'Female', 'Admin'
);

-- Admin 5
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Ghala Alghamdi', 'temp', 'GhalaPW123', 0, 'Finance Admin');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Ghala Alghamdi', CONCAT(@aid, '@maksad.org.sa'),
    NULL, 'GhalaPW123', 'Female', 'Admin'
);

-- Admin 6
INSERT INTO admins (name, email, password, is_super_admin, role_title)
VALUES ('Reemas Aleid', 'temp', 'ReemasPW123', 0, 'Audit Admin');
SET @aid = LAST_INSERT_ID();
UPDATE admins SET email = CONCAT(@aid, '@maksad.org.sa') WHERE admin_id = @aid;

INSERT INTO maksad_users (
    admin_id, organizer_id, volunteer_id,
    full_name, email, phone, password, gender, role
) VALUES (
    @aid, NULL, NULL,
    'Reemas Aleid', CONCAT(@aid, '@maksad.org.sa'),
    NULL, 'ReemasPW123', 'Female', 'Admin'
);