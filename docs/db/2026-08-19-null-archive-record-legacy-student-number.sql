-- Legacy D.COM users.userid is a login ID, not a student number.
-- Keep legacy author names, but do not expose legacy userid through author.studentNumber.

UPDATE archive_records
SET legacy_author_student_number = NULL
WHERE legacy_anonymous IS NOT NULL;

SELECT
    COUNT(*) AS legacy_author_record_count,
    SUM(legacy_author_student_number IS NOT NULL) AS remaining_legacy_student_number_count
FROM archive_records
WHERE legacy_anonymous IS NOT NULL;
