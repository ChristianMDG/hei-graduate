-- =============================================================================
-- V42_99__cdc_full_seed.sql  (ou V42_100 si 99 déjà joué)
-- Seed CDC MAXIMAL + compte heichristian@gmail.com (test email relevé)
-- Mot de passe tous comptes : Password123!
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- 1. USERS
-- ---------------------------------------------------------------------------
INSERT INTO users (id, email, password, role, active, created_at) VALUES
  ('a1000000-0000-4000-8000-000000000001', 'demo.admin@hei.mg',     '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'ADMIN',   true, now()),
  ('a1000000-0000-4000-8000-000000000011', 'demo.teacher1@hei.mg',  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'TEACHER', true, now()),
  ('a1000000-0000-4000-8000-000000000012', 'demo.teacher2@hei.mg',  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'TEACHER', true, now()),
  ('a1000000-0000-4000-8000-000000000013', 'demo.teacher3@hei.mg',  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'TEACHER', true, now()),
  ('a1000000-0000-4000-8000-000000000014', 'demo.teacher4@hei.mg',  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'TEACHER', true, now()),
  ('a1000000-0000-4000-8000-000000000015', 'demo.teacher5@hei.mg',  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'TEACHER', true, now()),
  -- Compte personnel pour test email
  ('1d705863-da7b-4376-b572-a81f0aa6032e', 'heichristian@gmail.com', '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG', 'STUDENT', true, now())
ON CONFLICT (email) DO UPDATE
  SET password = EXCLUDED.password,
      active   = true;

INSERT INTO users (id, email, password, role, active, created_at)
SELECT
  ('44000000-1000-4000-8000-0000000000' || lpad(n::text, 2, '0'))::uuid,
  'demo.std240' || lpad(n::text, 2, '0') || '@hei.mg',
  '$2a$10$Wef15kf3UiPlNrh5DGFaXObVpjml2F9djiKE8c0TcLYtPs0DVhSfG',
  'STUDENT', true, now()
FROM generate_series(1, 20) AS n
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2. STRUCTURE
-- ---------------------------------------------------------------------------
INSERT INTO academic_year (id, label, start_date, end_date, level) VALUES
  ('b1000000-0000-4000-8000-000000000001', '2023-2024', '2023-09-01', '2024-06-30', 'L1'),
  ('b1000000-0000-4000-8000-000000000002', '2024-2025', '2024-09-01', '2025-06-30', 'L2'),
  ('b1000000-0000-4000-8000-000000000003', '2025-2026', '2025-09-01', '2026-06-30', 'L3')
ON CONFLICT (id) DO NOTHING;

INSERT INTO semester (id, label, start_date, end_date, expected_credits, active, academic_year_id) VALUES
  ('c1000000-0000-4000-8000-000000000001', 'S1-L1', '2023-09-01', '2024-02-29', 30, true, 'b1000000-0000-4000-8000-000000000001'),
  ('c1000000-0000-4000-8000-000000000011', 'S2-L1', '2024-03-01', '2024-06-30', 30, true, 'b1000000-0000-4000-8000-000000000001'),
  ('c1000000-0000-4000-8000-000000000002', 'S1-L2', '2024-09-01', '2025-02-28', 30, true, 'b1000000-0000-4000-8000-000000000002'),
  ('c1000000-0000-4000-8000-000000000012', 'S2-L2', '2025-03-01', '2025-06-30', 30, true, 'b1000000-0000-4000-8000-000000000002'),
  ('c1000000-0000-4000-8000-000000000003', 'S1-L3', '2025-09-01', '2026-02-28', 30, true, 'b1000000-0000-4000-8000-000000000003'),
  ('c1000000-0000-4000-8000-000000000013', 'S2-L3', '2026-03-01', '2026-06-30', 30, true, 'b1000000-0000-4000-8000-000000000003')
ON CONFLICT (id) DO NOTHING;

INSERT INTO parcours (id, code, label, active) VALUES
  ('d1000000-0000-4000-8000-000000000001', 'EL', 'Ecosysteme Logiciel', true),
  ('d1000000-0000-4000-8000-000000000002', 'TN', 'Transformation Numerique', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO groups (id, reference, capacity, active) VALUES
  ('e1000000-0000-4000-8000-000000000001', 'K1', 40, true),
  ('e1000000-0000-4000-8000-000000000002', 'K2', 40, true),
  ('e1000000-0000-4000-8000-000000000003', 'K3', 40, true)
ON CONFLICT (reference) DO NOTHING;

INSERT INTO promotion (id, label, final_academic_year_id) VALUES
  ('f1000000-0000-4000-8000-000000000001', 'Promotion 2026', 'b1000000-0000-4000-8000-000000000003')
ON CONFLICT (final_academic_year_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3. COURS
-- ---------------------------------------------------------------------------
INSERT INTO course (id, course_reference, title, credits, active) VALUES
  ('11000000-1000-4000-8000-000000000001', 'METIER1', 'Metier TN', 5, true),
  ('11000000-1000-4000-8000-000000000002', 'PROG4',   'Programmation avancee', 5, true),
  ('11000000-1000-4000-8000-000000000003', 'WEB1',    'Developpement Web', 3, true),
  ('11000000-1000-4000-8000-000000000004', 'ARCHI1',  'Architecture logicielle', 5, true),
  ('11000000-1000-4000-8000-000000000005', 'BDD1',    'Bases de donnees', 4, true),
  ('11000000-1000-4000-8000-000000000006', 'SYS1',    'Systemes d exploitation', 4, true),
  ('11000000-1000-4000-8000-000000000007', 'MATH1',   'Mathematiques pour l informatique', 3, true),
  ('11000000-1000-4000-8000-000000000008', 'DIGI1',   'Transformation digitale', 4, true)
ON CONFLICT (course_reference) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 4. COURSE TRACKS
-- ---------------------------------------------------------------------------
INSERT INTO course_track (id, course_id, track_id, semester_id, mandatory)
SELECT gen_random_uuid(), c.id, p.id, s.id, true
FROM (VALUES
  ('PROG4',  'EL', 'S1-L1'), ('MATH1',  'EL', 'S1-L1'), ('SYS1',   'EL', 'S2-L1'),
  ('PROG4',  'EL', 'S1-L2'), ('WEB1',   'EL', 'S1-L2'), ('BDD1',   'EL', 'S2-L2'),
  ('ARCHI1', 'EL', 'S1-L3'), ('BDD1',   'EL', 'S1-L3'), ('SYS1',   'EL', 'S2-L3'),
  ('METIER1','TN', 'S1-L1'), ('MATH1',  'TN', 'S1-L1'), ('DIGI1',  'TN', 'S2-L1'),
  ('METIER1','TN', 'S1-L2'), ('WEB1',   'TN', 'S1-L2'), ('DIGI1',  'TN', 'S2-L2'),
  ('BDD1',   'TN', 'S1-L3'), ('DIGI1',  'TN', 'S1-L3'), ('ARCHI1', 'TN', 'S2-L3')
) AS v(cref, pcode, slabel)
JOIN course c ON c.course_reference = v.cref
JOIN parcours p ON p.code = v.pcode
JOIN semester s ON s.label = v.slabel
WHERE NOT EXISTS (
  SELECT 1 FROM course_track ct
  WHERE ct.course_id = c.id AND ct.track_id = p.id AND ct.semester_id = s.id
);

-- ---------------------------------------------------------------------------
-- 5. TEACHERS
-- ---------------------------------------------------------------------------
INSERT INTO teacher (id, user_id, last_name, first_name, specialty, contract_type)
SELECT '33000000-1000-4000-8000-000000000001', u.id, 'Randria', 'Paul',  'Programmation', 'CDI' FROM users u WHERE u.email = 'demo.teacher1@hei.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO teacher (id, user_id, last_name, first_name, specialty, contract_type)
SELECT '33000000-1000-4000-8000-000000000002', u.id, 'Rasoa',   'Hery',  'Web',            'CDI' FROM users u WHERE u.email = 'demo.teacher2@hei.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO teacher (id, user_id, last_name, first_name, specialty, contract_type)
SELECT '33000000-1000-4000-8000-000000000003', u.id, 'Andria',  'Mialy', 'Architecture',   'CDD' FROM users u WHERE u.email = 'demo.teacher3@hei.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO teacher (id, user_id, last_name, first_name, specialty, contract_type)
SELECT '33000000-1000-4000-8000-000000000004', u.id, 'Rakoto',  'Nivo',  'Base de donnees', 'CDI' FROM users u WHERE u.email = 'demo.teacher4@hei.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO teacher (id, user_id, last_name, first_name, specialty, contract_type)
SELECT '33000000-1000-4000-8000-000000000005', u.id, 'Rabe',    'Tovo',  'Metier TN',       'CDI' FROM users u WHERE u.email = 'demo.teacher5@hei.mg'
ON CONFLICT (user_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 6. STUDENTS STD24001..20 + STD24189 (heichristian@gmail.com)
-- ---------------------------------------------------------------------------
INSERT INTO student (id, user_id, student_number, last_name, first_name, birth_date, enrollment_date, status)
SELECT
  ('55000000-1000-4000-8000-0000000000' || lpad(n::text, 2, '0'))::uuid,
  u.id,
  'STD240' || lpad(n::text, 2, '0'),
  (ARRAY[
    'Rakoto','Rabe','Andria','Rasoa','Ranaivo','Soa','Fidy','Mamy','Tiana','Nomena',
    'Tojo','Voahirana','Hasina','Miora','Fara','Eric','Prisca','Kevin','Sandra','Bruno'
  ])[n],
  (ARRAY[
    'Jean','Marie','Paul','Hery','Lala','Soa','Fidy','Mamy','Tiana','Nomena',
    'Tojo','Voahirana','Hasina','Miora','Fara','Eric','Prisca','Kevin','Sandra','Bruno'
  ])[n],
  DATE '2003-01-01' + ((n - 1) || ' days')::interval,
  DATE '2023-09-01',
  'ACTIVE'
FROM generate_series(1, 20) AS n
JOIN users u ON u.email = 'demo.std240' || lpad(n::text, 2, '0') || '@hei.mg'
ON CONFLICT (student_number) DO NOTHING;

INSERT INTO student (id, user_id, student_number, last_name, first_name, birth_date, enrollment_date, status)
SELECT
  'aa24a89d-ee73-4ff4-90de-ddb80a1369e8',
  u.id,
  'STD24189',
  'Christian',
  'Jean',
  DATE '2003-05-15',
  DATE '2023-09-01',
  'ACTIVE'
FROM users u
WHERE u.email = 'heichristian@gmail.com'
ON CONFLICT (student_number) DO UPDATE
  SET user_id = EXCLUDED.user_id,
      last_name = EXCLUDED.last_name,
      first_name = EXCLUDED.first_name,
      status = 'ACTIVE';

-- ---------------------------------------------------------------------------
-- 7. ASSIGNMENTS
-- ---------------------------------------------------------------------------
INSERT INTO assignment (id, course_id, teacher_id, academic_year_id, semester_id)
SELECT v.aid::uuid, c.id, t.id, ay.id, s.id
FROM (VALUES
  ('aa100000-0000-4000-8000-000000000001', 'PROG4',   'demo.teacher1@hei.mg', '2024-2025', 'S1-L2'),
  ('aa100000-0000-4000-8000-000000000002', 'WEB1',    'demo.teacher2@hei.mg', '2024-2025', 'S1-L2'),
  ('aa100000-0000-4000-8000-000000000003', 'ARCHI1',  'demo.teacher3@hei.mg', '2025-2026', 'S1-L3'),
  ('aa100000-0000-4000-8000-000000000004', 'BDD1',    'demo.teacher4@hei.mg', '2025-2026', 'S1-L3'),
  ('aa100000-0000-4000-8000-000000000005', 'METIER1', 'demo.teacher5@hei.mg', '2023-2024', 'S1-L1'),
  ('aa100000-0000-4000-8000-000000000006', 'PROG4',   'demo.teacher1@hei.mg', '2023-2024', 'S1-L1')
) AS v(aid, cref, temail, ylabel, slabel)
JOIN course c ON c.course_reference = v.cref
JOIN users u ON u.email = v.temail
JOIN teacher t ON t.user_id = u.id
JOIN academic_year ay ON ay.label = v.ylabel
JOIN semester s ON s.label = v.slabel
WHERE NOT EXISTS (SELECT 1 FROM assignment a WHERE a.id = v.aid::uuid);

INSERT INTO assignment_group (assignment_id, group_id)
SELECT a.id, g.id
FROM assignment a
CROSS JOIN groups g
WHERE a.id IN (
  'aa100000-0000-4000-8000-000000000001'::uuid,
  'aa100000-0000-4000-8000-000000000002'::uuid,
  'aa100000-0000-4000-8000-000000000003'::uuid,
  'aa100000-0000-4000-8000-000000000004'::uuid,
  'aa100000-0000-4000-8000-000000000006'::uuid
)
AND g.reference IN ('K1', 'K2')
AND NOT EXISTS (
  SELECT 1 FROM assignment_group ag
  WHERE ag.assignment_id = a.id AND ag.group_id = g.id
);

INSERT INTO assignment_group (assignment_id, group_id)
SELECT a.id, g.id
FROM assignment a
JOIN groups g ON g.reference = 'K3'
WHERE a.id = 'aa100000-0000-4000-8000-000000000005'::uuid
AND NOT EXISTS (
  SELECT 1 FROM assignment_group ag
  WHERE ag.assignment_id = a.id AND ag.group_id = g.id
);

-- ---------------------------------------------------------------------------
-- 8. EXAMS
-- ---------------------------------------------------------------------------
INSERT INTO exam (id, course_id, academic_year_id, semester_id, label, date, start_time, end_time, coefficient_numerator, coefficient_denominator)
SELECT v.eid::uuid, c.id, ay.id, s.id, v.label, v.dt::date, v.st::time, v.et::time, v.num, v.den
FROM (VALUES
  ('22000000-1000-4000-8000-000000000001', 'METIER1', '2023-2024', 'S1-L1', 'Exam METIER1',     '2024-01-15', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000006', 'PROG4',   '2023-2024', 'S1-L1', 'Exam PROG4-L1',    '2024-01-12', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000007', 'MATH1',   '2023-2024', 'S1-L1', 'Exam MATH1',       '2024-01-18', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000008', 'SYS1',    '2023-2024', 'S2-L1', 'Exam SYS1-L1',     '2024-05-10', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000002', 'PROG4',   '2024-2025', 'S1-L2', 'Exam PROG4-CC',    '2024-11-15', '08:00', '10:00', 1, 2),
  ('22000000-1000-4000-8000-000000000012', 'PROG4',   '2024-2025', 'S1-L2', 'Exam PROG4-Final', '2025-01-15', '08:00', '10:00', 1, 2),
  ('22000000-1000-4000-8000-000000000003', 'WEB1',    '2024-2025', 'S1-L2', 'Exam WEB1',        '2025-01-20', '14:00', '16:00', 1, 1),
  ('22000000-1000-4000-8000-000000000009', 'BDD1',    '2024-2025', 'S2-L2', 'Exam BDD1-L2',     '2025-05-12', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000004', 'ARCHI1',  '2025-2026', 'S1-L3', 'Exam ARCHI1',      '2026-01-15', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000005', 'BDD1',    '2025-2026', 'S1-L3', 'Exam BDD1',        '2026-01-22', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000010', 'SYS1',    '2025-2026', 'S2-L3', 'Exam SYS1-L3',     '2026-05-10', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000011', 'DIGI1',   '2023-2024', 'S2-L1', 'Exam DIGI1-L1',    '2024-05-15', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000013', 'DIGI1',   '2024-2025', 'S2-L2', 'Exam DIGI1-L2',    '2025-05-15', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000014', 'DIGI1',   '2025-2026', 'S1-L3', 'Exam DIGI1-L3',    '2026-01-25', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000015', 'METIER1', '2024-2025', 'S1-L2', 'Exam METIER1-L2',  '2025-01-10', '08:00', '10:00', 1, 1),
  ('22000000-1000-4000-8000-000000000016', 'ARCHI1',  '2025-2026', 'S2-L3', 'Exam ARCHI1-TN',   '2026-05-20', '08:00', '10:00', 1, 1)
) AS v(eid, cref, ylabel, slabel, label, dt, st, et, num, den)
JOIN course c ON c.course_reference = v.cref
JOIN academic_year ay ON ay.label = v.ylabel
JOIN semester s ON s.label = v.slabel
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 9. ENROLLMENTS
-- ---------------------------------------------------------------------------
INSERT INTO enrollment (id, student_id, parcours_id, group_id, start_date, end_date)
SELECT gen_random_uuid(), s.id, p.id, g.id, d.sd::date, d.ed::date
FROM student s
CROSS JOIN (VALUES
  ('2023-09-01', '2024-06-30', 'EL', 'K1'),
  ('2024-07-01', '2025-06-30', 'EL', 'K1'),
  ('2025-07-01', NULL,         'EL', 'K2')
) AS d(sd, ed, pcode, gref)
JOIN parcours p ON p.code = d.pcode
JOIN groups g ON g.reference = d.gref
WHERE s.student_number IN (
  'STD24001','STD24002','STD24003','STD24004','STD24005',
  'STD24006','STD24007','STD24008','STD24009','STD24010',
  'STD24011','STD24012','STD24013','STD24017','STD24018',
  'STD24189'
)
AND NOT EXISTS (
  SELECT 1 FROM enrollment e WHERE e.student_id = s.id AND e.start_date = d.sd::date
);

UPDATE enrollment e
SET parcours_id = 'd1000000-0000-4000-8000-000000000002'::uuid,
    group_id    = 'e1000000-0000-4000-8000-000000000003'::uuid
FROM student s
WHERE e.student_id = s.id AND s.student_number = 'STD24001' AND e.start_date = DATE '2023-09-01';

UPDATE enrollment e SET group_id = 'e1000000-0000-4000-8000-000000000003'::uuid
FROM student s WHERE e.student_id = s.id AND s.student_number IN ('STD24017','STD24018') AND e.start_date = DATE '2023-09-01';

UPDATE enrollment e SET group_id = 'e1000000-0000-4000-8000-000000000001'::uuid
FROM student s WHERE e.student_id = s.id AND s.student_number IN ('STD24017','STD24018') AND e.start_date = DATE '2024-07-01';

UPDATE enrollment e SET group_id = 'e1000000-0000-4000-8000-000000000002'::uuid
FROM student s WHERE e.student_id = s.id AND s.student_number IN ('STD24017','STD24018') AND e.start_date = DATE '2025-07-01';

INSERT INTO enrollment (id, student_id, parcours_id, group_id, start_date, end_date)
SELECT gen_random_uuid(), s.id, p.id, g.id, d.sd::date, d.ed::date
FROM student s
CROSS JOIN (VALUES
  ('2023-09-01', '2024-06-30', 'TN', 'K3'),
  ('2024-07-01', '2025-06-30', 'TN', 'K3'),
  ('2025-07-01', NULL,         'TN', 'K3')
) AS d(sd, ed, pcode, gref)
JOIN parcours p ON p.code = d.pcode
JOIN groups g ON g.reference = d.gref
WHERE s.student_number IN ('STD24014','STD24015','STD24016','STD24019','STD24020')
AND NOT EXISTS (
  SELECT 1 FROM enrollment e WHERE e.student_id = s.id AND e.start_date = d.sd::date
);

-- ---------------------------------------------------------------------------
-- 10. GRADES
-- ---------------------------------------------------------------------------
INSERT INTO grade (id, student_id, exam_id, value, status, entered_by_user_id)
SELECT gen_random_uuid(), s.id, e.id, v.val::numeric, 'PUBLISHED',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1)
FROM student s
CROSS JOIN (VALUES
  ('Exam PROG4-L1', 14.0), ('Exam MATH1', 13.0), ('Exam SYS1-L1', 12.5),
  ('Exam PROG4-CC', 14.0), ('Exam PROG4-Final', 15.0), ('Exam WEB1', 13.0),
  ('Exam BDD1-L2', 12.0), ('Exam ARCHI1', 14.5), ('Exam BDD1', 13.5), ('Exam SYS1-L3', 12.0)
) AS v(elabel, val)
JOIN exam e ON e.label = v.elabel
WHERE s.student_number IN (
  'STD24001','STD24002','STD24003','STD24004','STD24005',
  'STD24006','STD24007','STD24008','STD24009','STD24010',
  'STD24017','STD24018','STD24189'
)
AND NOT EXISTS (SELECT 1 FROM grade g WHERE g.student_id = s.id AND g.exam_id = e.id);

INSERT INTO grade (id, student_id, exam_id, value, status, entered_by_user_id)
SELECT gen_random_uuid(), s.id, e.id, 14.0, 'PUBLISHED',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1)
FROM student s
JOIN exam e ON e.label = 'Exam METIER1'
WHERE s.student_number = 'STD24001'
AND NOT EXISTS (SELECT 1 FROM grade g WHERE g.student_id = s.id AND g.exam_id = e.id);

INSERT INTO grade (id, student_id, exam_id, value, status, entered_by_user_id)
SELECT gen_random_uuid(), s.id, e.id, v.val::numeric, 'PUBLISHED',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1)
FROM student s
CROSS JOIN (VALUES
  ('Exam PROG4-L1', 12.0), ('Exam MATH1', 11.0), ('Exam SYS1-L1', 11.0),
  ('Exam PROG4-CC', 11.0), ('Exam PROG4-Final', 12.0), ('Exam WEB1', 8.0),
  ('Exam BDD1-L2', 11.0), ('Exam ARCHI1', 11.0), ('Exam BDD1', 10.5), ('Exam SYS1-L3', 11.0)
) AS v(elabel, val)
JOIN exam e ON e.label = v.elabel
WHERE s.student_number IN ('STD24011','STD24012','STD24013')
AND NOT EXISTS (SELECT 1 FROM grade g WHERE g.student_id = s.id AND g.exam_id = e.id);

INSERT INTO grade (id, student_id, exam_id, value, status, entered_by_user_id)
SELECT gen_random_uuid(), s.id, e.id, v.val::numeric, 'PUBLISHED',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1)
FROM student s
CROSS JOIN (VALUES
  ('Exam METIER1', 14.0), ('Exam MATH1', 13.0), ('Exam DIGI1-L1', 12.5),
  ('Exam METIER1-L2', 13.5), ('Exam WEB1', 12.0), ('Exam DIGI1-L2', 14.0),
  ('Exam BDD1', 13.0), ('Exam DIGI1-L3', 15.0), ('Exam ARCHI1-TN', 12.5)
) AS v(elabel, val)
JOIN exam e ON e.label = v.elabel
WHERE s.student_number IN ('STD24014','STD24015','STD24016')
AND NOT EXISTS (SELECT 1 FROM grade g WHERE g.student_id = s.id AND g.exam_id = e.id);

INSERT INTO grade (id, student_id, exam_id, value, status, entered_by_user_id)
SELECT gen_random_uuid(), s.id, e.id, v.val::numeric, 'PUBLISHED',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1)
FROM student s
CROSS JOIN (VALUES
  ('Exam METIER1', 12.0), ('Exam MATH1', 11.0), ('Exam DIGI1-L1', 11.0),
  ('Exam METIER1-L2', 12.0), ('Exam WEB1', 11.0), ('Exam DIGI1-L2', 11.0),
  ('Exam BDD1', 11.0), ('Exam DIGI1-L3', 7.0), ('Exam ARCHI1-TN', 11.0)
) AS v(elabel, val)
JOIN exam e ON e.label = v.elabel
WHERE s.student_number IN ('STD24019','STD24020')
AND NOT EXISTS (SELECT 1 FROM grade g WHERE g.student_id = s.id AND g.exam_id = e.id);

-- ---------------------------------------------------------------------------
-- 11. GRADE HISTORY
-- ---------------------------------------------------------------------------
INSERT INTO grade_history (id, grade_id, old_value, new_value, reason, changed_by_user_id, changed_at)
SELECT gen_random_uuid(), g.id, 12.00, g.value,
       'Correction apres reclamation etudiant',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1), now()
FROM grade g
JOIN student s ON s.id = g.student_id
JOIN exam e ON e.id = g.exam_id
WHERE s.student_number = 'STD24001' AND e.label = 'Exam PROG4-Final'
  AND NOT EXISTS (SELECT 1 FROM grade_history gh WHERE gh.grade_id = g.id)
LIMIT 1;

INSERT INTO grade_history (id, grade_id, old_value, new_value, reason, changed_by_user_id, changed_at)
SELECT gen_random_uuid(), g.id, 9.50, g.value,
       'Erreur de saisie corrige par administrateur',
       (SELECT id FROM users WHERE email = 'demo.admin@hei.mg' LIMIT 1), now()
FROM grade g
JOIN student s ON s.id = g.student_id
JOIN exam e ON e.id = g.exam_id
WHERE s.student_number = 'STD24002' AND e.label = 'Exam WEB1'
  AND NOT EXISTS (SELECT 1 FROM grade_history gh WHERE gh.grade_id = g.id)
LIMIT 1;