-- ============================================================
--  HRM SYSTEM — Complete Database Schema
--  HR Manager Portal | MySQL 5.7+
-- ============================================================

DROP DATABASE IF EXISTS hrm_system;
CREATE DATABASE hrm_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hrm_system;

-- DEPARTMENTS
CREATE TABLE departments (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(500),
    location      VARCHAR(200),
    budget        DECIMAL(15,2) DEFAULT 0.00,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- POSITIONS
CREATE TABLE positions (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(150) NOT NULL,
    department_id INT,
    min_salary    DECIMAL(12,2) DEFAULT 0,
    max_salary    DECIMAL(12,2) DEFAULT 0,
    description   VARCHAR(500),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- EMPLOYEES
CREATE TABLE employees (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(80) NOT NULL,
    last_name     VARCHAR(80) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(30),
    date_of_birth DATE,
    gender        ENUM('Male','Female') DEFAULT 'Male',
    national_id   VARCHAR(30),
    address       VARCHAR(300),
    city          VARCHAR(100),
    department_id INT,
    position_id   INT,
    manager_id    INT,
    hire_date     DATE NOT NULL,
    contract_type ENUM('CDI','CDD','Freelance','Intern') DEFAULT 'CDI',
    base_salary   DECIMAL(12,2) NOT NULL DEFAULT 0,
    status        ENUM('Active','On Leave','Suspended','Terminated') DEFAULT 'Active',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (position_id)   REFERENCES positions(id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id)    REFERENCES employees(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- USERS (HR Manager login)
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(160) NOT NULL,
    email         VARCHAR(150),
    role          VARCHAR(30) DEFAULT 'HR_MANAGER',
    employee_id   INT,
    is_active     TINYINT(1) DEFAULT 1,
    last_login    TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ATTENDANCE
CREATE TABLE attendance (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    employee_id   INT NOT NULL,
    date          DATE NOT NULL,
    check_in      TIME,
    check_out     TIME,
    status        ENUM('Present','Absent','Late','Half Day','On Leave') DEFAULT 'Present',
    notes         VARCHAR(300),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_att (employee_id, date),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- LEAVE TYPES
CREATE TABLE leave_types (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(80) NOT NULL UNIQUE,
    max_days      INT DEFAULT 0,
    is_paid       TINYINT(1) DEFAULT 1,
    color         VARCHAR(7) DEFAULT '#3B82F6'
) ENGINE=InnoDB;

-- LEAVE REQUESTS
CREATE TABLE leave_requests (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    employee_id   INT NOT NULL,
    leave_type_id INT NOT NULL,
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    total_days    INT NOT NULL,
    reason        VARCHAR(500),
    status        ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    reviewed_by   INT,
    review_date   TIMESTAMP NULL,
    review_notes  VARCHAR(300),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id)   REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by)   REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- PAYROLL
CREATE TABLE payroll (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT NOT NULL,
    period_month    INT NOT NULL,
    period_year     INT NOT NULL,
    base_salary     DECIMAL(12,2) NOT NULL,
    overtime_hours  DECIMAL(6,2) DEFAULT 0,
    overtime_pay    DECIMAL(12,2) DEFAULT 0,
    bonuses         DECIMAL(12,2) DEFAULT 0,
    deductions      DECIMAL(12,2) DEFAULT 0,
    tax_amount      DECIMAL(12,2) DEFAULT 0,
    cnss            DECIMAL(12,2) DEFAULT 0,
    net_salary      DECIMAL(12,2) NOT NULL,
    payment_status  ENUM('Pending','Paid','Cancelled') DEFAULT 'Pending',
    payment_date    DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_pay (employee_id, period_month, period_year),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- PERFORMANCE REVIEWS
CREATE TABLE performance_reviews (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT NOT NULL,
    review_date     DATE NOT NULL,
    period          VARCHAR(50),
    productivity    INT DEFAULT 0,
    quality         INT DEFAULT 0,
    communication   INT DEFAULT 0,
    teamwork        INT DEFAULT 0,
    initiative      INT DEFAULT 0,
    overall_rating  DECIMAL(3,1) DEFAULT 0,
    strengths       TEXT,
    improvements    TEXT,
    goals           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ANNOUNCEMENTS
CREATE TABLE announcements (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    content       TEXT NOT NULL,
    priority      ENUM('Low','Normal','Important','Urgent') DEFAULT 'Normal',
    is_active     TINYINT(1) DEFAULT 1,
    expires_at    DATE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;



-- ============================================================
--  SAMPLE DATA  —  uses CURDATE() so data is always recent
-- ============================================================

-- ── Departments ──
INSERT INTO departments (name, description, location, budget) VALUES
('Engineering',     'Software development & IT',         'Casablanca - Floor 3', 850000),
('Marketing',       'Brand strategy & digital marketing', 'Casablanca - Floor 2', 420000),
('Finance',         'Accounting & financial planning',    'Rabat - Floor 1',      380000),
('Human Resources', 'Talent acquisition & HR ops',        'Casablanca - Floor 1', 300000),
('Operations',      'Supply chain & logistics',           'Tangier - Main Hall',  520000),
('Sales',           'Client relations & BD',              'Casablanca - Floor 2', 600000),
('Legal',           'Contracts & compliance',             'Rabat - Floor 2',      290000),
('Product',         'Product strategy & UX',              'Casablanca - Floor 3', 470000);

-- ── Positions ──
INSERT INTO positions (title, department_id, min_salary, max_salary) VALUES
('Senior Software Engineer', 1, 18000, 30000),
('Junior Developer',         1,  8000, 14000),
('DevOps Engineer',          1, 15000, 25000),
('Marketing Manager',        2, 14000, 22000),
('Content Creator',          2,  7000, 12000),
('Financial Analyst',        3, 12000, 20000),
('Accountant',               3,  9000, 15000),
('HR Specialist',            4, 10000, 17000),
('Operations Manager',       5, 15000, 24000),
('Warehouse Supervisor',     5,  8000, 13000),
('Sales Executive',          6, 10000, 18000),
('Business Developer',       6, 13000, 22000),
('Legal Counsel',            7, 16000, 28000),
('Product Manager',          8, 16000, 26000),
('UX Designer',              8, 12000, 20000);

-- ── Employees (must be inserted BEFORE users due to FK) ──
INSERT INTO employees (first_name, last_name, email, phone, date_of_birth, gender, national_id, address, city, department_id, position_id, hire_date, contract_type, base_salary, status) VALUES
('Youssef',  'Benali',      'youssef.benali@company.ma',    '+212 661-123456','1990-03-15','Male',  'BE789012','45 Rue Mohammed V',          'Casablanca',1,1, '2020-01-15','CDI',     25000,'Active'),
('Amina',    'Chakir',       'amina.chakir@company.ma',      '+212 662-234567','1993-07-22','Female','CH456789','12 Avenue Hassan II',         'Casablanca',1,2, '2022-06-01','CDI',     11000,'Active'),
('Karim',    'El Fassi',     'karim.elfassi@company.ma',     '+212 663-345678','1988-11-08','Male',  'EF123456','78 Bd Zerktouni',             'Casablanca',1,3, '2021-03-10','CDI',     20000,'Active'),
('Fatima Z.','Alaoui',       'fatima.alaoui@company.ma',     '+212 664-456789','1991-05-30','Female','AL789012','23 Rue de la Liberté',        'Rabat',     2,4, '2019-09-01','CDI',     18000,'Active'),
('Omar',     'Idrissi',      'omar.idrissi@company.ma',      '+212 665-567890','1995-01-14','Male',  'ID345678','56 Ave Mohammed VI',          'Casablanca',2,5, '2023-02-15','CDD',      9000,'Active'),
('Leila',    'Mansouri',     'leila.mansouri@company.ma',    '+212 666-678901','1987-09-25','Female','MA567890','34 Rue Ibn Sina',             'Rabat',     3,6, '2018-04-20','CDI',     17000,'Active'),
('Hassan',   'Tazi',         'hassan.tazi@company.ma',       '+212 667-789012','1992-12-03','Male',  'TA890123','67 Avenue FAR',               'Rabat',     3,7, '2021-08-01','CDI',     12000,'Active'),
('Nadia',    'Berrada',      'nadia.berrada@company.ma',     '+212 668-890123','1994-04-18','Female','BR234567','89 Rue Allal Ben Abdellah',   'Casablanca',4,8, '2020-11-01','CDI',     14000,'Active'),
('Mehdi',    'Squalli',      'mehdi.squalli@company.ma',     '+212 669-901234','1996-08-07','Male',  'SQ678901','15 Boulevard Anfa',           'Casablanca',5,9, '2019-06-15','CDI',     21000,'Active'),
('Salma',    'El Ouazzani',  'salma.elouazzani@company.ma',  '+212 670-012345','1993-02-28','Female','EO012345','42 Rue de Fès',               'Tangier',   5,10,'2022-01-10','CDD',     10000,'Active'),
('Rachid',   'Bennani',      'rachid.bennani@company.ma',    '+212 671-123456','1989-06-11','Male',  'BN345678','31 Ave Nations Unies',        'Casablanca',6,11,'2020-05-01','CDI',     15000,'Active'),
('Hajar',    'Chraibi',      'hajar.chraibi@company.ma',     '+212 672-234567','1994-10-20','Female','CR678901','55 Rue Oued Ziz',             'Casablanca',6,12,'2021-09-15','CDI',     18000,'Active'),
('Anas',     'Kettani',      'anas.kettani@company.ma',      '+212 673-345678','1986-03-09','Male',  'KT901234','73 Bd Roudani',               'Rabat',     7,13,'2017-02-01','CDI',     24000,'Active'),
('Imane',    'Filali',       'imane.filali@company.ma',      '+212 674-456789','1992-07-16','Female','FI234567','18 Rue Sebou',                'Casablanca',8,14,'2020-10-01','CDI',     22000,'Active'),
('Zakaria',  'El Moudir',    'zakaria.elmoudir@company.ma',  '+212 675-567890','1997-11-02','Male',  'EM567890','27 Ave Mers Sultan',          'Casablanca',8,15,'2023-04-01','Intern',   6000,'Active');

UPDATE employees SET manager_id=1  WHERE id IN (2,3);
UPDATE employees SET manager_id=4  WHERE id=5;
UPDATE employees SET manager_id=6  WHERE id=7;
UPDATE employees SET manager_id=9  WHERE id=10;
UPDATE employees SET manager_id=11 WHERE id=12;
UPDATE employees SET manager_id=14 WHERE id=15;

-- ── Users (after employees so FK is valid) ──
INSERT INTO users (username, password, full_name, email, role, employee_id) VALUES
('admin',   'admin123', 'Sara El Amrani',  'sara.elamrani@company.ma',  'HR_MANAGER', NULL),
('youssef', 'emp123',   'Youssef Benali',  'youssef.benali@company.ma', 'EMPLOYEE',   1),
('amina',   'emp123',   'Amina Chakir',    'amina.chakir@company.ma',   'EMPLOYEE',   2);

-- ── Leave Types ──
INSERT INTO leave_types (name, max_days, is_paid, color) VALUES
('Annual Leave',     18,1,'#3B82F6'),
('Sick Leave',       12,1,'#EF4444'),
('Maternity Leave',  98,1,'#EC4899'),
('Paternity Leave',   3,1,'#8B5CF6'),
('Unpaid Leave',     30,0,'#6B7280'),
('Marriage Leave',    4,1,'#F59E0B'),
('Bereavement Leave', 3,1,'#1F2937');

-- ── Leave Requests (relative dates) ──
INSERT INTO leave_requests (employee_id, leave_type_id, start_date, end_date, total_days, reason, status, reviewed_by, review_date) VALUES
-- Youssef (id=1) — variety of statuses
(1, 1, DATE_ADD(CURDATE(), INTERVAL 14 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 5, 'Family vacation in Marrakech', 'Pending', NULL, NULL),
(1, 2, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 23 DAY), 3, 'Flu recovery', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 26 DAY)),
(1, 1, DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 87 DAY), 4, 'Winter break', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 95 DAY)),
(1, 5, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 59 DAY), 2, 'Personal matters', 'Rejected', 1, DATE_SUB(CURDATE(), INTERVAL 62 DAY)),
-- Amina (id=2) — pending + approved
(2, 1, DATE_ADD(CURDATE(), INTERVAL 21 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 5, 'Family vacation', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(2, 2, DATE_ADD(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 6 DAY),  2, 'Medical appointment', 'Pending', NULL, NULL),
(2, 6, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 117 DAY), 4, 'Wedding celebration', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 130 DAY)),
-- Other employees
(5,  1, DATE_ADD(CURDATE(), INTERVAL 12 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 4, 'Personal trip', 'Pending', NULL, NULL),
(7,  2, DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), 3, 'Medical recovery', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 29 DAY)),
(10, 1, DATE_ADD(CURDATE(), INTERVAL 54 DAY), DATE_ADD(CURDATE(), INTERVAL 63 DAY), 10, 'Summer vacation', 'Pending', NULL, NULL),
(8,  2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(CURDATE(), INTERVAL 2 DAY),  2, 'Dental surgery', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(12, 6, DATE_ADD(CURDATE(), INTERVAL 95 DAY), DATE_ADD(CURDATE(), INTERVAL 98 DAY), 4, 'Wedding', 'Approved', 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY));

-- ── Attendance (last 25 working days for Youssef and Amina) ──
INSERT INTO attendance (employee_id, date, check_in, check_out, status) VALUES
-- Youssef (id=1) — recent days
(1, CURDATE(),                                 '08:55','18:05','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY),       '09:00','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 2 DAY),       '08:50','18:10','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY),       '09:15','18:00','Late'),
(1, DATE_SUB(CURDATE(), INTERVAL 4 DAY),       '08:45','18:05','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 7 DAY),       '08:55','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 8 DAY),       '08:50','17:55','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 9 DAY),       '09:05','18:10','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 10 DAY),      '08:40','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 11 DAY),      '09:20','18:00','Late'),
(1, DATE_SUB(CURDATE(), INTERVAL 14 DAY),      '08:55','18:05','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 15 DAY),      '08:50','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 16 DAY),      '08:45','18:10','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 17 DAY),      '08:55','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 18 DAY),      '09:00','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 21 DAY),      '08:50','18:05','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 22 DAY),      '08:45','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 23 DAY),       NULL,    NULL,'Absent'),
(1, DATE_SUB(CURDATE(), INTERVAL 24 DAY),       NULL,    NULL,'On Leave'),
(1, DATE_SUB(CURDATE(), INTERVAL 25 DAY),       NULL,    NULL,'On Leave'),
(1, DATE_SUB(CURDATE(), INTERVAL 28 DAY),      '08:55','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 29 DAY),      '08:50','18:05','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 30 DAY),      '09:00','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 35 DAY),      '08:45','18:00','Present'),
(1, DATE_SUB(CURDATE(), INTERVAL 36 DAY),      '08:50','18:00','Present'),

-- Amina (id=2)
(2, CURDATE(),                                 '09:02','17:58','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY),       '08:58','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY),       '09:00','17:55','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),       '09:25','18:00','Late'),
(2, DATE_SUB(CURDATE(), INTERVAL 4 DAY),       '08:55','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 7 DAY),       '08:50','17:55','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 8 DAY),       '09:05','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 9 DAY),       '09:00','18:05','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 10 DAY),      '08:55','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 11 DAY),      '09:00','17:55','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 14 DAY),       NULL,    NULL,'Absent'),
(2, DATE_SUB(CURDATE(), INTERVAL 15 DAY),      '08:50','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 16 DAY),      '08:55','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 17 DAY),      '09:10','18:00','Late'),
(2, DATE_SUB(CURDATE(), INTERVAL 18 DAY),      '09:00','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 21 DAY),      '08:55','17:55','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 22 DAY),      '09:00','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 23 DAY),      '08:50','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 24 DAY),      '08:55','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 25 DAY),      '08:45','17:55','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 28 DAY),      '09:00','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 29 DAY),      '08:55','18:00','Present'),
(2, DATE_SUB(CURDATE(), INTERVAL 30 DAY),      '09:00','17:55','Present'),

-- Other employees (today only, for HR dashboard)
(3, CURDATE(),                                 '08:45','18:10','Present'),
(4, CURDATE(),                                 '09:15','18:00','Late'),
(5, CURDATE(),                                 '08:50','17:55','Present'),
(6, CURDATE(),                                 '08:30','18:00','Present'),
(7, CURDATE(),                                  NULL,    NULL,'Absent'),
(8, CURDATE(),                                  NULL,    NULL,'On Leave'),
(9, CURDATE(),                                 '08:58','18:02','Present'),
(10, CURDATE(),                                '09:10','18:00','Late'),
(11, CURDATE(),                                '08:40','18:05','Present'),
(12, CURDATE(),                                '08:55','17:50','Present'),
(13, CURDATE(),                                '09:00','18:00','Present'),
(14, CURDATE(),                                '08:50','18:15','Present'),
(15, CURDATE(),                                '09:05','17:45','Present');

-- ── Payroll (last 3 months for Youssef and Amina, current month for all) ──
INSERT INTO payroll (employee_id, period_month, period_year, base_salary, overtime_hours, overtime_pay, bonuses, deductions, tax_amount, cnss, net_salary, payment_status, payment_date) VALUES
-- Youssef — 3 months history
(1, MONTH(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), 25000, 6, 1100, 1500, 0, 5520, 1120, 21030, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 3 MONTH))),
(1, MONTH(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), 25000, 4, 750,  0,    0, 5150, 1120, 19480, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 2 MONTH))),
(1, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 25000, 8, 1500, 2000, 0, 5700, 1123, 21677, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
-- Amina — 3 months history
(2, MONTH(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), 11000, 0, 0,    500,  0, 2300, 494, 8706, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 3 MONTH))),
(2, MONTH(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), 11000, 2, 230,  0,    0, 2246, 494, 8490, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 2 MONTH))),
(2, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 11000, 0, 0,    1000, 0, 2400, 494, 9106, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
-- Other employees, last month
(3, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 20000, 5, 950, 0, 0, 4190, 940, 15820, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(4, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 18000, 0, 0, 1500, 0, 3900, 877, 14723, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(5, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)),  9000, 0, 0, 0, 0, 1800, 404, 6796, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(6, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 17000, 3, 575, 0, 500, 3415, 788, 12872, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(7, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 12000, 0, 0, 0, 0, 2400, 539, 9061, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(8, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 14000, 0, 0, 500, 0, 2900, 651, 10949, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(9, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 21000, 10, 1875, 1000, 0, 4775, 1073, 18027, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(10, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 10000, 0, 0, 0, 0, 2000, 449, 7551, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(11, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 15000, 6, 1125, 0, 0, 3225, 724, 12176, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(12, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 18000, 0, 0, 2000, 0, 4000, 899, 15101, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(13, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 24000, 0, 0, 0, 0, 4800, 1078, 18122, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(14, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 22000, 4, 800, 1500, 0, 4860, 1092, 18348, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))),
(15, MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)),  6000, 0, 0, 0, 0, 1200, 269, 4531, 'Paid', LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)));

-- ── Performance Reviews ──
INSERT INTO performance_reviews (employee_id, review_date, period, productivity, quality, communication, teamwork, initiative, overall_rating, strengths, improvements, goals) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 5, 5, 4, 4, 5, 4.6, 'Excellent technical leadership, mentors juniors, ships on time', 'Improve documentation habits', 'Lead architecture redesign next quarter'),
(2, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 4, 3, 4, 5, 3, 3.8, 'Great team player, eager to learn, positive attitude', 'Code quality and unit test coverage', 'Complete React advanced certification'),
(4, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 5, 4, 5, 4, 5, 4.6, 'Strong campaign results, creative strategies', 'Budget tracking precision', 'Launch 3 major campaigns this quarter'),
(6, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 4, 5, 3, 4, 4, 4.0, 'Highly accurate work, attention to detail', 'Presentation skills', 'Prepare quarterly board presentations'),
(9, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 5, 4, 4, 5, 4, 4.4, 'Process improvements saved 15% on logistics', 'Delegation skills', 'Implement new warehouse system'),
(11, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 4, 4, 5, 4, 3, 4.0, 'Strong client relationships, consistent revenue', 'Explore new market segments', 'Increase portfolio by 20%'),
(14, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Last Quarter', 5, 5, 4, 4, 5, 4.6, 'Excellent product vision, data-driven decisions', 'Stakeholder communication timing', 'Launch v2.0 by end of quarter');

-- ── Announcements ──
INSERT INTO announcements (title, content, priority, expires_at) VALUES
('Welcome to the new HRM Portal!', 'Our new HR system is now live. Use it to view your attendance, payslips, and request leave online. Contact HR for any questions.', 'Important', DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
('Eid Al-Adha Holiday Schedule', 'The office will be closed for the Eid Al-Adha holiday. Please plan your deliverables accordingly and ensure handover before the break.', 'Important', DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
('Quarterly Performance Reviews Begin', 'The performance review cycle starts next month. Please prepare your self-assessment and discuss goals with your manager.', 'Normal', DATE_ADD(CURDATE(), INTERVAL 25 DAY)),
('Health Insurance Provider Update', 'We are upgrading our health insurance provider. Detailed information about the new coverage will be sent via email shortly.', 'Important', DATE_ADD(CURDATE(), INTERVAL 60 DAY)),
('Team Building Day — Bouznika Bay', 'Join us for our annual team-building event! RSVP through HR before next Friday. Transportation and lunch provided.', 'Normal', DATE_ADD(CURDATE(), INTERVAL 14 DAY)),
('System Maintenance Notice', 'Scheduled IT maintenance from 10 PM to 2 AM tomorrow. Internal systems may be temporarily unavailable.', 'Urgent', DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
('Office Coffee Machine Upgraded!', 'The break room has a new espresso machine. Enjoy fresh coffee throughout the day. Please clean up after use.', 'Low', DATE_ADD(CURDATE(), INTERVAL 90 DAY));
