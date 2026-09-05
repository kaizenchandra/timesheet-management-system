-- Deterministic development data. This migration is applied once by Flyway.

INSERT INTO employee (
    id, version, first_name, last_name, address_line1, address_line2,
    city, state, zip_code, country, contact_number, email_address
) VALUES
    ('00000000-0000-0000-0000-000000000001', 0, 'Alice', 'Manager',
     '1 Main Street', 'Suite 100', 'New York', 'NY', '10001', 'USA', '555-0101', 'alice.manager@example.com'),
    ('00000000-0000-0000-0000-000000000002', 0, 'Bob', 'Developer',
     '2 Main Street', 'Apt 10', 'New York', 'NY', '10002', 'USA', '555-0102', 'bob.developer@example.com'),
    ('00000000-0000-0000-0000-000000000003', 0, 'Carol', 'Analyst',
     '3 Main Street', 'Apt 20', 'New York', 'NY', '10003', 'USA', '555-0103', 'carol.analyst@example.com');

INSERT INTO employee_manager (employee_id, manager_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001');

INSERT INTO project (id, version, name, description, active) VALUES
    ('10000000-0000-0000-0000-000000000001', 0, 'Timesheet Platform',
     'Core timesheet management implementation', TRUE),
    ('10000000-0000-0000-0000-000000000002', 0, 'Reporting Portal',
     'Reporting and work calendar improvements', TRUE);

INSERT INTO project_employees (projects_id, employees_id) VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003');

INSERT INTO task (id, version, name, description, active, project_id) OVERRIDING SYSTEM VALUE VALUES
    (1001, 0, 'Implement API', 'Implement the timesheet REST API', TRUE,
     '10000000-0000-0000-0000-000000000001'),
    (1002, 0, 'Review database', 'Review database schema and migrations', TRUE,
     '10000000-0000-0000-0000-000000000001'),
    (1003, 0, 'Build reports', 'Build employee hours reports', TRUE,
     '10000000-0000-0000-0000-000000000002');

SELECT setval(
    pg_get_serial_sequence('task', 'id'),
    GREATEST((SELECT MAX(id) FROM task), 1),
    TRUE
);

INSERT INTO task_employees (tasks_id, employees_id) VALUES
    (1001, '00000000-0000-0000-0000-000000000002'),
    (1002, '00000000-0000-0000-0000-000000000001'),
    (1003, '00000000-0000-0000-0000-000000000003');

INSERT INTO "leave" (
    id, version, start_date, end_date, type, status, reason, hours, comments, employee_id
) VALUES
    ('20000000-0000-0000-0000-000000000001', 0, DATE '2026-09-14', DATE '2026-09-15',
     'VACATION', 'APPROVED', 'Annual vacation', 16, 'Approved for planned vacation', 
     '00000000-0000-0000-0000-000000000002'),
    ('20000000-0000-0000-0000-000000000002', 0, DATE '2026-09-21', DATE '2026-09-21',
     'PERSONAL_LEAVE', 'PENDING', 'Personal appointment', 8, '',
     '00000000-0000-0000-0000-000000000003');

INSERT INTO leave_approval (
    status, comments, decision_date, approver_id, leave_id
) VALUES
    ('APPROVED', 'Approved by manager', DATE '2026-09-05',
     '00000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000001');

INSERT INTO timesheet (
    id, version, status, start_date, end_date, employee_id
) VALUES
    ('30000000-0000-0000-0000-000000000001', 0, 'APPROVED',
     DATE '2026-09-07', DATE '2026-09-13',
     '00000000-0000-0000-0000-000000000002'),
    ('30000000-0000-0000-0000-000000000002', 0, 'SUBMITTED',
     DATE '2026-09-07', DATE '2026-09-13',
     '00000000-0000-0000-0000-000000000003');

INSERT INTO timesheet_entry (
    id, date, entry_type, hours, disable, task_id, timesheet_id
) VALUES
    ('40000000-0000-0000-0000-000000000001', DATE '2026-09-07', 'BILLABLE', 8, FALSE,
     1001, '30000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000002', DATE '2026-09-08', 'BILLABLE', 8, FALSE,
     1001, '30000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000003', DATE '2026-09-09', 'NON_BILLABLE', 6, FALSE,
     1002, '30000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000004', DATE '2026-09-07', 'BILLABLE', 8, FALSE,
     1003, '30000000-0000-0000-0000-000000000002'),
    ('40000000-0000-0000-0000-000000000005', DATE '2026-09-08', 'OVERTIME', 2, FALSE,
     1003, '30000000-0000-0000-0000-000000000002');

INSERT INTO timesheet_approval (
    status, comments, approval_date, approver_id, timesheet_id
) VALUES
    ('APPROVED', 'Timesheet reviewed and approved', DATE '2026-09-14',
     '00000000-0000-0000-0000-000000000001',
     '30000000-0000-0000-0000-000000000001');

INSERT INTO holiday (version, name, description, date) VALUES
    (0, 'Founders Day', 'Company holiday', DATE '2026-09-18'),
    (0, 'Year End Holiday', 'Office closed for year end', DATE '2026-12-25');
