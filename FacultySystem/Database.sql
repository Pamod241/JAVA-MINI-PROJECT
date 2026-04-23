

CREATE DATABASE IF NOT EXISTS FacultyDB;
USE FacultyDB;



/* Department Table */
CREATE TABLE Department (
    Dep_id   VARCHAR(10)  PRIMARY KEY,
    D_name   VARCHAR(100) NOT NULL
);

/* Lecturer Table */
CREATE TABLE Lecturer (
    Lec_id   VARCHAR(10)  PRIMARY KEY,
    Fullname VARCHAR(100) NOT NULL,
    Email    VARCHAR(100) UNIQUE,
    Gender   ENUM('Male', 'Female', 'Other'),
    Dep_id   VARCHAR(10),
    FOREIGN KEY (Dep_id) REFERENCES Department(Dep_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

/* Course Table */
CREATE TABLE Course (
    C_code   VARCHAR(10)  PRIMARY KEY,
    C_name   VARCHAR(100) NOT NULL,
    Credit   INT,
    Type     ENUM('Theory', 'Practical'),
    Lec_id   VARCHAR(10),
    FOREIGN KEY (Lec_id) REFERENCES Lecturer(Lec_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

/* Student Table */
CREATE TABLE Student (
    Reg_no   VARCHAR(15)  PRIMARY KEY,
    Fullname VARCHAR(100) NOT NULL,
    DOB      DATE,
    Age      INT,
    Email    VARCHAR(100) UNIQUE,
    Type     ENUM('Proper', 'Repeat', 'BatchMissed'),
    Dep_id   VARCHAR(10),
    FOREIGN KEY (Dep_id) REFERENCES Department(Dep_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

/* Student-Course Relationship */
CREATE TABLE Stu_Course (
    C_code VARCHAR(10),
    Reg_no VARCHAR(15),
    PRIMARY KEY (C_code, Reg_no),
    FOREIGN KEY (C_code) REFERENCES Course(C_code)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (Reg_no) REFERENCES Student(Reg_no)
        ON DELETE CASCADE ON UPDATE CASCADE
);

/* Admin Table */
CREATE TABLE Admin (
    Admin_id VARCHAR(10)  PRIMARY KEY,
    Fullname VARCHAR(100),
    Email    VARCHAR(100)
);

/* Dean Table */
CREATE TABLE Dean (
    D_id     VARCHAR(10)  PRIMARY KEY,
    Fullname VARCHAR(100),
    Email    VARCHAR(100)
);

/* Technical Officer Table */
CREATE TABLE Technical_Officer (
    TO_id    VARCHAR(10)  PRIMARY KEY,
    Fullname VARCHAR(100),
    Email    VARCHAR(100)
);

/* User Table */
CREATE TABLE User (
    User_id  VARCHAR(10) PRIMARY KEY,
    Role     ENUM('Admin', 'Dean', 'Lecturer', 'Technical_Officer', 'Student'),
    Password VARCHAR(100) NOT NULL,
    Email    VARCHAR(100)
);

/* Medical Table */
CREATE TABLE Medical (
    medical_id  CHAR(10)    PRIMARY KEY,
    description VARCHAR(50),
    s_date      DATE,
    e_date      DATE,
    student_id  VARCHAR(15),
    FOREIGN KEY (student_id) REFERENCES Student(Reg_no)
        ON DELETE CASCADE ON UPDATE CASCADE
);

/* Attendance Table */
CREATE TABLE Attendence (
    att_id       VARCHAR(10) PRIMARY KEY,
    date         DATE,
    att_state    VARCHAR(20),
    session_type VARCHAR(25),
    hour         INT,
    student_id   VARCHAR(15),
    medical_id   CHAR(10),
    course_code  VARCHAR(10),
    FOREIGN KEY (student_id)  REFERENCES Student(Reg_no)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (course_code) REFERENCES Course(C_code)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (medical_id)  REFERENCES Medical(medical_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

/* Marks Table */
CREATE TABLE Mark (
    mark_id    CHAR(10)    PRIMARY KEY,
    quiz_1     INT DEFAULT 0,
    quiz_2     INT DEFAULT 0,
    quiz_3     INT DEFAULT 0,
    assesment  INT DEFAULT 0,
    mid        INT DEFAULT 0,
    end        INT DEFAULT 0,
    student_id VARCHAR(15),
    course_code CHAR(10),
    CONSTRAINT chk_marks CHECK (
        quiz_1    BETWEEN 0 AND 100 AND
        quiz_2    BETWEEN 0 AND 100 AND
        quiz_3    BETWEEN 0 AND 100 AND
        assesment BETWEEN 0 AND 100 AND
        mid       BETWEEN 0 AND 100 AND
        end       BETWEEN 0 AND 100
    ),
    FOREIGN KEY (course_code) REFERENCES Course(C_code)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id)  REFERENCES Student(Reg_no)
        ON DELETE CASCADE ON UPDATE CASCADE
);



/* Notice Table  */
CREATE TABLE Notice (
    notice_id   VARCHAR(10)  PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    created_by  VARCHAR(10)  NOT NULL,   -- Admin_id
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES Admin(Admin_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

/* Timetable Table  */
CREATE TABLE Timetable (
    tt_id        VARCHAR(10)  PRIMARY KEY,
    Dep_id       VARCHAR(10)  NOT NULL,
    day_of_week  ENUM('Monday','Tuesday','Wednesday','Thursday','Friday') NOT NULL,
    C_code       VARCHAR(10)  NOT NULL,
    start_time   TIME         NOT NULL,
    end_time     TIME         NOT NULL,
    location     VARCHAR(100),
    session_type ENUM('Theory','Practical') NOT NULL,
    FOREIGN KEY (Dep_id) REFERENCES Department(Dep_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (C_code) REFERENCES Course(C_code)
        ON DELETE CASCADE ON UPDATE CASCADE
);

/* Course Material Table  */
CREATE TABLE Course_Material (
    mat_id        VARCHAR(10)  PRIMARY KEY,
    C_code        VARCHAR(10)  NOT NULL,
    uploaded_by   VARCHAR(10)  NOT NULL,   -- Lec_id
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    file_url      VARCHAR(500),
    material_type ENUM('Lecture_Note','Assignment','Lab_Sheet','Reference','Other') NOT NULL DEFAULT 'Other',
    uploaded_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (C_code)       REFERENCES Course(C_code)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (uploaded_by)  REFERENCES Lecturer(Lec_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

/* Login Table  */

CREATE TABLE Login (
    login_id    VARCHAR(10)  PRIMARY KEY,
    User_id     VARCHAR(10)  NOT NULL UNIQUE,
    profile_id  VARCHAR(10)  NOT NULL,   -- matches Admin_id / Lec_id / TO_id / Reg_no / D_id
    role        ENUM('Admin','Dean','Lecturer','Technical_Officer','Student') NOT NULL,
    FOREIGN KEY (User_id) REFERENCES User(User_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);



/* Department */
INSERT INTO Department (Dep_id, D_name) VALUES
('D01', 'Information and Communication Technology'),
('D02', 'Engineering Technology'),
('D03', 'Biosystems Technology'),
('D04', 'Multidisciplinary Studies');

/* Lecturers */
INSERT INTO Lecturer (Lec_id, Fullname, Email, Gender, Dep_id) VALUES
('L001', 'Dr. P.H.P. Nuwan Laksiri',  'nuwan@tech.ruh.ac.lk',      'Male',   'D01'),
('L002', 'Dr. S. Perera',              'sperera@tech.ruh.ac.lk',    'Male',   'D01'),
('L003', 'Ms. I. Fernando',            'ifernando@tech.ruh.ac.lk',  'Female', 'D01'),
('L004', 'Mr. K. Silva',               'ksilva@tech.ruh.ac.lk',     'Male',   'D04'),
('L005', 'Dr. M. Jayasinghe',          'mjayasinghe@tech.ruh.ac.lk','Female', 'D01'),
('L006', 'Mr. S. Lakal',               'Lakal@tech.ruh.ac.lk',      'Male',   'D01'),
('L007', 'Dr. N. Kannangara',          'Kannangara@tech.ruh.ac.lk', 'Male',   'D04');

/* Courses */
INSERT INTO Course (C_code, C_name, Credit, Type, Lec_id) VALUES
('ENG1222', 'English',                                  2, 'Theory',    'L007'),
('ICT1212', 'Database Management Systems',              2, 'Theory',    'L001'),
('ICT1222', 'Database Management Systems Practicum',    2, 'Practical', 'L001'),
('ICT1233', 'Server Side Web Development',              3, 'Theory',    'L002'),
('ICT1242', 'Computer Architecture',                    2, 'Theory',    'L006'),
('ICT1253', 'Computer Networks',                        3, 'Theory',    'L005'),
('TCS1212', 'Fundamentals of Management',               2, 'Theory',    'L004'),
('TMS1233', 'Discrete Mathematics',                     3, 'Theory',    'L002');

/* Students */
INSERT INTO Student (Reg_no, Fullname, DOB, Age, Email, Type, Dep_id) VALUES
('TG0001', 'Kavindu Perera',        '2002-01-12', 23, 'kavindu@stu.ruh.ac.lk',   'Proper',    'D01'),
('TG0002', 'Nimesha Fernando',      '2003-03-18', 22, 'nimesha@stu.ruh.ac.lk',   'Proper',    'D01'),
('TG0003', 'Sithum Bandara',        '2002-07-02', 23, 'sithum@stu.ruh.ac.lk',    'Proper',    'D01'),
('TG0004', 'Sanduni Gamage',        '2003-01-28', 22, 'sanduni@stu.ruh.ac.lk',   'Proper',    'D01'),
('TG0005', 'Amasha Dilrukshi',      '2002-11-10', 23, 'amasha@stu.ruh.ac.lk',    'Proper',    'D01'),
('TG0006', 'Isuru Ranasinghe',      '2002-05-21', 23, 'isuru@stu.ruh.ac.lk',     'Repeat',    'D01'),
('TG0007', 'Hiruni Madushika',      '2003-02-16', 22, 'hiruni@stu.ruh.ac.lk',    'Repeat',    'D01'),
('TG0008', 'Pasindu Lakshan',       '2002-06-11', 23, 'pasindu@stu.ruh.ac.lk',   'Repeat',    'D01'),
('TG0009', 'Bimsara Jayasuriya',    '2003-04-09', 22, 'bimsara@stu.ruh.ac.lk',   'Repeat',    'D01'),
('TG0010', 'Mihiri De Silva',       '2002-09-13', 23, 'mihiri@stu.ruh.ac.lk',    'Repeat',    'D01'),
('TG0011', 'Sajith Abeywickrama',   '2002-08-15', 23, 'sajith@stu.ruh.ac.lk',    'Proper',    'D01'),
('TG0012', 'Chathura Weerasinghe',  '2003-01-10', 22, 'chathura@stu.ruh.ac.lk',  'Proper',    'D01'),
('TG0013', 'Naduni Samarasekara',   '2003-06-02', 22, 'naduni@stu.ruh.ac.lk',    'Proper',    'D01'),
('TG0014', 'Dinuka Rajapaksha',     '2003-07-18', 22, 'dinuka@stu.ruh.ac.lk',    'Proper',    'D01'),
('TG0015', 'Hashini Rathnayake',    '2002-10-22', 23, 'hashini@stu.ruh.ac.lk',   'Proper',    'D01'),
('TG0016', 'Asanka Dissanayake',    '2002-03-05', 23, 'asanka@stu.ruh.ac.lk',    'BatchMissed', 'D01'),
('TG0017', 'Buddhika Wijesooriya',  '2003-08-19', 22, 'buddhika@stu.ruh.ac.lk',  'BatchMissed', 'D01');

/* Student-Course enrolments  */
INSERT INTO Stu_Course (C_code, Reg_no) VALUES
('ENG1222','TG0001'),('ICT1212','TG0001'),('ICT1222','TG0001'),('ICT1233','TG0001'),
('ICT1242','TG0001'),('ICT1253','TG0001'),('TCS1212','TG0001'),('TMS1233','TG0001'),

('ENG1222','TG0002'),('ICT1212','TG0002'),('ICT1222','TG0002'),('ICT1233','TG0002'),
('ICT1242','TG0002'),('ICT1253','TG0002'),('TCS1212','TG0002'),('TMS1233','TG0002'),

('ENG1222','TG0003'),('ICT1212','TG0003'),('ICT1222','TG0003'),('ICT1233','TG0003'),
('ICT1242','TG0003'),('ICT1253','TG0003'),('TCS1212','TG0003'),('TMS1233','TG0003'),

('ENG1222','TG0004'),('ICT1212','TG0004'),('ICT1222','TG0004'),('ICT1233','TG0004'),
('ICT1242','TG0004'),('ICT1253','TG0004'),('TCS1212','TG0004'),('TMS1233','TG0004'),

('ENG1222','TG0005'),('ICT1212','TG0005'),('ICT1222','TG0005'),('ICT1233','TG0005'),
('ICT1242','TG0005'),('ICT1253','TG0005'),('TCS1212','TG0005'),('TMS1233','TG0005'),

('ENG1222','TG0006'),('ICT1212','TG0006'),('ICT1222','TG0006'),('ICT1233','TG0006'),
('ICT1242','TG0006'),('ICT1253','TG0006'),('TCS1212','TG0006'),('TMS1233','TG0006'),

('ENG1222','TG0007'),('ICT1212','TG0007'),('ICT1222','TG0007'),('ICT1233','TG0007'),
('ICT1242','TG0007'),('ICT1253','TG0007'),('TCS1212','TG0007'),('TMS1233','TG0007'),

('ENG1222','TG0008'),('ICT1212','TG0008'),('ICT1222','TG0008'),('ICT1233','TG0008'),
('ICT1242','TG0008'),('ICT1253','TG0008'),('TCS1212','TG0008'),('TMS1233','TG0008'),

('ENG1222','TG0009'),('ICT1212','TG0009'),('ICT1222','TG0009'),('ICT1233','TG0009'),
('ICT1242','TG0009'),('ICT1253','TG0009'),('TCS1212','TG0009'),('TMS1233','TG0009'),

('ENG1222','TG0010'),('ICT1212','TG0010'),('ICT1222','TG0010'),('ICT1233','TG0010'),
('ICT1242','TG0010'),('ICT1253','TG0010'),('TCS1212','TG0010'),('TMS1233','TG0010'),

('ENG1222','TG0011'),('ICT1212','TG0011'),('ICT1222','TG0011'),('ICT1233','TG0011'),
('ICT1242','TG0011'),('ICT1253','TG0011'),('TCS1212','TG0011'),('TMS1233','TG0011'),

('ENG1222','TG0012'),('ICT1212','TG0012'),('ICT1222','TG0012'),('ICT1233','TG0012'),
('ICT1242','TG0012'),('ICT1253','TG0012'),('TCS1212','TG0012'),('TMS1233','TG0012'),

('ENG1222','TG0013'),('ICT1212','TG0013'),('ICT1222','TG0013'),('ICT1233','TG0013'),
('ICT1242','TG0013'),('ICT1253','TG0013'),('TCS1212','TG0013'),('TMS1233','TG0013'),

('ENG1222','TG0014'),('ICT1212','TG0014'),('ICT1222','TG0014'),('ICT1233','TG0014'),
('ICT1242','TG0014'),('ICT1253','TG0014'),('TCS1212','TG0014'),('TMS1233','TG0014'),

('ENG1222','TG0015'),('ICT1212','TG0015'),('ICT1222','TG0015'),('ICT1233','TG0015'),
('ICT1242','TG0015'),('ICT1253','TG0015'),('TCS1212','TG0015'),('TMS1233','TG0015');

/* Admin */
INSERT INTO Admin VALUES
('A001', 'System Administrator', 'admin@tech.ruh.ac.lk');

/* Dean */
INSERT INTO Dean VALUES
('DE01', 'Prof. H. Senanayake', 'dean@tech.ruh.ac.lk');

/* Technical Officers */
INSERT INTO Technical_Officer VALUES
('TO01', 'Kasun Wijesinghe',    'kasun.to@tech.ruh.ac.lk'),
('TO02', 'Nadeesha Fernando',   'nadeesha.to@tech.ruh.ac.lk'),
('TO03', 'Ruwan Jayalath',      'ruwan.to@tech.ruh.ac.lk'),
('TO04', 'Dilini Samarasekara', 'dilini.to@tech.ruh.ac.lk'),
('TO05', 'Tharaka Perera',      'tharaka.to@tech.ruh.ac.lk');

/* User accounts */
INSERT INTO User VALUES
('U001', 'Admin',            'adminpwd',       'admin@tech.ruh.ac.lk'),
('U002', 'Dean',             'deanpwdd',       'dean@tech.ruh.ac.lk'),
('U003', 'Lecturer',         'lecturepwd',     'nuwan@tech.ruh.ac.lk'),
('U004', 'Technical_Officer','techofficerpwd', 'kasun.to@tech.ruh.ac.lk'),
('U005', 'Student',          'studentpwd',     'kavindu@stu.ruh.ac.lk'),
('U006', 'Student',          'studentpwd',     'nimesha@stu.ruh.ac.lk'),
('U007', 'Student',          'studentpwd',     'sithum@stu.ruh.ac.lk'),
('U008', 'Student',          'studentpwd',     'sanduni@stu.ruh.ac.lk'),
('U009', 'Student',          'studentpwd',     'amasha@stu.ruh.ac.lk'),
('U010', 'Student',          'studentpwd',     'isuru@stu.ruh.ac.lk'),
('U011', 'Student',          'studentpwd',     'hiruni@stu.ruh.ac.lk'),
('U012', 'Student',          'studentpwd',     'pasindu@stu.ruh.ac.lk'),
('U013', 'Student',          'studentpwd',     'bimsara@stu.ruh.ac.lk'),
('U014', 'Student',          'studentpwd',     'mihiri@stu.ruh.ac.lk'),
('U015', 'Student',          'studentpwd',     'sajith@stu.ruh.ac.lk'),
('U016', 'Student',          'studentpwd',     'chathura@stu.ruh.ac.lk'),
('U017', 'Student',          'studentpwd',     'naduni@stu.ruh.ac.lk'),
('U018', 'Student',          'studentpwd',     'dinuka@stu.ruh.ac.lk'),
('U019', 'Student',          'studentpwd',     'hashini@stu.ruh.ac.lk'),
('U020', 'Student',          'studentpwd',     'asanka@stu.ruh.ac.lk'),
('U021', 'Student',          'studentpwd',     'buddhika@stu.ruh.ac.lk');

/* Login — maps User accounts to role-specific profile IDs */
INSERT INTO Login VALUES
('LG001', 'U001', 'A001', 'Admin'),
('LG002', 'U002', 'DE01', 'Dean'),
('LG003', 'U003', 'L001', 'Lecturer'),
('LG004', 'U004', 'TO01', 'Technical_Officer'),
('LG005', 'U005', 'TG0001', 'Student'),
('LG006', 'U006', 'TG0002', 'Student'),
('LG007', 'U007', 'TG0003', 'Student'),
('LG008', 'U008', 'TG0004', 'Student'),
('LG009', 'U009', 'TG0005', 'Student'),
('LG010', 'U010', 'TG0006', 'Student'),
('LG011', 'U011', 'TG0007', 'Student'),
('LG012', 'U012', 'TG0008', 'Student'),
('LG013', 'U013', 'TG0009', 'Student'),
('LG014', 'U014', 'TG0010', 'Student'),
('LG015', 'U015', 'TG0011', 'Student'),
('LG016', 'U016', 'TG0012', 'Student'),
('LG017', 'U017', 'TG0013', 'Student'),
('LG018', 'U018', 'TG0014', 'Student'),
('LG019', 'U019', 'TG0015', 'Student'),
('LG020', 'U020', 'TG0016', 'Student'),
('LG021', 'U021', 'TG0017', 'Student');

/* Medicals */
INSERT INTO Medical VALUES
('MD001', 'sick', '2025-08-06', '2025-08-08', 'TG0004'),
('MD002', 'sick', '2025-08-09', '2025-08-15', 'TG0015'),
('MD003', 'sick', '2025-08-27', '2025-08-27', 'TG0015'),
('MD004', 'sick', '2025-08-14', '2025-08-16', 'TG0014'),
('MD005', 'sick', '2025-08-15', '2025-08-22', 'TG0013'),
('MD006', 'sick', '2025-08-28', '2025-08-30', 'TG0002'),
('MD007', 'sick', '2025-09-16', '2025-09-18', 'TG0004'),
('MD008', 'sick', '2025-09-24', '2025-09-24', 'TG0004'),
('MD009', 'sick', '2025-11-15', '2025-11-15', 'TG0008'),
('MD010', 'sick', '2025-11-15', '2025-11-15', 'TG0001');

/* Notices — posted by Admin */
INSERT INTO Notice VALUES
('N001', 'Final Exam Schedule Released',
 'Final examinations for Semester I will commence on 5th January 2026. All undergraduates must check their individual timetables and confirm venue allocations with their department coordinator.',
 'A001', '2025-11-10 09:00:00'),

('N002', 'Medical Certificate Submission Deadline',
 'Medical certificates for absences must be submitted to the Technical Officer within 7 working days of the absence. Late submissions will not be considered for attendance eligibility.',
 'A001', '2025-11-10 09:15:00'),

('N003', 'Library Extended Hours — Exam Period',
 'The university library will remain open until 10 PM on weekdays and 6 PM on weekends throughout the examination period (1st December 2025 – 15th January 2026).',
 'A001', '2025-11-12 08:30:00'),

('N004', 'Semester II Course Registration',
 'Course registration for Semester II opens on 20th January 2026. Students must complete registration by 31st January 2026. Contact your academic advisor for module selection guidance.',
 'A001', '2025-11-15 10:00:00'),

('N005', 'Lab Safety Reminder',
 'All students must wear appropriate footwear and follow lab safety protocols when working in the computer laboratories. Eating and drinking inside labs is strictly prohibited.',
 'A001', '2025-11-18 08:00:00');

/* Timetable */
INSERT INTO Timetable VALUES
('TT001', 'D01', 'Monday',    'ICT1212', '08:00:00', '10:00:00', 'Hall B1',    'Theory'),
('TT002', 'D01', 'Monday',    'ICT1222', '10:00:00', '14:00:00', 'Lab 01',     'Practical'),
('TT003', 'D01', 'Tuesday',   'TMS1233', '08:00:00', '11:00:00', 'Hall A2',    'Theory'),
('TT004', 'D01', 'Tuesday',   'ICT1242', '11:00:00', '13:00:00', 'Hall B2',    'Theory'),
('TT005', 'D01', 'Wednesday', 'ICT1253', '08:00:00', '12:00:00', 'Hall A1',    'Theory'),
('TT006', 'D01', 'Wednesday', 'ICT1233', '13:00:00', '17:00:00', 'Lab 02',     'Theory'),
('TT007', 'D01', 'Thursday',  'ENG1222', '08:00:00', '10:00:00', 'Hall C1',    'Theory'),
('TT008', 'D01', 'Thursday',  'TCS1212', '10:00:00', '12:00:00', 'Hall C2',    'Theory'),
('TT009', 'D04', 'Friday',    'ENG1222', '09:00:00', '11:00:00', 'Hall D1',    'Theory'),
('TT010', 'D04', 'Friday',    'TCS1212', '11:00:00', '13:00:00', 'Hall D2',    'Theory');

/* Course Materials — uploaded by lecturers */
INSERT INTO Course_Material VALUES
('CM001', 'ICT1212', 'L001', 'Week 01 - Introduction to Databases',
 'Covers relational model, keys, and basic SQL concepts.',
 'https://lms.ruh.ac.lk/ict1212/week01.pdf', 'Lecture_Note', '2025-08-07 10:00:00'),

('CM002', 'ICT1212', 'L001', 'Week 02 - ER Diagrams',
 'Entity-Relationship modelling and mapping to relational schema.',
 'https://lms.ruh.ac.lk/ict1212/week02.pdf', 'Lecture_Note', '2025-08-14 10:00:00'),

('CM003', 'ICT1212', 'L001', 'Assignment 01 - Database Design',
 'Design a database for a library management system. Due: 2025-09-10.',
 'https://lms.ruh.ac.lk/ict1212/assign01.pdf', 'Assignment', '2025-08-20 09:00:00'),

('CM004', 'ICT1222', 'L001', 'Lab Sheet 01 - Basic SQL Queries',
 'SELECT, WHERE, ORDER BY, GROUP BY exercises.',
 'https://lms.ruh.ac.lk/ict1222/lab01.pdf', 'Lab_Sheet', '2025-08-06 14:00:00'),

('CM005', 'ICT1222', 'L001', 'Lab Sheet 02 - Joins and Subqueries',
 'INNER JOIN, LEFT JOIN, nested SELECT exercises.',
 'https://lms.ruh.ac.lk/ict1222/lab02.pdf', 'Lab_Sheet', '2025-08-13 14:00:00'),

('CM006', 'ICT1233', 'L002', 'Week 01 - Introduction to Server-Side Development',
 'Overview of web architecture, HTTP, and PHP basics.',
 'https://lms.ruh.ac.lk/ict1233/week01.pdf', 'Lecture_Note', '2025-08-12 13:00:00'),

('CM007', 'TMS1233', 'L002', 'Week 01 - Sets and Logic',
 'Set theory, propositional logic, and truth tables.',
 'https://lms.ruh.ac.lk/tms1233/week01.pdf', 'Lecture_Note', '2025-08-07 08:00:00'),

('CM008', 'ICT1253', 'L005', 'Week 01 - Network Fundamentals',
 'OSI model, TCP/IP stack, and network topologies.',
 'https://lms.ruh.ac.lk/ict1253/week01.pdf', 'Lecture_Note', '2025-08-08 08:00:00'),

('CM009', 'ICT1242', 'L006', 'Week 01 - Computer Organisation',
 'Von Neumann architecture, CPU components, memory hierarchy.',
 'https://lms.ruh.ac.lk/ict1242/week01.pdf', 'Lecture_Note', '2025-08-08 11:00:00'),

('CM010', 'ENG1222', 'L007', 'Academic Writing Reference Guide',
 'Style guide for academic reports and presentations.',
 'https://lms.ruh.ac.lk/eng1222/writing_guide.pdf', 'Reference', '2025-08-09 09:00:00');

/* ── ATTENDANCE DATA  */
INSERT INTO Attendence VALUES
('A001','2025-08-06','Present','Lecture',2,'TG0001',NULL,'ICT1212'),
('A002','2025-08-06','Present','Lecture',2,'TG0002',NULL,'ICT1212'),
('A003','2025-08-06','Present','Lecture',2,'TG0003',NULL,'ICT1212'),
('A004','2025-08-06','Absent','Lecture',2,'TG0004','MD001','ICT1212'),
('A005','2025-08-06','Present','Lecture',2,'TG0005',NULL,'ICT1212'),
('A006','2025-08-06','Present','Lecture',2,'TG0006',NULL,'ICT1212'),
('A007','2025-08-06','Present','Lecture',2,'TG0007',NULL,'ICT1212'),
('A008','2025-08-06','Present','Lecture',2,'TG0008',NULL,'ICT1212'),
('A009','2025-08-06','Present','Lecture',2,'TG0009',NULL,'ICT1212'),
('A010','2025-08-06','Present','Lecture',2,'TG0010',NULL,'ICT1212'),
('A011','2025-08-06','Present','Lecture',2,'TG0011',NULL,'ICT1212'),
('A012','2025-08-06','Present','Lecture',2,'TG0012',NULL,'ICT1212'),
('A013','2025-08-06','Present','Lecture',2,'TG0013',NULL,'ICT1212'),
('A014','2025-08-06','Present','Lecture',2,'TG0014',NULL,'ICT1212'),
('A015','2025-08-06','Present','Lecture',2,'TG0015',NULL,'ICT1212'),
('A016','2025-08-06','Present','Practical',4,'TG0001',NULL,'ICT1222'),
('A017','2025-08-06','Present','Practical',4,'TG0002',NULL,'ICT1222'),
('A018','2025-08-06','Present','Practical',4,'TG0003',NULL,'ICT1222'),
('A019','2025-08-06','Absent','Practical',4,'TG0004','MD001','ICT1222'),
('A020','2025-08-06','Present','Practical',4,'TG0005',NULL,'ICT1222'),
('A021','2025-08-06','Present','Practical',4,'TG0006',NULL,'ICT1222'),
('A022','2025-08-06','Present','Practical',4,'TG0007',NULL,'ICT1222'),
('A023','2025-08-06','Present','Practical',4,'TG0008',NULL,'ICT1222'),
('A024','2025-08-06','Present','Practical',4,'TG0009',NULL,'ICT1222'),
('A025','2025-08-06','Present','Practical',4,'TG0010',NULL,'ICT1222'),
('A026','2025-08-06','Present','Practical',4,'TG0011',NULL,'ICT1222'),
('A027','2025-08-06','Present','Practical',4,'TG0012',NULL,'ICT1222'),
('A028','2025-08-06','Present','Practical',4,'TG0013',NULL,'ICT1222'),
('A029','2025-08-06','Present','Practical',4,'TG0014',NULL,'ICT1222'),
('A030','2025-08-06','Present','Practical',4,'TG0015',NULL,'ICT1222'),
('A031','2025-08-07','Present','Lecture',3,'TG0001',NULL,'TMS1233'),
('A032','2025-08-07','Present','Lecture',3,'TG0002',NULL,'TMS1233'),
('A033','2025-08-07','Present','Lecture',3,'TG0003',NULL,'TMS1233'),
('A034','2025-08-07','Absent','Lecture',3,'TG0004','MD001','TMS1233'),
('A035','2025-08-07','Present','Lecture',3,'TG0005',NULL,'TMS1233'),
('A036','2025-08-07','Present','Lecture',3,'TG0006',NULL,'TMS1233'),
('A037','2025-08-07','Present','Lecture',3,'TG0007',NULL,'TMS1233'),
('A038','2025-08-07','Present','Lecture',3,'TG0008',NULL,'TMS1233'),
('A039','2025-08-07','Present','Lecture',3,'TG0009',NULL,'TMS1233'),
('A040','2025-08-07','Present','Lecture',3,'TG0010',NULL,'TMS1233'),
('A041','2025-08-07','Present','Lecture',3,'TG0011',NULL,'TMS1233'),
('A042','2025-08-07','Present','Lecture',3,'TG0012',NULL,'TMS1233'),
('A043','2025-08-07','Present','Lecture',3,'TG0013',NULL,'TMS1233'),
('A044','2025-08-07','Absent','Lecture',3,'TG0014',NULL,'TMS1233'),
('A045','2025-08-07','Present','Lecture',3,'TG0015',NULL,'TMS1233'),
('A046','2025-08-08','Present','Lecture',2,'TG0001',NULL,'ICT1242'),
('A047','2025-08-08','Present','Lecture',2,'TG0002',NULL,'ICT1242'),
('A048','2025-08-08','Present','Lecture',2,'TG0003',NULL,'ICT1242'),
('A049','2025-08-08','Absent','Lecture',2,'TG0004','MD001','ICT1242'),
('A050','2025-08-08','Present','Lecture',2,'TG0005',NULL,'ICT1242'),
('A051','2025-08-08','Present','Lecture',2,'TG0006',NULL,'ICT1242'),
('A052','2025-08-08','Present','Lecture',2,'TG0007',NULL,'ICT1242'),
('A053','2025-08-08','Present','Lecture',2,'TG0008',NULL,'ICT1242'),
('A054','2025-08-08','Present','Lecture',2,'TG0009',NULL,'ICT1242'),
('A055','2025-08-08','Present','Lecture',2,'TG0010',NULL,'ICT1242'),
('A056','2025-08-08','Present','Lecture',2,'TG0011',NULL,'ICT1242'),
('A057','2025-08-08','Present','Lecture',2,'TG0012',NULL,'ICT1242'),
('A058','2025-08-08','Present','Lecture',2,'TG0013',NULL,'ICT1242'),
('A059','2025-08-08','Present','Lecture',2,'TG0014',NULL,'ICT1242'),
('A060','2025-08-08','Present','Lecture',2,'TG0015',NULL,'ICT1242'),
('A061','2025-08-08','Present','Lecture',4,'TG0001',NULL,'ICT1253'),
('A062','2025-08-08','Present','Lecture',4,'TG0002',NULL,'ICT1253'),
('A063','2025-08-08','Present','Lecture',4,'TG0003',NULL,'ICT1253'),
('A064','2025-08-08','Absent','Lecture',4,'TG0004','MD001','ICT1253'),
('A065','2025-08-08','Present','Lecture',4,'TG0005',NULL,'ICT1253'),
('A066','2025-08-08','Present','Lecture',4,'TG0006',NULL,'ICT1253'),
('A067','2025-08-08','Present','Lecture',4,'TG0007',NULL,'ICT1253'),
('A068','2025-08-08','Present','Lecture',4,'TG0008',NULL,'ICT1253'),
('A069','2025-08-08','Present','Lecture',4,'TG0009',NULL,'ICT1253'),
('A070','2025-08-08','Present','Lecture',4,'TG0010',NULL,'ICT1253'),
('A071','2025-08-08','Present','Lecture',4,'TG0011',NULL,'ICT1253'),
('A072','2025-08-08','Present','Lecture',4,'TG0012',NULL,'ICT1253'),
('A073','2025-08-08','Present','Lecture',4,'TG0013',NULL,'ICT1253'),
('A074','2025-08-08','Present','Lecture',4,'TG0014',NULL,'ICT1253'),
('A075','2025-08-08','Present','Lecture',4,'TG0015',NULL,'ICT1253'),
('A076','2025-08-09','Present','Lecture',2,'TG0001',NULL,'ENG1222'),
('A077','2025-08-09','Present','Lecture',2,'TG0002',NULL,'ENG1222'),
('A078','2025-08-09','Present','Lecture',2,'TG0003',NULL,'ENG1222'),
('A079','2025-08-09','Present','Lecture',2,'TG0004',NULL,'ENG1222'),
('A080','2025-08-09','Present','Lecture',2,'TG0005',NULL,'ENG1222'),
('A081','2025-08-09','Present','Lecture',2,'TG0006',NULL,'ENG1222'),
('A082','2025-08-09','Present','Lecture',2,'TG0007',NULL,'ENG1222'),
('A083','2025-08-09','Present','Lecture',2,'TG0008',NULL,'ENG1222'),
('A084','2025-08-09','Present','Lecture',2,'TG0009',NULL,'ENG1222'),
('A085','2025-08-09','Present','Lecture',2,'TG0010',NULL,'ENG1222'),
('A086','2025-08-09','Present','Lecture',2,'TG0011',NULL,'ENG1222'),
('A087','2025-08-09','Present','Lecture',2,'TG0012',NULL,'ENG1222'),
('A088','2025-08-09','Present','Lecture',2,'TG0013',NULL,'ENG1222'),
('A089','2025-08-09','Present','Lecture',2,'TG0014',NULL,'ENG1222'),
('A090','2025-08-09','Absent','Lecture',2,'TG0015','MD002','ENG1222'),
('A091','2025-08-09','Present','Lecture',2,'TG0001',NULL,'TCS1212'),
('A092','2025-08-09','Present','Lecture',2,'TG0002',NULL,'TCS1212'),
('A093','2025-08-09','Present','Lecture',2,'TG0003',NULL,'TCS1212'),
('A094','2025-08-09','Present','Lecture',2,'TG0004',NULL,'TCS1212'),
('A095','2025-08-09','Present','Lecture',2,'TG0005',NULL,'TCS1212'),
('A096','2025-08-09','Present','Lecture',2,'TG0006',NULL,'TCS1212'),
('A097','2025-08-09','Present','Lecture',2,'TG0007',NULL,'TCS1212'),
('A098','2025-08-09','Present','Lecture',2,'TG0008',NULL,'TCS1212'),
('A099','2025-08-09','Present','Lecture',2,'TG0009',NULL,'TCS1212'),
('A100','2025-08-09','Present','Lecture',2,'TG0010',NULL,'TCS1212'),
('A101','2025-08-09','Present','Lecture',2,'TG0011',NULL,'TCS1212'),
('A102','2025-08-09','Present','Lecture',2,'TG0012',NULL,'TCS1212'),
('A103','2025-08-09','Present','Lecture',2,'TG0013',NULL,'TCS1212'),
('A104','2025-08-09','Present','Lecture',2,'TG0014',NULL,'TCS1212'),
('A105','2025-08-09','Present','Lecture',2,'TG0015',NULL,'TCS1212'),
('A106','2025-08-12','Present','Lecture',4,'TG0001',NULL,'ICT1233'),
('A107','2025-08-12','Present','Lecture',4,'TG0002',NULL,'ICT1233'),
('A108','2025-08-12','Present','Lecture',4,'TG0003',NULL,'ICT1233'),
('A109','2025-08-12','Present','Lecture',4,'TG0004',NULL,'ICT1233'),
('A110','2025-08-12','Present','Lecture',4,'TG0005',NULL,'ICT1233'),
('A111','2025-08-12','Present','Lecture',4,'TG0006',NULL,'ICT1233'),
('A112','2025-08-12','Present','Lecture',4,'TG0007',NULL,'ICT1233'),
('A113','2025-08-12','Present','Lecture',4,'TG0008',NULL,'ICT1233'),
('A114','2025-08-12','Present','Lecture',4,'TG0009',NULL,'ICT1233'),
('A115','2025-08-12','Present','Lecture',4,'TG0010',NULL,'ICT1233'),
('A116','2025-08-12','Present','Lecture',4,'TG0011',NULL,'ICT1233'),
('A117','2025-08-12','Present','Lecture',4,'TG0012',NULL,'ICT1233'),
('A118','2025-08-12','Present','Lecture',4,'TG0013',NULL,'ICT1233'),
('A119','2025-08-12','Present','Lecture',4,'TG0014',NULL,'ICT1233'),
('A120','2025-08-12','Absent','Lecture',4,'TG0015','MD002','ICT1233'),
('A121','2025-08-13','Present','Lecture',2,'TG0001',NULL,'ICT1212'),
('A122','2025-08-13','Present','Lecture',2,'TG0002',NULL,'ICT1212'),
('A123','2025-08-13','Present','Lecture',2,'TG0003',NULL,'ICT1212'),
('A124','2025-08-13','Present','Lecture',2,'TG0004',NULL,'ICT1212'),
('A125','2025-08-13','Present','Lecture',2,'TG0005',NULL,'ICT1212'),
('A126','2025-08-13','Present','Lecture',2,'TG0006',NULL,'ICT1212'),
('A127','2025-08-13','Present','Lecture',2,'TG0007',NULL,'ICT1212'),
('A128','2025-08-13','Present','Lecture',2,'TG0008',NULL,'ICT1212'),
('A129','2025-08-13','Present','Lecture',2,'TG0009',NULL,'ICT1212'),
('A130','2025-08-13','Present','Lecture',2,'TG0010',NULL,'ICT1212'),
('A131','2025-08-13','Present','Lecture',2,'TG0011',NULL,'ICT1212'),
('A132','2025-08-13','Present','Lecture',2,'TG0012',NULL,'ICT1212'),
('A133','2025-08-13','Present','Lecture',2,'TG0013',NULL,'ICT1212'),
('A134','2025-08-13','Present','Lecture',2,'TG0014',NULL,'ICT1212'),
('A135','2025-08-13','Absent','Lecture',2,'TG0015','MD002','ICT1212'),
('A136','2025-08-13','Present','Practical',4,'TG0001',NULL,'ICT1222'),
('A137','2025-08-13','Present','Practical',4,'TG0002',NULL,'ICT1222'),
('A138','2025-08-13','Present','Practical',4,'TG0003',NULL,'ICT1222'),
('A139','2025-08-13','Present','Practical',4,'TG0004',NULL,'ICT1222'),
('A140','2025-08-13','Present','Practical',4,'TG0005',NULL,'ICT1222'),
('A141','2025-08-13','Present','Practical',4,'TG0006',NULL,'ICT1222'),
('A142','2025-08-13','Present','Practical',4,'TG0007',NULL,'ICT1222'),
('A143','2025-08-13','Present','Practical',4,'TG0008',NULL,'ICT1222'),
('A144','2025-08-13','Present','Practical',4,'TG0009',NULL,'ICT1222'),
('A145','2025-08-13','Present','Practical',4,'TG0010',NULL,'ICT1222'),
('A146','2025-08-13','Present','Practical',4,'TG0011',NULL,'ICT1222'),
('A147','2025-08-13','Present','Practical',4,'TG0012',NULL,'ICT1222'),
('A148','2025-08-13','Present','Practical',4,'TG0013',NULL,'ICT1222'),
('A149','2025-08-13','Present','Practical',4,'TG0014',NULL,'ICT1222'),
('A150','2025-08-13','Absent','Practical',4,'TG0015','MD002','ICT1222'),
('A151','2025-08-14','Present','Lecture',3,'TG0001',NULL,'TMS1233'),
('A152','2025-08-14','Present','Lecture',3,'TG0002',NULL,'TMS1233'),
('A153','2025-08-14','Present','Lecture',3,'TG0003',NULL,'TMS1233'),
('A154','2025-08-14','Present','Lecture',3,'TG0004',NULL,'TMS1233'),
('A155','2025-08-14','Present','Lecture',3,'TG0005',NULL,'TMS1233'),
('A156','2025-08-14','Present','Lecture',3,'TG0006',NULL,'TMS1233'),
('A157','2025-08-14','Present','Lecture',3,'TG0007',NULL,'TMS1233'),
('A158','2025-08-14','Present','Lecture',3,'TG0008',NULL,'TMS1233'),
('A159','2025-08-14','Present','Lecture',3,'TG0009',NULL,'TMS1233'),
('A160','2025-08-14','Present','Lecture',3,'TG0010',NULL,'TMS1233'),
('A161','2025-08-14','Present','Lecture',3,'TG0011',NULL,'TMS1233'),
('A162','2025-08-14','Present','Lecture',3,'TG0012',NULL,'TMS1233'),
('A163','2025-08-14','Present','Lecture',3,'TG0013',NULL,'TMS1233'),
('A164','2025-08-14','Absent','Lecture',3,'TG0014','MD004','TMS1233'),
('A165','2025-08-14','Absent','Lecture',3,'TG0015','MD002','TMS1233'),
('A166','2025-08-15','Present','Lecture',2,'TG0001',NULL,'ICT1242'),
('A167','2025-08-15','Present','Lecture',2,'TG0002',NULL,'ICT1242'),
('A168','2025-08-15','Present','Lecture',2,'TG0003',NULL,'ICT1242'),
('A169','2025-08-15','Present','Lecture',2,'TG0004',NULL,'ICT1242'),
('A170','2025-08-15','Present','Lecture',2,'TG0005',NULL,'ICT1242'),
('A171','2025-08-15','Present','Lecture',2,'TG0006',NULL,'ICT1242'),
('A172','2025-08-15','Present','Lecture',2,'TG0007',NULL,'ICT1242'),
('A173','2025-08-15','Present','Lecture',2,'TG0008',NULL,'ICT1242'),
('A174','2025-08-15','Present','Lecture',2,'TG0009',NULL,'ICT1242'),
('A175','2025-08-15','Present','Lecture',2,'TG0010',NULL,'ICT1242'),
('A176','2025-08-15','Present','Lecture',2,'TG0011',NULL,'ICT1242'),
('A177','2025-08-15','Present','Lecture',2,'TG0012',NULL,'ICT1242'),
('A178','2025-08-15','Present','Lecture',2,'TG0013',NULL,'ICT1242'),
('A179','2025-08-15','Absent','Lecture',2,'TG0014','MD004','ICT1242'),
('A180','2025-08-15','Absent','Lecture',2,'TG0015','MD002','ICT1242'),
('A181','2025-08-15','Present','Lecture',4,'TG0001',NULL,'ICT1253'),
('A182','2025-08-15','Present','Lecture',4,'TG0002',NULL,'ICT1253'),
('A183','2025-08-15','Present','Lecture',4,'TG0003',NULL,'ICT1253'),
('A184','2025-08-15','Present','Lecture',4,'TG0004',NULL,'ICT1253'),
('A185','2025-08-15','Present','Lecture',4,'TG0005',NULL,'ICT1253'),
('A186','2025-08-15','Present','Lecture',4,'TG0006',NULL,'ICT1253'),
('A187','2025-08-15','Present','Lecture',4,'TG0007',NULL,'ICT1253'),
('A188','2025-08-15','Present','Lecture',4,'TG0008',NULL,'ICT1253'),
('A189','2025-08-15','Present','Lecture',4,'TG0009',NULL,'ICT1253'),
('A190','2025-08-15','Present','Lecture',4,'TG0010',NULL,'ICT1253'),
('A191','2025-08-15','Present','Lecture',4,'TG0011',NULL,'ICT1253'),
('A192','2025-08-15','Present','Lecture',4,'TG0012',NULL,'ICT1253'),
('A193','2025-08-15','Absent','Lecture',4,'TG0013','MD005','ICT1253'),
('A194','2025-08-15','Absent','Lecture',4,'TG0014','MD004','ICT1253'),
('A195','2025-08-15','Absent','Lecture',4,'TG0015','MD002','ICT1253'),
('A1756','2025-11-15','Absent','Lecture',2,'TG0001','MD010','TCS1212'),
('A1763','2025-11-15','Absent','Lecture',2,'TG0008','MD009','TCS1212');

/* ── MARKS DATA (original — preserved exactly) ── */
INSERT INTO Mark VALUES
('M01',0,62,48,79,67,44,'TG0001','ICT1233'),
('M02',61,78,40,85,74,39,'TG0001','TMS1233'),
('M03',71,65,49,80,66,84,'TG0001','ICT1253'),
('M04',0,76,38,87,64,45,'TG0001','ICT1242'),
('M05',69,73,43,84,70,45,'TG0001','ICT1212'),
('M06',63,72,37,81,40,90,'TG0001','ICT1222'),
('M07',64,79,35,78,73,32,'TG0001','TCS1212'),
('M08',66,77,42,83,69,41,'TG0001','ENG1222'),
('M09',72,82,91,61,68,70,'TG0002','ICT1233'),
('M10',74,89,34,0,93,50,'TG0002','TMS1233'),
('M11',76,0,94,45,92,60,'TG0002','ICT1253'),
('M12',73,81,93,82,31,96,'TG0002','ICT1242'),
('M13',75,83,90,86,33,91,'TG0002','ICT1212'),
('M14',77,87,95,81,43,66,'TG0002','ICT1222'),
('M15',71,84,89,80,35,90,'TG0002','TCS1212'),
('M16',0,88,97,87,38,98,'TG0002','ENG1222'),
('M17',0,75,95,68,46,90,'TG0003','ICT1233'),
('M18',40,78,97,69,49,36,'TG0003','TMS1233'),
('M19',43,76,98,71,47,94,'TG0003','ICT1253'),
('M20',41,74,96,67,60,37,'TG0003','ICT1242'),
('M21',44,80,99,72,50,39,'TG0003','ICT1212'),
('M22',45,79,95,70,40,49,'TG0003','ICT1222'),
('M23',42,76,94,69,45,35,'TG0003','TCS1212'),
('M24',39,81,97,70,0,59,'TG0003','ENG1222'),
('M25',40,88,60,45,50,68,'TG0004','ICT1233'),
('M26',36,90,59,61,30,60,'TG0004','TMS1233'),
('M27',37,87,55,48,64,70,'TG0004','ICT1253'),
('M28',39,86,0,50,63,64,'TG0004','ICT1242'),
('M29',41,90,58,46,65,65,'TG0004','ICT1212'),
('M30',38,85,56,47,70,66,'TG0004','ICT1222'),
('M31',36,89,57,44,62,60,'TG0004','TCS1212'),
('M32',39,88,55,45,61,62,'TG0004','ENG1222'),
('M33',60,72,42,47,91,80,'TG0005','ICT1233'),
('M34',57,71,39,44,93,79,'TG0005','TMS1233'),
('M35',59,69,41,46,77,71,'TG0005','ICT1253'),
('M36',61,73,43,48,92,78,'TG0005','ICT1242'),
('M37',58,70,40,50,89,81,'TG0005','ICT1212'),
('M38',45,79,95,49,38,95,'TG0005','ICT1222'),
('M39',62,74,45,49,76,67,'TG0005','TCS1212'),
('M40',56,68,38,43,94,75,'TG0005','ENG1222'),
('M41',30,50,10,68,0,53,'TG0006','ICT1233'),
('M42',79,30,50,0,0,52,'TG0006','TMS1233'),
('M43',25,0,53,30,40,50,'TG0006','ICT1253'),
('M44',0,82,51,88,0,54,'TG0006','ICT1242'),
('M45',25,50,55,92,39,0,'TG0006','ICT1212'),
('M46',30,28,49,0,42,48,'TG0006','ICT1222'),
('M47',20,0,52,47,38,0,'TG0006','TCS1212'),
('M48',10,30,42,80,0,68,'TG0006','ENG1222'),
('M49',55,47,38,51,33,47,'TG0007','ICT1233'),
('M50',0,44,36,0,31,0,'TG0007','TMS1233'),
('M51',54,45,51,50,32,46,'TG0007','ICT1253'),
('M52',56,48,0,50,34,0,'TG0007','ICT1242'),
('M53',55,0,10,47,32,0,'TG0007','ICT1212'),
('M54',54,46,51,48,0,46,'TG0007','ICT1222'),
('M55',54,45,51,0,32,0,'TG0007','TCS1212'),
('M56',53,48,37,0,32,0,'TG0007','ENG1222'),
('M57',45,61,42,58,55,48,'TG0008','ICT1233'),
('M58',37,36,49,0,53,0,'TG0008','TMS1233'),
('M59',40,35,46,54,51,49,'TG0008','ICT1253'),
('M60',39,34,44,0,54,0,'TG0008','ICT1242'),
('M61',36,33,40,0,54,0,'TG0008','ICT1212'),
('M62',38,30,43,62,54,52,'TG0008','ICT1222'),
('M63',42,32,50,60,56,0,'TG0008','TCS1212'),
('M64',44,31,48,0,57,0,'TG0008','ENG1222'),
('M65',61,72,80,45,69,34,'TG0009','ICT1233'),
('M66',59,70,78,43,67,32,'TG0009','TMS1233'),
('M67',60,75,79,44,68,33,'TG0009','ICT1253'),
('M68',46,73,81,46,70,35,'TG0009','ICT1242'),
('M69',58,74,77,43,67,31,'TG0009','ICT1212'),
('M70',60,75,44,44,68,33,'TG0009','ICT1222'),
('M71',54,45,51,48,32,0,'TG0009','TCS1212'),
('M72',59,76,43,70,67,32,'TG0009','ENG1222'),
('M73',47,99,98,88,66,0,'TG0010','ICT1233'),
('M74',84,30,65,49,92,55,'TG0010','TMS1233'),
('M75',81,46,41,91,70,100,'TG0010','ICT1253'),
('M76',80,50,46,90,73,95,'TG0010','ICT1242'),
('M77',49,54,91,88,65,34,'TG0010','ICT1212'),
('M78',86,46,81,51,95,57,'TG0010','ICT1222'),
('M79',49,54,91,88,65,34,'TG0010','TCS1212'),
('M80',95,80,50,97,91,30,'TG0010','ENG1222'),
('M81',85,32,70,48,93,51,'TG0011','ICT1233'),
('M82',90,45,68,53,97,56,'TG0011','TMS1233'),
('M83',84,44,78,49,92,55,'TG0011','ICT1253'),
('M84',87,42,75,52,94,58,'TG0011','ICT1242'),
('M85',82,69,50,88,67,43,'TG0011','ICT1212'),
('M86',85,50,68,30,95,57,'TG0011','ICT1222'),
('M87',89,47,80,50,96,53,'TG0011','TCS1212'),
('M88',86,46,81,39,95,57,'TG0011','ENG1222'),
('M89',90,98,99,89,80,34,'TG0012','ICT1233'),
('M90',72,65,68,60,70,33,'TG0012','TMS1233'),
('M91',50,60,70,83,60,33,'TG0012','ICT1253'),
('M92',60,85,89,0,65,33,'TG0012','ICT1242'),
('M93',70,75,91,80,75,42,'TG0012','ICT1212'),
('M94',75,48,88,79,82,34,'TG0012','ICT1222'),
('M95',80,78,89,90,75,33,'TG0012','TCS1212'),
('M96',58,58,78,75,79,78,'TG0012','ENG1222'),
('M97',82,50,88,58,42,93,'TG0013','ICT1233'),
('M98',85,47,91,57,45,92,'TG0013','TMS1233'),
('M99',84,49,89,55,74,98,'TG0013','ICT1253'),
('M100',81,46,87,59,70,100,'TG0013','ICT1242'),
('M101',83,48,90,56,0,97,'TG0013','ICT1212'),
('M102',80,50,86,60,73,95,'TG0013','ICT1222'),
('M103',32,33,90,0,35,0,'TG0013','TCS1212'),
('M104',55,46,52,47,33,47,'TG0013','ENG1222'),
('M105',95,80,50,70,68,30,'TG0014','ICT1233'),
('M106',96,78,48,73,65,0,'TG0014','TMS1233'),
('M107',97,75,51,95,92,34,'TG0014','ICT1253'),
('M108',94,76,52,74,89,0,'TG0014','ICT1242'),
('M109',92,79,50,75,70,88,'TG0014','ICT1212'),
('M110',93,77,49,72,80,35,'TG0014','ICT1222'),
('M111',91,74,53,70,68,87,'TG0014','TCS1212'),
('M112',99,81,47,71,65,90,'TG0014','ENG1222'),
('M113',29,35,91,37,97,51,'TG0015','ICT1233'),
('M114',32,33,90,36,99,35,'TG0015','TMS1233'),
('M115',30,36,93,35,33,49,'TG0015','ICT1253'),
('M116',33,34,92,95,82,34,'TG0015','ICT1242'),
('M117',31,34,92,36,98,68,'TG0015','ICT1212'),
('M118',30,34,92,36,0,40,'TG0015','ICT1222'),
('M119',31,36,91,36,97,34,'TG0015','TCS1212'),
('M120',30,34,92,37,98,35,'TG0015','ENG1222');


