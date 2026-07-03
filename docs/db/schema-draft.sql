-- D.COM 인트라넷 MariaDB 스키마 초안
-- 목적: DB 설계 검토 및 PR 리뷰용 초안이다.
-- 이 파일은 운영 적용용 마이그레이션 스크립트가 아니다.
-- 실제 사용자 데이터, 레거시 dump, DB 접속 정보, 파일 바이너리는 저장하지 않는다.

SET NAMES utf8mb4;

-- REVIEW: DROP 문은 로컬 임시 DB에서만 추가한다. 이 초안에서는 의도적으로 제외한다.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT '회원 권한: USER, ADMIN',
    status VARCHAR(20) NOT NULL COMMENT '회원 상태: PENDING, APPROVED, WITHDRAWN',
    created_at DATETIME NOT NULL,
    last_login_at DATETIME NULL,
    withdrawn_at DATETIME NULL,
    temp_password VARCHAR(255) NULL,
    temp_password_expires_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_login_id (login_id),
    UNIQUE KEY uk_users_student_id (student_id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NULL,
    email VARCHAR(100) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    email_change_token VARCHAR(100) NULL,
    expires_at DATETIME NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verifications_email_change_token (email_change_token),
    KEY idx_email_verifications_email (email),
    KEY idx_email_verifications_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(512) NOT NULL,
    login_id VARCHAR(100) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    KEY idx_refresh_tokens_login_id (login_id)
    -- REVIEW: 현재 Entity는 users.id FK가 아니라 login_id 문자열을 저장한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE archives (
    archive_id BIGINT NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    professor_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    last_modified_at DATETIME NOT NULL,
    PRIMARY KEY (archive_id),
    UNIQUE KEY uk_archive_subject_professor (subject_name, professor_name),
    KEY idx_archives_subject_name (subject_name),
    KEY idx_archives_professor_name (professor_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE archive_records (
    record_id BIGINT NOT NULL AUTO_INCREMENT,
    archive_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    exam_year INT NULL,
    semester VARCHAR(20) NULL COMMENT '학기: FIRST, SECOND, SUMMER, WINTER',
    exam_type VARCHAR(20) NULL COMMENT '시험 유형: MIDTERM, FINAL, QUIZ, ASSIGNMENT, ETC',
    content TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (record_id),
    KEY idx_archive_records_archive_id (archive_id),
    KEY idx_archive_records_author_id (author_id),
    CONSTRAINT fk_archive_records_archive
        FOREIGN KEY (archive_id) REFERENCES archives (archive_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_archive_records_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON DELETE RESTRICT
    -- REVIEW: 레거시 족보 record는 migration admin 계정 연결을 우선 가정한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE archive_files (
    archive_file_id BIGINT NOT NULL AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) NULL,
    file_size BIGINT NULL,
    content_type VARCHAR(100) NULL,
    download_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (archive_file_id),
    KEY idx_archive_files_record_id (record_id),
    CONSTRAINT fk_archive_files_record
        FOREIGN KEY (record_id) REFERENCES archive_records (record_id)
        ON DELETE CASCADE
    -- REVIEW: 현재 코드가 file_url을 저장하므로 유지한다. 장기적으로는 object_key 기반 URL 생성을 검토할 수 있다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE info_posts (
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    views INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (post_id),
    KEY idx_info_posts_author_id (author_id),
    KEY idx_info_posts_created_at (created_at),
    KEY idx_info_posts_views (views),
    CONSTRAINT fk_info_posts_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE info_post_files (
    file_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NULL,
    PRIMARY KEY (file_id),
    KEY idx_info_post_files_post_id (post_id),
    CONSTRAINT fk_info_post_files_post
        FOREIGN KEY (post_id) REFERENCES info_posts (post_id)
        ON DELETE CASCADE
    -- REVIEW: 현재 Entity에는 post_id nullable=false가 명시되어 있지 않지만, SQL 초안에서는 첨부파일을 게시글 소유로 본다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE info_comments (
    comment_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (comment_id),
    KEY idx_info_comments_post_id (post_id),
    KEY idx_info_comments_author_id (author_id),
    CONSTRAINT fk_info_comments_post
        FOREIGN KEY (post_id) REFERENCES info_posts (post_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_info_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notices (
    notice_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    author_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (notice_id),
    KEY idx_notices_author_id (author_id),
    KEY idx_notices_created_at (created_at)
    -- REVIEW: 현재 Notice Entity는 author_id를 User FK가 아니라 Long 값으로 저장한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notice_files (
    notice_file_id BIGINT NOT NULL AUTO_INCREMENT,
    notice_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (notice_file_id),
    KEY idx_notice_files_notice_id (notice_id),
    CONSTRAINT fk_notice_files_notice
        FOREIGN KEY (notice_id) REFERENCES notices (notice_id)
        ON DELETE CASCADE
    -- REVIEW: 최신 develop 기준 NoticeFile은 별도 Entity다.
    -- TODO: 공지 파일에 object_key, file_size, content_type, 생성/수정 시각이 필요한지 검토한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- REVIEW: ERD 초안은 photo_albums/photo_images를 사용하지만, 최신 develop 코드는 photo_posts/photo_post_images를 사용한다.
CREATE TABLE photo_posts (
    album_id BIGINT NOT NULL AUTO_INCREMENT,
    event_name VARCHAR(100) NOT NULL,
    activity_date DATE NOT NULL,
    description LONGTEXT NULL,
    PRIMARY KEY (album_id),
    KEY idx_photo_posts_activity_date (activity_date)
    -- REVIEW: 현재 PhotoPost Entity에는 작성자/admin_id와 생성/수정 시각 컬럼이 없다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE photo_post_images (
    album_id BIGINT NOT NULL,
    upload_order INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (album_id, upload_order),
    KEY idx_photo_post_images_album_id (album_id),
    CONSTRAINT fk_photo_post_images_album
        FOREIGN KEY (album_id) REFERENCES photo_posts (album_id)
        ON DELETE CASCADE
    -- REVIEW: 현재 develop 코드는 이미지 메타데이터 없이 URL만 ElementCollection으로 저장한다.
    -- TODO: object_key, file_size, content_type 등 파일 메타데이터가 필요하면 별도 PhotoImage Entity 전환을 검토한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE photo_comments (
    comment_id BIGINT NOT NULL AUTO_INCREMENT,
    album_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (comment_id),
    KEY idx_photo_comments_album_id (album_id),
    KEY idx_photo_comments_author_id (author_id),
    CONSTRAINT fk_photo_comments_album
        FOREIGN KEY (album_id) REFERENCES photo_posts (album_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_photo_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON DELETE RESTRICT
    -- REVIEW: 최신 develop 기준 PhotoComment Entity는 photo_posts를 부모로 참조한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE legacy_migration_maps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    archive_record_id BIGINT NULL,
    archive_file_id BIGINT NULL,
    legacy_board_id BIGINT NULL,
    legacy_comment_id BIGINT NULL,
    legacy_file_id BIGINT NULL,
    legacy_filename VARCHAR(255) NULL,
    legacy_original_filename VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_legacy_migration_maps_archive_record_id (archive_record_id),
    KEY idx_legacy_migration_maps_archive_file_id (archive_file_id),
    KEY idx_legacy_migration_maps_legacy_board_id (legacy_board_id),
    KEY idx_legacy_migration_maps_legacy_file_id (legacy_file_id),
    CONSTRAINT fk_legacy_migration_maps_archive_record
        FOREIGN KEY (archive_record_id) REFERENCES archive_records (record_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_legacy_migration_maps_archive_file
        FOREIGN KEY (archive_file_id) REFERENCES archive_files (archive_file_id)
        ON DELETE SET NULL
    -- REVIEW: 선택 매핑 테이블이다. CSV만으로 추적하기로 하면 이 테이블은 적용하지 않는다.
    -- 결정에 따라 제외한 컬럼: legacy_uploader_id, migration_source, legacy_table, target_table, target_id.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
