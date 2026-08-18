-- Apply this while connected to the target DB, e.g. dcom_intranet_dev or dcom_intranet.
-- Backfill requires the legacy DB `dcomkhu` and `legacy_migration_maps` to exist
-- on the same MariaDB instance.

ALTER TABLE archive_records
    ADD COLUMN IF NOT EXISTS legacy_author_student_number VARCHAR(255) NULL AFTER content;

ALTER TABLE archive_records
    ADD COLUMN IF NOT EXISTS legacy_author_name VARCHAR(255) NULL AFTER legacy_author_student_number;

ALTER TABLE archive_records
    ADD COLUMN IF NOT EXISTS legacy_anonymous TINYINT(1) NULL AFTER legacy_author_name;

UPDATE archive_records ar
JOIN (
    SELECT DISTINCT
        m.archive_record_id,
        b.userid AS legacy_author_student_number,
        u.realname AS legacy_author_name,
        COALESCE(b.anonymous, 0) AS legacy_anonymous
    FROM legacy_migration_maps m
    JOIN dcomkhu.boards b
        ON b.id = m.legacy_board_id
    LEFT JOIN dcomkhu.users u
        ON u.userid = b.userid
    WHERE m.archive_record_id IS NOT NULL
      AND m.archive_file_id IS NULL
      AND m.legacy_comment_id IS NULL
) legacy_board
    ON legacy_board.archive_record_id = ar.record_id
SET ar.legacy_author_student_number = CASE
        WHEN legacy_board.legacy_anonymous = 1 THEN NULL
        ELSE NULLIF(TRIM(legacy_board.legacy_author_student_number), '')
    END,
    ar.legacy_author_name = CASE
        WHEN legacy_board.legacy_anonymous = 1 THEN NULL
        ELSE NULLIF(TRIM(legacy_board.legacy_author_name), '')
    END,
    ar.legacy_anonymous = legacy_board.legacy_anonymous;

UPDATE archive_records ar
JOIN (
    SELECT DISTINCT
        m.archive_record_id,
        c.userid AS legacy_author_student_number,
        u.realname AS legacy_author_name,
        COALESCE(c.anonymous, 0) AS legacy_anonymous
    FROM legacy_migration_maps m
    JOIN dcomkhu.comments c
        ON c.id = m.legacy_comment_id
    LEFT JOIN dcomkhu.users u
        ON u.userid = c.userid
    WHERE m.archive_record_id IS NOT NULL
      AND m.archive_file_id IS NULL
      AND m.legacy_comment_id IS NOT NULL
) legacy_comment
    ON legacy_comment.archive_record_id = ar.record_id
SET ar.legacy_author_student_number = CASE
        WHEN legacy_comment.legacy_anonymous = 1 THEN NULL
        ELSE NULLIF(TRIM(legacy_comment.legacy_author_student_number), '')
    END,
    ar.legacy_author_name = CASE
        WHEN legacy_comment.legacy_anonymous = 1 THEN NULL
        ELSE NULLIF(TRIM(legacy_comment.legacy_author_name), '')
    END,
    ar.legacy_anonymous = legacy_comment.legacy_anonymous;

SELECT
    COUNT(*) AS archive_record_count,
    SUM(legacy_anonymous IS NOT NULL) AS legacy_author_backfilled_count,
    SUM(legacy_anonymous = 1) AS legacy_anonymous_count,
    SUM(legacy_anonymous = 0 AND legacy_author_name IS NULL) AS legacy_author_name_missing_count
FROM archive_records;
