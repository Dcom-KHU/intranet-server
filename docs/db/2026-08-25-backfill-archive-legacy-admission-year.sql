-- Apply this while connected to the target DB, e.g. dcom_intranet_dev or dcom_intranet.
-- Backfill requires the legacy DB `legacy_dcomkhu` and `legacy_migration_maps` to exist
-- on the same MariaDB instance.
--
-- Legacy D.COM users.userid is a login ID, not a student number.
-- Store only users.admissionyear as a two-digit display value.

ALTER TABLE archive_records
    ADD COLUMN IF NOT EXISTS legacy_author_student_number VARCHAR(255) NULL AFTER content;

UPDATE archive_records ar
JOIN (
    SELECT DISTINCT
        m.archive_record_id,
        CASE
            WHEN u.admissionyear IS NULL OR u.admissionyear <= 0 THEN NULL
            ELSE LPAD(CAST(u.admissionyear AS CHAR), 2, '0')
        END AS legacy_author_student_number,
        COALESCE(b.anonymous, 0) AS legacy_anonymous
    FROM legacy_migration_maps m
    JOIN legacy_dcomkhu.boards b
        ON b.id = m.legacy_board_id
    LEFT JOIN legacy_dcomkhu.users u
        ON u.userid = b.userid
    WHERE m.archive_record_id IS NOT NULL
      AND m.archive_file_id IS NULL
      AND m.legacy_comment_id IS NULL
) legacy_board
    ON legacy_board.archive_record_id = ar.record_id
SET ar.legacy_author_student_number = CASE
    WHEN legacy_board.legacy_anonymous = 1 THEN NULL
    ELSE legacy_board.legacy_author_student_number
END;

UPDATE archive_records ar
JOIN (
    SELECT DISTINCT
        m.archive_record_id,
        CASE
            WHEN u.admissionyear IS NULL OR u.admissionyear <= 0 THEN NULL
            ELSE LPAD(CAST(u.admissionyear AS CHAR), 2, '0')
        END AS legacy_author_student_number,
        COALESCE(c.anonymous, 0) AS legacy_anonymous
    FROM legacy_migration_maps m
    JOIN legacy_dcomkhu.comments c
        ON c.id = m.legacy_comment_id
    LEFT JOIN legacy_dcomkhu.users u
        ON u.userid = c.userid
    WHERE m.archive_record_id IS NOT NULL
      AND m.archive_file_id IS NULL
      AND m.legacy_comment_id IS NOT NULL
) legacy_comment
    ON legacy_comment.archive_record_id = ar.record_id
SET ar.legacy_author_student_number = CASE
    WHEN legacy_comment.legacy_anonymous = 1 THEN NULL
    ELSE legacy_comment.legacy_author_student_number
END;

SELECT
    COUNT(*) AS legacy_author_record_count,
    SUM(legacy_author_student_number IS NOT NULL) AS admission_year_backfilled_count,
    SUM(legacy_anonymous = 1 AND legacy_author_student_number IS NOT NULL) AS anonymous_exposure_count,
    SUM(legacy_author_student_number REGEXP '^[0-9]{2}$') AS two_digit_count
FROM archive_records
WHERE legacy_anonymous IS NOT NULL;
