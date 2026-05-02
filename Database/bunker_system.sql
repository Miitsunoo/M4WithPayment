CREATE DATABASE bunker_system;

USE bunker_system;

CREATE TABLE occupants (
    occupant_id     INT PRIMARY KEY AUTO_INCREMENT,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    phone           VARCHAR(20),
    registered_at   DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 2. KEYCARDS
CREATE TABLE keycards (
    keycard_id      INT PRIMARY KEY AUTO_INCREMENT,
    occupant_id     INT          NOT NULL,
    keycard_code    VARCHAR(100) UNIQUE NOT NULL,
    is_active       BOOLEAN      DEFAULT TRUE,
    issued_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    access_level    INT          DEFAULT 1,
    FOREIGN KEY (occupant_id) REFERENCES occupants(occupant_id)
);

-- 3. ROOMS
CREATE TABLE rooms (
    room_id         INT PRIMARY KEY AUTO_INCREMENT,
    room_name       VARCHAR(100) NOT NULL,
    room_type       VARCHAR(50)  NOT NULL,   -- e.g. Sleeping, Work, Storage
    capacity        INT          NOT NULL,
    is_available    BOOLEAN      DEFAULT TRUE
);

-- 4. RESERVATIONS
CREATE TABLE reservations (
    reservation_id  INT PRIMARY KEY AUTO_INCREMENT,
    occupant_id     INT          NOT NULL,
    room_id         INT          NOT NULL,
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME     NOT NULL,
    status          VARCHAR(20)  DEFAULT 'Pending',  -- Pending, Approved, Cancelled
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (occupant_id) REFERENCES occupants(occupant_id),
    FOREIGN KEY (room_id)     REFERENCES rooms(room_id)
);

-- 5. WORK DUTIES
CREATE TABLE work_duties (
    duty_id         INT PRIMARY KEY AUTO_INCREMENT,
    occupant_id     INT          NOT NULL,
    duty_name       VARCHAR(100) NOT NULL,
    assigned_date   DATE         NOT NULL,
    shift           VARCHAR(20),                     -- Morning, Afternoon, Night
    status          VARCHAR(20)  DEFAULT 'Pending',  -- Pending, Ongoing, Completed
    FOREIGN KEY (occupant_id) REFERENCES occupants(occupant_id)
);

INSERT INTO occupants (occupant_id,first_name, last_name, email, phone, registered_at)
VALUES	(1, 'Aldrei', 'Domingo', 'mat.domingo08@gmail.com', '09397193219', NOW());

INSERT INTO keycards (occupant_id, keycard_code, is_active, issued_at, access_level)
VALUES (1, 'KC125', true, NOW(), 3);