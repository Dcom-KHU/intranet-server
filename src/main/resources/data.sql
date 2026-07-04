-- 개발/테스트용 초기 데이터
-- 서버 시작 시 자동 삽입 (H2 인메모리)


-- 관리자 계정 (비밀번호: admin123)
INSERT INTO users (login_id, password, name, student_id, email, phone_number, role, status, created_at)
VALUES (
           'admin',
           '$2a$10$c5m04guDtF9LZUYDo3TeJukgC.lGoYBtWvtV/6eYJkZTkP4zhrJfW',
           '관리자',
           '2023000001',
           'davepark1111007@gmail.com',
           '010-0000-0001',
           'ADMIN',
           'APPROVED',
           NOW()
       );

-- 일반 유저 계정 (비밀번호: user123)
INSERT INTO users (login_id, password, name, student_id, email, phone_number, role, status, created_at)
VALUES (
           'testuser',
           '$2a$10$c5m04guDtF9LZUYDo3TeJukgC.lGoYBtWvtV/6eYJkZTkP4zhrJfW',
           '테스트유저',
           '2023000002',
           'davepark10071@gmail.com',
           '010-0000-0002',
           'USER',
           'APPROVED',
           NOW()
       );

-- 승인 대기 유저 (비밀번호: pending123)
INSERT INTO users (login_id, password, name, student_id, email, phone_number, role, status, created_at)
VALUES (
           'pendinguser',
           '$2a$10$c5m04guDtF9LZUYDo3TeJukgC.lGoYBtWvtV/6eYJkZTkP4zhrJfW',
           '대기유저',
           '2023000003',
           'davepark1007@gmail.com',
           '010-0000-0003',
           'USER',
           'PENDING',
           NOW()
       );