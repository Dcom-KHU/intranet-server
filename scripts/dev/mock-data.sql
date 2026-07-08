-- D.COM intranet frontend mock seed data for MariaDB dev DB.
-- Source: https://github.com/Dcom-KHU/intranet-web/tree/main/src/mocks
-- Purpose: API integration checks with non-empty responses. Apply to a fresh development DB.
-- Shared login accounts: admin/admin1234, user/user1234. Passwords are stored as BCrypt hashes.
-- Note: frontend mocks contain duplicate student numbers with different names.
--       users.student_id is unique, so duplicate student numbers reuse the first user-data.mock.ts row.

SET NAMES utf8mb4;
START TRANSACTION;

-- users
INSERT INTO users (id, login_id, password, name, student_id, email, phone_number, role, status, created_at, last_login_at, approved_at)
VALUES
    (1, 'hong123', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '홍길동', '20230001', 'hong@gmail.com', '010-1234-5678', 'USER', 'PENDING', '2026-06-24 09:00:00', '2026-06-20 09:00:00', NULL),
    (2, 'hello', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '김하늘', '20230002', 'hello@gmail.com', '010-2222-3333', 'USER', 'PENDING', '2026-06-23 09:00:00', '2026-06-18 09:00:00', NULL),
    (3, 'leejiwon', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '이지원', '20230003', 'jiwon@gmail.com', '010-4444-5555', 'USER', 'APPROVED', '2026-05-15 09:00:00', '2026-06-25 09:00:00', '2026-05-15 09:00:00'),
    (4, 'parkseo', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '박서연', '20230004', 'seo@gmail.com', '010-6666-7777', 'USER', 'APPROVED', '2026-04-02 09:00:00', '2026-06-22 09:00:00', '2026-04-02 09:00:00'),
    (5, 'user', '$2y$10$4rB0kOQNcuyO.WLDA41EmuYUb5UU/Ee6UCoPObUYllzzSsKoVxeLG', '최민준', '20209999', 'user@gmail.com', '010-0000-0000', 'USER', 'APPROVED', '2026-03-10 09:00:00', '2026-06-21 09:00:00', '2026-03-10 09:00:00'),
    (6, 'admin', '$2y$10$EuE4H64ONbRjtt0tm9K48eEjxWpTYXLuA.kikUCCmVS3BjNvdYXMG', '관리자', '20239999', 'admin@gmail.com', '010-0000-0000', 'ADMIN', 'APPROVED', '2026-01-01 09:00:00', '2026-06-25 09:00:00', '2026-01-01 09:00:00'),
    (7, 'yuna24', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '정유나', '20240011', 'yuna@gmail.com', '010-1111-2222', 'USER', 'PENDING', '2026-06-25 09:00:00', '2026-06-19 09:00:00', NULL),
    (100, 'mock_20201234', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '표지훈', '20201234', 'mock-20201234@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-06-20 09:00:00', '2026-06-20 09:00:00', '2026-06-20 09:00:00'),
    (101, 'mock_20201111', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '허남준', '20201111', 'mock-20201111@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-06-21 09:00:00', '2026-06-21 09:00:00', '2026-06-21 09:00:00'),
    (102, 'mock_20201333', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '안유진', '20201333', 'mock-20201333@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-06-22 09:00:00', '2026-06-22 09:00:00', '2026-06-22 09:00:00'),
    (103, 'mock_20201444', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '김선호', '20201444', 'mock-20201444@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-06-23 09:00:00', '2026-06-23 09:00:00', '2026-06-23 09:00:00'),
    (104, 'mock_20201555', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '지창욱', '20201555', 'mock-20201555@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-06-24 09:00:00', '2026-06-24 09:00:00', '2026-06-24 09:00:00'),
    (105, 'mock_2022100001', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '김우빈', '2022100001', 'mock-2022100001@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-16 09:00:00', '2026-05-16 09:00:00', '2026-05-16 09:00:00'),
    (106, 'mock_2022100002', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '박보검', '2022100002', 'mock-2022100002@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-16 09:00:00', '2026-05-16 09:00:00', '2026-05-16 09:00:00'),
    (107, 'mock_2022100003', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '차은우', '2022100003', 'mock-2022100003@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-10 09:00:00', '2026-05-10 09:00:00', '2026-05-10 09:00:00'),
    (108, 'mock_2022100004', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '정해인', '2022100004', 'mock-2022100004@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-10 09:00:00', '2026-05-10 09:00:00', '2026-05-10 09:00:00'),
    (109, 'mock_2022100005', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '김연아', '2022100005', 'mock-2022100005@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-27 09:00:00', '2026-04-27 09:00:00', '2026-04-27 09:00:00'),
    (110, 'mock_2022100006', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '한지민', '2022100006', 'mock-2022100006@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-27 09:00:00', '2026-04-27 09:00:00', '2026-04-27 09:00:00'),
    (111, 'mock_2022100007', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '손예진', '2022100007', 'mock-2022100007@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-03-18 09:00:00', '2026-03-18 09:00:00', '2026-03-18 09:00:00'),
    (112, 'mock_2022100008', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '강하늘', '2022100008', 'mock-2022100008@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-03-18 09:00:00', '2026-03-18 09:00:00', '2026-03-18 09:00:00'),
    (113, 'mock_2022100009', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '아이유', '2022100009', 'mock-2022100009@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-02-11 09:00:00', '2026-02-11 09:00:00', '2026-02-11 09:00:00'),
    (114, 'mock_2022100010', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '수지', '2022100010', 'mock-2022100010@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-02-11 09:00:00', '2026-02-11 09:00:00', '2026-02-11 09:00:00'),
    (115, 'mock_2022100011', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '이도현', '2022100011', 'mock-2022100011@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-01-20 09:00:00', '2026-01-20 09:00:00', '2026-01-20 09:00:00'),
    (116, 'mock_2022100012', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '박서준', '2022100012', 'mock-2022100012@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-01-20 09:00:00', '2026-01-20 09:00:00', '2026-01-20 09:00:00'),
    (117, 'mock_2022100013', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '이민호', '2022100013', 'mock-2022100013@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-17 09:00:00', '2026-05-17 09:00:00', '2026-05-17 09:00:00'),
    (118, 'mock_2022100014', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '한소희', '2022100014', 'mock-2022100014@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-11 09:00:00', '2026-05-11 09:00:00', '2026-05-11 09:00:00'),
    (119, 'mock_2022100015', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '박보영', '2022100015', 'mock-2022100015@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-28 09:00:00', '2026-04-28 09:00:00', '2026-04-28 09:00:00'),
    (121, 'mock_2022101001', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '카리나', '2022101001', 'mock-2022101001@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-21 09:00:00', '2026-05-21 09:00:00', '2026-05-21 09:00:00'),
    (122, 'mock_2022101002', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '윈터', '2022101002', 'mock-2022101002@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-21 09:00:00', '2026-05-21 09:00:00', '2026-05-21 09:00:00'),
    (123, 'mock_2022101003', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '장원영', '2022101003', 'mock-2022101003@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-18 09:00:00', '2026-05-18 09:00:00', '2026-05-18 09:00:00'),
    (124, 'mock_2022101004', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '안유진', '2022101004', 'mock-2022101004@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-18 09:00:00', '2026-05-18 09:00:00', '2026-05-18 09:00:00'),
    (125, 'mock_2022101005', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '아이유', '2022101005', 'mock-2022101005@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-12 09:00:00', '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
    (126, 'mock_2022101006', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '지효', '2022101006', 'mock-2022101006@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-12 09:00:00', '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
    (127, 'mock_2022101007', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '사나', '2022101007', 'mock-2022101007@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-30 09:00:00', '2026-04-30 09:00:00', '2026-04-30 09:00:00'),
    (128, 'mock_2022101008', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '닝닝', '2022101008', 'mock-2022101008@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-30 09:00:00', '2026-04-30 09:00:00', '2026-04-30 09:00:00'),
    (129, 'mock_2022101009', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '해린', '2022101009', 'mock-2022101009@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-15 09:00:00', '2026-04-15 09:00:00', '2026-04-15 09:00:00'),
    (130, 'mock_2022101010', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '하니', '2022101010', 'mock-2022101010@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-15 09:00:00', '2026-04-15 09:00:00', '2026-04-15 09:00:00'),
    (131, 'mock_2022101011', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '민지', '2022101011', 'mock-2022101011@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-03-28 09:00:00', '2026-03-28 09:00:00', '2026-03-28 09:00:00'),
    (132, 'mock_2022101012', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '다니엘', '2022101012', 'mock-2022101012@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-03-28 09:00:00', '2026-03-28 09:00:00', '2026-03-28 09:00:00'),
    (133, 'mock_2022101013', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '태연', '2022101013', 'mock-2022101013@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-03-10 09:00:00', '2026-03-10 09:00:00', '2026-03-10 09:00:00'),
    (134, 'mock_2022101014', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '로제', '2022101014', 'mock-2022101014@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-02-22 09:00:00', '2026-02-22 09:00:00', '2026-02-22 09:00:00'),
    (135, 'mock_2022101015', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '지수', '2022101015', 'mock-2022101015@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-02-22 09:00:00', '2026-02-22 09:00:00', '2026-02-22 09:00:00'),
    (139, 'mock_20210012', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '신정안', '20210012', 'mock-20210012@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2025-05-05 09:00:00', '2025-05-05 09:00:00', '2025-05-05 09:00:00'),
    (140, 'mock_20220014', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '최진영', '20220014', 'mock-20220014@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-20 09:00:00', '2026-05-20 09:00:00', '2026-05-20 09:00:00'),
    (141, 'mock_20210032', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '최진영', '20210032', 'mock-20210032@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-15 09:00:00', '2026-05-15 09:00:00', '2026-05-15 09:00:00'),
    (142, 'mock_20220027', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '최진영', '20220027', 'mock-20220027@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-10 09:00:00', '2026-05-10 09:00:00', '2026-05-10 09:00:00'),
    (143, 'mock_20230018', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '이수민', '20230018', 'mock-20230018@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-12 09:00:00', '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
    (144, 'mock_20220045', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '이수민', '20220045', 'mock-20220045@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-08 09:00:00', '2026-05-08 09:00:00', '2026-05-08 09:00:00'),
    (145, 'mock_20210009', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '곽민서', '20210009', 'mock-20210009@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-05-03 09:00:00', '2026-05-03 09:00:00', '2026-05-03 09:00:00'),
    (146, 'mock_20200021', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '곽민서', '20200021', 'mock-20200021@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-28 09:00:00', '2026-04-28 09:00:00', '2026-04-28 09:00:00'),
    (147, 'mock_20230007', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '표지훈', '20230007', 'mock-20230007@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-20 09:00:00', '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
    (148, 'mock_20220003', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '표지훈', '20220003', 'mock-20220003@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-18 09:00:00', '2026-04-18 09:00:00', '2026-04-18 09:00:00'),
    (149, 'mock_20210041', '$2y$10$OymH1Fd62ejo0tUGTVvtD.r0qymiKNblx7lTe.0ItYEGAgaJpiVc2', '표지훈', '20210041', 'mock-20210041@dcom.local', '010-0000-0000', 'USER', 'APPROVED', '2026-04-04 09:00:00', '2026-04-04 09:00:00', '2026-04-04 09:00:00');

-- notices
INSERT INTO notices (notice_id, title, content, author_id, created_at, updated_at)
VALUES
    (1, '2026 D.COM 여름 프로젝트 팀 모집 안내', '안녕하세요.\n\n2026년 여름 프로젝트 팀원을 모집합니다.\n\n모집 분야\n- 프론트엔드\n- 백엔드\n- AI/데이터\n- 디자인\n\n신청 기간: 2026.06.20 ~ 2026.06.30\n\n많은 관심과 참여 부탁드립니다.', 6, '2026-06-20 09:00:00', '2026-06-20 09:00:00'),
    (2, '정기 세미나 발표자 신청 안내', '다음 정기 세미나 발표자를 모집합니다.\n\n관심 있는 기술 주제를 자유롭게 선정하여 발표할 수 있습니다.\n\n발표 신청 마감: 2026.06.21\n발표 일시: 2026.06.28', 6, '2026-06-14 09:00:00', '2026-06-14 09:00:00'),
    (3, '동아리방 이용 수칙 변경 안내', '동아리방 이용 수칙이 일부 변경되었습니다.\n\n주요 변경 사항\n- 퇴실 전 정리 정돈 필수\n- 음식물 반입 후 즉시 정리\n- 공용 장비 사용 후 원위치\n\n변경된 수칙을 확인해 주세요.', 6, '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
    (4, '신입 부원 Git 기초 워크숍 일정', '신입 부원을 대상으로 Git 기초 워크숍을 진행합니다.\n\n일시: 2026.06.07 14:00\n장소: 공학관 301호\n\n노트북을 지참해 주세요.', 6, '2026-05-29 09:00:00', '2026-05-29 09:00:00'),
    (5, '기말고사 기간 활동 일정 조정 안내', '기말고사 기간 동안 정기 활동 일정이 일부 조정됩니다.\n\n조정 기간: 2026.06.10 ~ 2026.06.24\n\n자세한 일정은 추후 공지 예정입니다.', 6, '2026-05-18 09:00:00', '2026-05-18 09:00:00'),
    (6, '홈페이지 개선 의견 수렴', '동아리 홈페이지 개선을 위한 의견을 받고 있습니다.\n\n불편했던 점이나 추가되었으면 하는 기능을 자유롭게 제안해 주세요.\n\n제출 기한: 2026.05.15', 6, '2026-05-03 09:00:00', '2026-05-03 09:00:00');

-- notice_files
INSERT INTO notice_files (notice_file_id, notice_id, original_file_name, stored_file_name, object_key, file_url, file_size, content_type)
VALUES
    (1, 1, '2026-summer-project-guide.pdf', 'mock-2026-summer-project-guide.pdf', 'notice/mock/2026-summer-project-guide.pdf', '/uploads/notice/mock/2026-summer-project-guide.pdf', 0, 'application/pdf'),
    (2, 3, 'clubroom-rules.pdf', 'mock-clubroom-rules.pdf', 'notice/mock/clubroom-rules.pdf', '/uploads/notice/mock/clubroom-rules.pdf', 0, 'application/pdf'),
    (3, 6, 'homepage-feedback-form.docx', 'mock-homepage-feedback-form.docx', 'notice/mock/homepage-feedback-form.docx', '/uploads/notice/mock/homepage-feedback-form.docx', 0, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document');

-- info_posts
INSERT INTO info_posts (post_id, author_id, title, content, views, created_at, updated_at)
VALUES
    (1, 100, '시간 복잡도 Big-O 핵심 정리 (면접 필수)', '시간 복잡도는 알고리즘 성능을 평가하는 가장 기본적인 기준이다.\n\n- O(1): 해시 접근, 배열 인덱스 접근\n- O(log N): 이진 탐색\n- O(N): 순차 탐색\n- O(N log N): Merge Sort, Quick Sort\n- O(N^2): Bubble Sort\n\n면접 핵심:\n이진 탐색이 왜 O(log N)인지 설명할 수 있어야 한다.\n=> 매 단계마다 탐색 범위가 절반으로 줄어든다.', 11, '2026-06-20 09:00:00', NULL),
    (2, 101, 'TCP 3-way handshake 동작 원리 정리', 'TCP는 신뢰성 있는 연결을 제공하는 프로토콜이다.\n\n[3-way handshake]\n1. SYN (Client → Server)\n2. SYN-ACK (Server → Client)\n3. ACK (Client → Server)\n\n연결 종료:\nFIN / ACK 교환 후 TIME_WAIT 상태 유지', 12, '2026-06-21 09:00:00', NULL),
    (3, 102, '운영체제: 프로세스 vs 스레드 완벽 비교', '프로세스:\n- 독립된 메모리 공간\n- IPC 필요\n\n스레드:\n- 같은 프로세스 내 실행 단위\n- Heap 공유, Stack 독립\n\n핵심:\n자원 공유 여부와 컨텍스트 스위칭 비용이 차이점이다.', 13, '2026-06-22 09:00:00', NULL),
    (4, 103, 'DB 인덱스(B-Tree) 구조 이해하기', 'B-Tree 인덱스는 균형 트리 구조로 데이터 검색을 최적화한다.\n\n특징:\n- 모든 leaf node depth 동일\n- range query에 강함\n- insert/delete 시 rebalancing 발생\n\n주의:\n인덱스를 무조건 많이 만든다고 성능이 좋아지지 않는다.', 14, '2026-06-23 09:00:00', NULL),
    (5, 104, '동기/비동기 & Blocking/Non-blocking 차이', '이 개념은 서로 다른 축이다.\n\n동기/비동기:\n- 결과를 기다리는 방식\n\nBlocking/Non-blocking:\n- 제어권 반환 여부\n\n조합:\n- Sync + Blocking: 일반 함수 호출\n- Async + Non-blocking: 이벤트 루프 기반 처리 (Node.js)\n\n핵심:\n두 개념을 혼동하면 안 된다.', 15, '2026-06-24 09:00:00', NULL),
    (6, 6, '정보공유 댓글 연결용 mock 게시글 6', 'comments.mock.ts에 postId 6 댓글이 있어 FK 유지를 위해 추가한 placeholder 게시글입니다.', 0, '2026-03-01 09:00:00', NULL),
    (7, 6, '정보공유 댓글 연결용 mock 게시글 7', 'comments.mock.ts에 postId 7 댓글이 있어 FK 유지를 위해 추가한 placeholder 게시글입니다.', 0, '2026-03-01 09:00:00', NULL),
    (8, 6, '정보공유 댓글 연결용 mock 게시글 8', 'comments.mock.ts에 postId 8 댓글이 있어 FK 유지를 위해 추가한 placeholder 게시글입니다.', 0, '2026-03-01 09:00:00', NULL);

-- info_post_files
INSERT INTO info_post_files (file_id, post_id, original_file_name, stored_file_name, object_key, file_url, file_size, content_type)
VALUES
    (1, 1, 'big-o-summary.pdf', 'mock-big-o-summary.pdf', 'info/mock/big-o-summary.pdf', '/uploads/info/mock/big-o-summary.pdf', 0, 'application/pdf'),
    (2, 1, 'algorithm-cheatsheet.png', 'mock-algorithm-cheatsheet.png', 'info/mock/algorithm-cheatsheet.png', '/uploads/info/mock/algorithm-cheatsheet.png', 0, 'image/png'),
    (3, 3, 'process-thread-diagram.png', 'mock-process-thread-diagram.png', 'info/mock/process-thread-diagram.png', '/uploads/info/mock/process-thread-diagram.png', 0, 'image/png');

-- info_comments
INSERT INTO info_comments (comment_id, post_id, author_id, content, created_at, updated_at)
VALUES
    (1, 1, 121, '좋은 정보 감사합니다!', '2026-05-21 09:00:00', NULL),
    (2, 1, 122, '덕분에 신청 기간 놓치지 않았어요.', '2026-05-21 09:00:00', NULL),
    (3, 2, 123, '공유해주셔서 감사합니다.', '2026-05-18 09:00:00', NULL),
    (4, 2, 124, '정리도 깔끔해서 보기 편했어요.', '2026-05-18 09:00:00', NULL),
    (5, 3, 125, '혹시 관련 링크도 있을까요?', '2026-05-12 09:00:00', NULL),
    (6, 3, 126, '유용한 정보네요. 감사합니다!', '2026-05-12 09:00:00', NULL),
    (7, 4, 127, '관심 있던 내용이었는데 도움이 됐습니다.', '2026-04-30 09:00:00', NULL),
    (8, 4, 128, '친구들에게도 공유했어요!', '2026-04-30 09:00:00', NULL),
    (9, 5, 129, '이런 게시글 자주 올라오면 좋겠네요.', '2026-04-15 09:00:00', NULL),
    (10, 5, 130, '좋은 정보 감사합니다. 도움이 많이 됐어요.', '2026-04-15 09:00:00', NULL),
    (11, 6, 131, '궁금했던 내용이 해결됐습니다.', '2026-03-28 09:00:00', NULL),
    (12, 6, 132, '추가 정보가 있으면 알려주세요.', '2026-03-28 09:00:00', NULL),
    (13, 7, 133, '덕분에 많은 도움이 되었습니다.', '2026-03-10 09:00:00', NULL),
    (14, 8, 134, '최신 정보로 업데이트해주셔서 감사합니다.', '2026-02-22 09:00:00', NULL),
    (15, 8, 135, '다음에도 유익한 정보 기대하겠습니다.', '2026-02-22 09:00:00', NULL),
    (16, 1, 6, '면접 준비에 바로 활용할 수 있겠네요. 감사합니다.', '2026-06-29 09:00:00', NULL);

-- photo_posts
INSERT INTO photo_posts (album_id, event_name, activity_date, description, created_at)
VALUES
    (1, '2026-1 D.COM 커리어세션', '2026-05-16', '2026년 1학기 D.COM 커리어 세션 성료!\n이번 학기 D.COM 학우들을 위해 현업에서 활동 중인 자랑스러운 선배님들이 한걸음에 달려와 주셨습니다.\n백엔드, 웹 AI 연구까지 평소 쉽게 들을 수 없었던 생생한 실무 이야기와 커리어 꿀팁으로 가득 채워진 뜻깊은 시간이었습니다.\n바쁜 시간 내어 소중한 경험을 나눠주신 선배님들께 다시 한번 감사드립니다.', '2026-05-16 09:00:00'),
    (2, '2026-1 D.COM 정기 세미나', '2026-05-09', '프론트엔드와 백엔드의 최신 기술 동향을 주제로 정기 세미나를 진행했습니다. 부원들이 직접 발표를 준비하고 질의응답을 통해 지식을 공유했습니다.', '2026-05-09 09:00:00'),
    (3, '2026-1 D.COM 네트워킹 데이', '2026-04-26', '기수 간 교류를 활성화하기 위해 네트워킹 프로그램을 진행했습니다. 팀별 미션과 자유로운 대화를 통해 친목을 다지는 시간을 가졌습니다.', '2026-04-26 09:00:00'),
    (4, '2026-1 D.COM 프로젝트 발표회', '2026-04-12', '한 학기 동안 진행한 프로젝트 결과물을 발표하고 피드백을 주고받는 시간을 가졌습니다. 다양한 아이디어와 기술적 시도가 돋보인 행사였습니다.', '2026-04-12 09:00:00'),
    (5, '2026-1 D.COM MT', '2026-03-29', '부원 간 친목을 다지기 위해 MT를 진행했습니다. 레크리에이션과 팀 활동을 통해 서로를 알아가고 즐거운 추억을 만들었습니다.', '2026-03-29 09:00:00'),
    (6, '2026-1 D.COM 신입부원 환영회', '2026-03-15', '새롭게 합류한 신입부원들을 환영하는 자리를 마련했습니다. 동아리 소개와 선후배 교류를 통해 첫 만남을 따뜻하게 시작했습니다.', '2026-03-15 09:00:00'),
    (7, '2025-2 D.COM 종강총회', '2025-12-20', '한 학기를 마무리하며 활동을 돌아보고 우수 활동 부원을 시상하는 종강총회를 진행했습니다.', '2025-12-20 09:00:00'),
    (8, '2025-2 D.COM 해커톤', '2025-11-22', '24시간 동안 팀별로 서비스를 기획하고 개발하는 해커톤을 개최했습니다. 창의적인 아이디어와 협업 역량이 돋보이는 행사였습니다.', '2025-11-22 09:00:00');

-- photo_post_images
INSERT INTO photo_post_images (image_id, album_id, original_file_name, stored_file_name, object_key, file_url, file_size, content_type, upload_order)
VALUES
    (1, 1, 'khu-bg-1.png', 'album-1-0-khu-bg-1.png', 'photo/mock/album-1/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 0),
    (2, 1, 'khu-bg-2.jpg', 'album-1-1-khu-bg-2.jpg', 'photo/mock/album-1/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 1),
    (3, 1, 'khu-bg-3.jpg', 'album-1-2-khu-bg-3.jpg', 'photo/mock/album-1/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 2),
    (4, 1, 'khu-bg-1.png', 'album-1-3-khu-bg-1.png', 'photo/mock/album-1/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 3),
    (5, 1, 'khu-bg-2.jpg', 'album-1-4-khu-bg-2.jpg', 'photo/mock/album-1/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 4),
    (6, 2, 'khu-bg-2.jpg', 'album-2-0-khu-bg-2.jpg', 'photo/mock/album-2/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 0),
    (7, 2, 'khu-bg-3.jpg', 'album-2-1-khu-bg-3.jpg', 'photo/mock/album-2/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 1),
    (8, 2, 'khu-bg-1.png', 'album-2-2-khu-bg-1.png', 'photo/mock/album-2/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 2),
    (9, 2, 'khu-bg-2.jpg', 'album-2-3-khu-bg-2.jpg', 'photo/mock/album-2/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 3),
    (10, 2, 'khu-bg-3.jpg', 'album-2-4-khu-bg-3.jpg', 'photo/mock/album-2/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 4),
    (11, 2, 'khu-bg-1.png', 'album-2-5-khu-bg-1.png', 'photo/mock/album-2/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 5),
    (12, 2, 'khu-bg-2.jpg', 'album-2-6-khu-bg-2.jpg', 'photo/mock/album-2/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 6),
    (13, 2, 'khu-bg-3.jpg', 'album-2-7-khu-bg-3.jpg', 'photo/mock/album-2/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 7),
    (14, 3, 'khu-bg-3.jpg', 'album-3-0-khu-bg-3.jpg', 'photo/mock/album-3/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 0),
    (15, 3, 'khu-bg-1.png', 'album-3-1-khu-bg-1.png', 'photo/mock/album-3/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 1),
    (16, 3, 'khu-bg-2.jpg', 'album-3-2-khu-bg-2.jpg', 'photo/mock/album-3/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 2),
    (17, 3, 'khu-bg-3.jpg', 'album-3-3-khu-bg-3.jpg', 'photo/mock/album-3/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 3),
    (18, 3, 'khu-bg-1.png', 'album-3-4-khu-bg-1.png', 'photo/mock/album-3/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 4),
    (19, 3, 'khu-bg-2.jpg', 'album-3-5-khu-bg-2.jpg', 'photo/mock/album-3/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 5),
    (20, 4, 'khu-bg-1.png', 'album-4-0-khu-bg-1.png', 'photo/mock/album-4/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 0),
    (21, 4, 'khu-bg-2.jpg', 'album-4-1-khu-bg-2.jpg', 'photo/mock/album-4/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 1),
    (22, 4, 'khu-bg-3.jpg', 'album-4-2-khu-bg-3.jpg', 'photo/mock/album-4/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 2),
    (23, 4, 'khu-bg-1.png', 'album-4-3-khu-bg-1.png', 'photo/mock/album-4/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 3),
    (24, 4, 'khu-bg-2.jpg', 'album-4-4-khu-bg-2.jpg', 'photo/mock/album-4/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 4),
    (25, 4, 'khu-bg-3.jpg', 'album-4-5-khu-bg-3.jpg', 'photo/mock/album-4/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 5),
    (26, 4, 'khu-bg-1.png', 'album-4-6-khu-bg-1.png', 'photo/mock/album-4/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 6),
    (27, 4, 'khu-bg-2.jpg', 'album-4-7-khu-bg-2.jpg', 'photo/mock/album-4/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 7),
    (28, 4, 'khu-bg-3.jpg', 'album-4-8-khu-bg-3.jpg', 'photo/mock/album-4/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 8),
    (29, 4, 'khu-bg-1.png', 'album-4-9-khu-bg-1.png', 'photo/mock/album-4/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 9),
    (30, 4, 'khu-bg-2.jpg', 'album-4-10-khu-bg-2.jpg', 'photo/mock/album-4/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 10),
    (31, 4, 'khu-bg-3.jpg', 'album-4-11-khu-bg-3.jpg', 'photo/mock/album-4/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 11),
    (32, 5, 'khu-bg-2.jpg', 'album-5-0-khu-bg-2.jpg', 'photo/mock/album-5/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 0),
    (33, 5, 'khu-bg-3.jpg', 'album-5-1-khu-bg-3.jpg', 'photo/mock/album-5/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 1),
    (34, 5, 'khu-bg-1.png', 'album-5-2-khu-bg-1.png', 'photo/mock/album-5/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 2),
    (35, 5, 'khu-bg-2.jpg', 'album-5-3-khu-bg-2.jpg', 'photo/mock/album-5/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 3),
    (36, 5, 'khu-bg-3.jpg', 'album-5-4-khu-bg-3.jpg', 'photo/mock/album-5/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 4),
    (37, 5, 'khu-bg-1.png', 'album-5-5-khu-bg-1.png', 'photo/mock/album-5/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 5),
    (38, 5, 'khu-bg-2.jpg', 'album-5-6-khu-bg-2.jpg', 'photo/mock/album-5/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 6),
    (39, 5, 'khu-bg-3.jpg', 'album-5-7-khu-bg-3.jpg', 'photo/mock/album-5/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 7),
    (40, 5, 'khu-bg-1.png', 'album-5-8-khu-bg-1.png', 'photo/mock/album-5/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 8),
    (41, 5, 'khu-bg-2.jpg', 'album-5-9-khu-bg-2.jpg', 'photo/mock/album-5/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 9),
    (42, 6, 'khu-bg-3.jpg', 'album-6-0-khu-bg-3.jpg', 'photo/mock/album-6/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 0),
    (43, 6, 'khu-bg-1.png', 'album-6-1-khu-bg-1.png', 'photo/mock/album-6/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 1),
    (44, 6, 'khu-bg-2.jpg', 'album-6-2-khu-bg-2.jpg', 'photo/mock/album-6/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 2),
    (45, 6, 'khu-bg-3.jpg', 'album-6-3-khu-bg-3.jpg', 'photo/mock/album-6/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 3),
    (46, 6, 'khu-bg-1.png', 'album-6-4-khu-bg-1.png', 'photo/mock/album-6/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 4),
    (47, 6, 'khu-bg-2.jpg', 'album-6-5-khu-bg-2.jpg', 'photo/mock/album-6/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 5),
    (48, 6, 'khu-bg-3.jpg', 'album-6-6-khu-bg-3.jpg', 'photo/mock/album-6/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 6),
    (49, 7, 'khu-bg-1.png', 'album-7-0-khu-bg-1.png', 'photo/mock/album-7/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 0),
    (50, 7, 'khu-bg-2.jpg', 'album-7-1-khu-bg-2.jpg', 'photo/mock/album-7/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 1),
    (51, 7, 'khu-bg-3.jpg', 'album-7-2-khu-bg-3.jpg', 'photo/mock/album-7/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 2),
    (52, 7, 'khu-bg-1.png', 'album-7-3-khu-bg-1.png', 'photo/mock/album-7/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 3),
    (53, 7, 'khu-bg-2.jpg', 'album-7-4-khu-bg-2.jpg', 'photo/mock/album-7/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 4),
    (54, 7, 'khu-bg-3.jpg', 'album-7-5-khu-bg-3.jpg', 'photo/mock/album-7/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 5),
    (55, 7, 'khu-bg-1.png', 'album-7-6-khu-bg-1.png', 'photo/mock/album-7/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 6),
    (56, 7, 'khu-bg-2.jpg', 'album-7-7-khu-bg-2.jpg', 'photo/mock/album-7/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 7),
    (57, 7, 'khu-bg-3.jpg', 'album-7-8-khu-bg-3.jpg', 'photo/mock/album-7/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 8),
    (58, 8, 'khu-bg-2.jpg', 'album-8-0-khu-bg-2.jpg', 'photo/mock/album-8/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 0),
    (59, 8, 'khu-bg-3.jpg', 'album-8-1-khu-bg-3.jpg', 'photo/mock/album-8/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 1),
    (60, 8, 'khu-bg-1.png', 'album-8-2-khu-bg-1.png', 'photo/mock/album-8/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 2),
    (61, 8, 'khu-bg-2.jpg', 'album-8-3-khu-bg-2.jpg', 'photo/mock/album-8/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 3),
    (62, 8, 'khu-bg-3.jpg', 'album-8-4-khu-bg-3.jpg', 'photo/mock/album-8/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 4),
    (63, 8, 'khu-bg-1.png', 'album-8-5-khu-bg-1.png', 'photo/mock/album-8/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 5),
    (64, 8, 'khu-bg-2.jpg', 'album-8-6-khu-bg-2.jpg', 'photo/mock/album-8/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 6),
    (65, 8, 'khu-bg-3.jpg', 'album-8-7-khu-bg-3.jpg', 'photo/mock/album-8/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 7),
    (66, 8, 'khu-bg-1.png', 'album-8-8-khu-bg-1.png', 'photo/mock/album-8/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 8),
    (67, 8, 'khu-bg-2.jpg', 'album-8-9-khu-bg-2.jpg', 'photo/mock/album-8/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 9),
    (68, 8, 'khu-bg-3.jpg', 'album-8-10-khu-bg-3.jpg', 'photo/mock/album-8/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 10),
    (69, 8, 'khu-bg-1.png', 'album-8-11-khu-bg-1.png', 'photo/mock/album-8/khu-bg-1.png', '/uploads/photo/mock/khu-bg-1.png', 0, 'image/png', 11),
    (70, 8, 'khu-bg-2.jpg', 'album-8-12-khu-bg-2.jpg', 'photo/mock/album-8/khu-bg-2.jpg', '/uploads/photo/mock/khu-bg-2.jpg', 0, 'image/jpeg', 12),
    (71, 8, 'khu-bg-3.jpg', 'album-8-13-khu-bg-3.jpg', 'photo/mock/album-8/khu-bg-3.jpg', '/uploads/photo/mock/khu-bg-3.jpg', 0, 'image/jpeg', 13);

-- photo_comments
INSERT INTO photo_comments (comment_id, album_id, author_id, content, created_at, updated_at)
VALUES
    (1, 1, 105, '커리어 세션 내용이 정말 유익했어요!', '2026-05-16 09:00:00', NULL),
    (2, 1, 106, '다음에도 이런 행사 기대됩니다.', '2026-05-16 09:00:00', NULL),
    (3, 2, 107, '발표 준비 너무 잘하셨어요.', '2026-05-10 09:00:00', NULL),
    (4, 2, 108, '질문 시간도 알찼습니다.', '2026-05-10 09:00:00', NULL),
    (5, 3, 109, '분위기가 너무 좋았어요.', '2026-04-27 09:00:00', NULL),
    (6, 3, 110, '사진도 예쁘게 나왔네요!', '2026-04-27 09:00:00', NULL),
    (7, 4, 111, '네트워킹 행사 유익했습니다.', '2026-03-18 09:00:00', NULL),
    (8, 4, 112, '좋은 사람들을 많이 만났어요.', '2026-03-18 09:00:00', NULL),
    (9, 5, 113, '세션 구성 정말 좋았습니다.', '2026-02-11 09:00:00', NULL),
    (10, 5, 114, '다음에는 더 길게 진행했으면 좋겠어요.', '2026-02-11 09:00:00', NULL),
    (11, 5, 115, '정말 도움이 되는 시간이었습니다.', '2026-01-20 09:00:00', NULL),
    (12, 8, 116, '준비하신 분들 고생 많으셨어요.', '2026-01-20 09:00:00', NULL),
    (13, 1, 117, '처음 참여했는데 너무 좋았어요.', '2026-05-17 09:00:00', NULL),
    (14, 2, 118, '다음에도 꼭 참여하고 싶어요.', '2026-05-11 09:00:00', NULL),
    (15, 3, 119, '전체적으로 매우 만족스러운 행사였습니다.', '2026-04-28 09:00:00', NULL),
    (16, 1, 5, '행사 사진과 내용 모두 잘 봤습니다!', '2026-06-28 09:00:00', NULL);

-- archives
INSERT INTO archives (archive_id, subject_name, professor_name, created_at, last_modified_at)
VALUES
    (1, '오픈소스SW개발방법및도구', '이성원', '2025-05-05 09:00:00', '2026-05-25 09:00:00'),
    (2, '자료구조', '박제만', '2026-05-10 09:00:00', '2026-05-20 09:00:00'),
    (3, '데이터베이스', '김태연', '2026-05-08 09:00:00', '2026-05-12 09:00:00'),
    (4, '데이터베이스', '이영구', '2026-04-28 09:00:00', '2026-05-03 09:00:00'),
    (5, '컴퓨터네트워크', '고한얼', '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
    (6, '확률및통계학', '김태연', '2026-04-18 09:00:00', '2026-04-18 09:00:00'),
    (7, '논리회로', '정연모', '2026-04-04 09:00:00', '2026-04-04 09:00:00');

-- archive_records
INSERT INTO archive_records (record_id, archive_id, author_id, exam_year, semester, exam_type, content, created_at, updated_at)
VALUES
    (1, 1, 1, 2024, 'FIRST', 'ETC', '', '2026-05-25 09:00:00', NULL),
    (2, 1, 5, 2023, NULL, 'ETC', '[문제 1]\n질문에서 명확하게 요구함: (1) 위치와 (2) 목적의 명시되어야 함. Manager 역할을 수행할 node에서 init을 수행하며, worker node들이 접속할 수 있도록 cluster를 생성하고, worker node들이 cluster에 접속시 사용할 token을 생성함. Worker node 역할을 수행할 node에서 join을 수행하여, manager가 생성한 cluster로 조인을 추가함.', '2026-04-25 09:00:00', NULL),
    (3, 1, 139, 2022, NULL, 'ETC', '', '2025-05-05 09:00:00', NULL),
    (4, 2, 140, 2025, 'FIRST', 'ETC', '자료구조 중간고사 기출 및 정답 정리.', '2026-05-20 09:00:00', NULL),
    (5, 2, 141, 2024, 'SECOND', 'ETC', '자료구조 기말고사 기출문제.', '2026-05-15 09:00:00', NULL),
    (6, 2, 142, NULL, NULL, 'ASSIGNMENT', '자료구조 수업 중 진행된 퀴즈와 과제 모음입니다.', '2026-05-10 09:00:00', NULL),
    (7, 3, 143, 2025, 'FIRST', 'ETC', 'ERD, 정규화 중심의 중간고사 기출.', '2026-05-12 09:00:00', NULL),
    (8, 3, 144, 2025, 'FIRST', 'ETC', '트랜잭션, 인덱스 관련 문제 포함.', '2026-05-08 09:00:00', NULL),
    (9, 4, 145, 2025, 'FIRST', 'ETC', 'SQL, 정규화, 인덱스 중심 문제.', '2026-05-03 09:00:00', NULL),
    (10, 4, 146, 2024, 'SECOND', 'ETC', 'SQL, 트랜잭션, 락 관련 문제 포함.', '2026-04-28 09:00:00', NULL),
    (11, 5, 147, 2025, 'FIRST', 'ETC', '네트워크 보안 및 응용 계층 중심 문제.', '2026-04-20 09:00:00', NULL),
    (12, 6, 148, NULL, NULL, 'MIDTERM', '최근 3년간 중간고사 기출 모음.', '2026-04-18 09:00:00', NULL),
    (13, 7, 149, 2024, 'SECOND', 'ETC', '플립플롭, FSM 중심의 기말고사 자료.', '2026-04-04 09:00:00', NULL);

-- archive_files
INSERT INTO archive_files (archive_file_id, record_id, original_file_name, stored_file_name, object_key, file_url, file_size, content_type, download_count, created_at)
VALUES
    (1, 1, '24-1_오소_중간_기출.pdf', 'mock-24-1_오소_중간_기출.pdf', 'archive/mock/24-1_오소_중간_기출.pdf', '/uploads/archive/mock/24-1_오소_중간_기출.pdf', 0, 'application/pdf', 0, '2026-05-25 09:00:00'),
    (2, 1, '24-1_오소_기말_기출.pdf', 'mock-24-1_오소_기말_기출.pdf', 'archive/mock/24-1_오소_기말_기출.pdf', '/uploads/archive/mock/24-1_오소_기말_기출.pdf', 0, 'application/pdf', 0, '2026-05-25 09:00:00'),
    (3, 1, '24-1_오소_중간_예상문제.pdf', 'mock-24-1_오소_중간_예상문제.pdf', 'archive/mock/24-1_오소_중간_예상문제.pdf', '/uploads/archive/mock/24-1_오소_중간_예상문제.pdf', 0, 'application/pdf', 0, '2026-05-25 09:00:00'),
    (4, 1, '24-1_오소_기말_예상문제.pdf', 'mock-24-1_오소_기말_예상문제.pdf', 'archive/mock/24-1_오소_기말_예상문제.pdf', '/uploads/archive/mock/24-1_오소_기말_예상문제.pdf', 0, 'application/pdf', 0, '2026-05-25 09:00:00'),
    (5, 3, '23-1_오소_중간.pdf', 'mock-23-1_오소_중간.pdf', 'archive/mock/23-1_오소_중간.pdf', '/uploads/archive/mock/23-1_오소_중간.pdf', 0, 'application/pdf', 0, '2025-05-05 09:00:00'),
    (6, 3, '23-1_오소_중간_예상문제.pdf', 'mock-23-1_오소_중간_예상문제.pdf', 'archive/mock/23-1_오소_중간_예상문제.pdf', '/uploads/archive/mock/23-1_오소_중간_예상문제.pdf', 0, 'application/pdf', 0, '2025-05-05 09:00:00'),
    (7, 4, 'ds-midterm-2025.pdf', 'mock-ds-midterm-2025.pdf', 'archive/mock/ds-midterm-2025.pdf', '/uploads/archive/mock/ds-midterm-2025.pdf', 0, 'application/pdf', 0, '2026-05-20 09:00:00'),
    (8, 5, 'ds-final-2024.pdf', 'mock-ds-final-2024.pdf', 'archive/mock/ds-final-2024.pdf', '/uploads/archive/mock/ds-final-2024.pdf', 0, 'application/pdf', 0, '2026-05-15 09:00:00'),
    (9, 6, 'ds-quiz-assignment.pdf', 'mock-ds-quiz-assignment.pdf', 'archive/mock/ds-quiz-assignment.pdf', '/uploads/archive/mock/ds-quiz-assignment.pdf', 0, 'application/pdf', 0, '2026-05-10 09:00:00'),
    (10, 7, 'db-midterm-2025.pdf', 'mock-db-midterm-2025.pdf', 'archive/mock/db-midterm-2025.pdf', '/uploads/archive/mock/db-midterm-2025.pdf', 0, 'application/pdf', 0, '2026-05-12 09:00:00'),
    (11, 8, 'db-final-2025.pdf', 'mock-db-final-2025.pdf', 'archive/mock/db-final-2025.pdf', '/uploads/archive/mock/db-final-2025.pdf', 0, 'application/pdf', 0, '2026-05-08 09:00:00'),
    (12, 9, 'db-midterm-lee-2025.pdf', 'mock-db-midterm-lee-2025.pdf', 'archive/mock/db-midterm-lee-2025.pdf', '/uploads/archive/mock/db-midterm-lee-2025.pdf', 0, 'application/pdf', 0, '2026-05-03 09:00:00'),
    (13, 10, 'db-final-lee-2024.pdf', 'mock-db-final-lee-2024.pdf', 'archive/mock/db-final-lee-2024.pdf', '/uploads/archive/mock/db-final-lee-2024.pdf', 0, 'application/pdf', 0, '2026-04-28 09:00:00'),
    (14, 11, 'network-final-kim.pdf', 'mock-network-final-kim.pdf', 'archive/mock/network-final-kim.pdf', '/uploads/archive/mock/network-final-kim.pdf', 0, 'application/pdf', 0, '2026-04-20 09:00:00'),
    (15, 12, 'statistics-midterms.zip', 'mock-statistics-midterms.zip', 'archive/mock/statistics-midterms.zip', '/uploads/archive/mock/statistics-midterms.zip', 0, 'application/zip', 0, '2026-04-18 09:00:00'),
    (16, 13, 'logic-final-2024.pdf', 'mock-logic-final-2024.pdf', 'archive/mock/logic-final-2024.pdf', '/uploads/archive/mock/logic-final-2024.pdf', 0, 'application/pdf', 0, '2026-04-04 09:00:00');

COMMIT;
