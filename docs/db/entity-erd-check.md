# Entity ERD Check

D.COM 인트라넷 개편 프로젝트의 현재 Spring Boot Entity와 MVP ERD v2.0, DB 결정사항을 비교한 문서이다.

이 문서는 DB 작업 브랜치에서 Entity와 ERD를 비교하고, MariaDB 검증 과정에서 반영한 매핑 변경을 정리한 문서이다. 실제 사용자 데이터, 레거시 dump, 파일 바이너리, DB 접속 정보는 포함하지 않는다.

## 1. 분석 기준

| 기준 | 파일 |
|---|---|
| 신규 MVP ERD | `docs/db/mvp-erd-v2.0.md` |
| 레거시 스키마 요약 | `docs/db/legacy-schema.md` |
| DB/마이그레이션 결정사항 | `docs/db/db-decisions.md` |
| 백엔드 Entity 기준 | `src/main/java/com/dcom/intranet/**/domain` |

Git 기준:

- 작업 브랜치: `feature/database`
- PR 대상 브랜치: `develop`
- 2026-07-04 기준 `origin/develop` 최신 변경사항을 `feature/database`에 병합했다.
- 병합 중 `README.md`, `Notice`, `MyPageControllerTest` 충돌을 해결했다.

## 2. 현재 Entity 분석 요약

| Domain | Entity | Table | PK | FK | Enum | 주요 nullable/제약 |
|---|---|---|---|---|---|---|
| Auth | `User` | `users` | `id` | 없음 | `role`, `status` 모두 `EnumType.STRING` | `loginId`, `password`, `name`, `studentId`, `email`, `phoneNumber`, `role`, `status`, `createdAt` not null. `loginId`, `studentId`, `email` unique |
| Auth | `UserRole` | enum | - | - | `USER`, `ADMIN` | ERD와 일치 |
| Auth | `UserStatus` | enum | - | - | `PENDING`, `APPROVED`, `WITHDRAWN` | ERD와 일치 |
| Auth | `EmailVerification` | `email_verifications` | `id` | 없음 | 없음 | `email`, `verification_code`, `expires_at`, `verified`, `used`, `created_at` not null. `login_id`, `email_change_token` nullable |
| Auth | `RefreshToken` | `refresh_tokens` | `id` | 없음 | 없음 | `token`, `loginId`, `expiresAt`, `createdAt` not null. `token` unique |
| Archive | `Archive` | `archives` | `archive_id` | 없음 | 없음 | `subject_name`, `professor_name` not null. `UNIQUE(subject_name, professor_name)` 적용 |
| Archive | `ArchiveRecord` | `archive_records` | `record_id` | `archive_id`, `author_id` | `semester`, `exam_type` 모두 `EnumType.STRING` + `varchar(20)` | `archive_id`, `author_id` not null. `examYear`, `semester`, `examType`, `content`, `updatedAt` nullable |
| Archive | `ArchiveFile` | `archive_files` | `archive_file_id` | `record_id` | 없음 | `record_id`, `originalFileName`, `storedFileName`, `objectKey`, `downloadCount` not null. `fileUrl`, `fileSize`, `contentType`, `createdAt` nullable |
| Archive | `Semester` | enum | - | - | `FIRST`, `SECOND`, `SUMMER`, `WINTER` | 값 집합은 ERD와 일치 |
| Archive | `ExamType` | enum | - | - | `MIDTERM`, `FINAL`, `QUIZ`, `ASSIGNMENT`, `ETC` | 값 집합은 ERD와 일치 |
| Info | `InfoPost` | `info_posts` | `post_id` | `author_id` | 없음 | `title`, `content`, `views`, `author_id`, `createdAt` not null. `updatedAt` nullable |
| Info | `InfoPostFile` | `info_post_files` | `file_id` | `post_id` | 없음 | `originalFileName`, `storedFileName`, `objectKey`, `fileUrl`, `fileSize`, `post_id` not null. `contentType` nullable, length 100 |
| Info | `InfoComment` | `info_comments` | `comment_id` | `post_id`, `author_id` | 없음 | `content`, `post_id`, `author_id`, `createdAt` not null. `updatedAt` nullable |
| Notice | `Notice` | `notices` | `noticeId` | 없음 | 없음 | `title`, `content`, `createdAt`, `updatedAt` not null. `authorId` nullable scalar, User FK 아님 |
| Notice | `NoticeFile` | `notice_files` | `notice_file_id` | `notice_id` | 없음 | `originalFileName`, `fileUrl` not null. `fileUrl` length 500 |
| Photo | `PhotoPost` | `photo_posts` | `albumId` | 없음 | 없음 | `eventName`, `activityDate` not null. `description` nullable. 이미지 URL은 `photo_post_images` ElementCollection |
| Photo | `PhotoComment` | `photo_comments` | `commentId` | `album_id`, `author_id` | 없음 | `content`, `album_id`, `author_id`, `createdAt` not null. `updatedAt` nullable |

## 3. ERD와 Entity가 일치하는 항목

| 항목 | 확인 결과 |
|---|---|
| `users.role` enum | `UserRole.USER`, `UserRole.ADMIN`이며 `EnumType.STRING` 사용 |
| `users.status` enum 저장 방식 | `EnumType.STRING` 사용 |
| `users.status` enum 값 | `PENDING`, `APPROVED`, `WITHDRAWN`으로 ERD와 일치 |
| `email_verifications.verified` | 현재 필드명은 `verified`로 확인됨 |
| `archives` 테이블명 | `@Table(name = "archives")` |
| `archives` 복합 unique | `UNIQUE(subject_name, professor_name)` 적용됨 |
| `archive_records.semester` | `EnumType.STRING` 사용, enum 값은 ERD와 일치 |
| `archive_records.exam_type` | `EnumType.STRING` 사용, enum 값은 ERD와 일치 |
| `archive_records.exam_year` nullable | 레거시 족보 데이터 누락 가능성을 반영해 nullable 허용 |
| `archive_records.semester` nullable | 선택값 정책을 반영해 nullable 허용 |
| `archive_records.exam_type` nullable | 선택값 정책을 반영해 nullable 허용 |
| `archive_files.download_count` | `downloadCount` 존재, 기본값 0 |
| `archive_files.content_type` | `String` 타입, enum 아님 |
| `info_posts.views` | `views` 존재, 기본값 0 |
| `info_post_files.content_type` | `String` 타입, enum 아님, MIME 타입 기준 length 100 |
| `info_post_files.post_id` | 게시글 첨부파일을 게시글 소유로 보고 nullable false 반영 |

## 4. ERD와 Entity가 불일치하는 항목

| 항목 | 현재 Entity | ERD/결정사항 | 판단 |
|---|---|---|---|
| `archive_files.created_at` nullable | `@Column` 없음, 생성자에서 설정 | ERD상 생성 일시 | DB에서는 not null 권장, Entity 명시 추천 |
| `notices.author_id` 관계 | scalar `Long authorId`, FK 아님 | ERD는 User 작성 관계를 표현하나 현재 코드상 FK 아님 | 현재 코드 유지 또는 FK 전환 결정 필요 |
| `notice_files` 구조 | 별도 `NoticeFile` Entity | ERD상 `notice_files` 테이블 | develop 최신 구조와 일치 |
| `notice_files` 메타데이터 | `originalFileName`, `fileUrl`만 있음 | 파일 메타데이터 확장 가능 | `object_key`, `file_size`, `content_type` 추가 여부 미확정/주의 필요 |
| `photo_albums` | `photo_posts` | ERD 기준 테이블명 `photo_albums` | develop 최신 코드와 ERD 초안의 테이블명 불일치 |
| `photo_images` | `photo_post_images` ElementCollection | ERD 기준 별도 `photo_images` 테이블 | 이미지 메타데이터 없이 URL만 저장 |
| `photo_comments` | `photo_comments` | ERD 기준 테이블 존재 | 테이블명은 일치, 부모 FK는 코드상 `photo_posts(album_id)` 기준 |
| `legacy_migration_maps` | Entity 없음 | 선택 테이블 후보 | SQL 초안에는 포함, Java Entity는 추후 결정 |

## 5. 수정 추천 항목

아래 항목은 추후 백엔드 수정 후보이다. MariaDB 검증 과정에서 일부 매핑 항목은 이미 반영했다.

1. `ArchiveFile.createdAt`에 `nullable = false, updatable = false` 명시 검토
2. `NoticeFile`에 `object_key`, `file_size`, `content_type` 같은 파일 메타데이터를 추가할지 검토
3. Photo 도메인의 테이블명을 ERD 기준 `photo_albums/photo_images`로 맞출지, 현재 develop 코드 기준 `photo_posts/photo_post_images`를 유지할지 결정
4. `legacy_migration_maps`를 실제 운영 테이블로 사용할 경우 별도 Entity 또는 마이그레이션 전용 SQL/CSV 관리 방식 결정

## 6. 미확정/주의 필요 항목

| 항목 | 상태 |
|---|---|
| `file_url` 저장 방식 | 현재 코드에 있으므로 SQL 초안에는 포함. 장기적으로 `object_key` 기반 생성 검토 |
| 마이그레이션 족보 `author_id` | 기존 users 미이전. `migration admin` 계정 연결을 우선 가정 |
| `notice_files` 구조 | 최신 develop 기준 별도 `NoticeFile` Entity로 변경됨. 파일 메타데이터 확장 여부는 미확정 |
| `legacy_migration_maps` | SQL 초안에는 포함. 실제 운영 테이블로 둘지 CSV 추적으로 둘지는 미확정 |
| Photo Album | 최신 develop에는 `PhotoPost`, `PhotoComment`, `photo_post_images`가 구현됨. ERD 초안의 `photo_albums`, `photo_images`와 이름/구조 차이 주의 필요 |
| `RefreshToken.loginId` | 현재 FK 없이 문자열 저장. 장기적으로 `user_id` FK 구조 검토 가능 |
| `Notice.authorId` | 현재 FK 없이 scalar 저장. User FK 전환 여부 주의 필요 |
| 레거시 `files.download` 이전 | `archive_files.download_count`로 이전 가능하나 실제 이전 정책 확인 필요 |

## 7. SQL 초안 작성 시 반영할 기준

`docs/db/schema-draft.sql`에는 다음 기준을 반영한다.

- MariaDB 검토용 DDL 초안이며 운영 적용용이 아니다.
- `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`를 사용한다.
- PK는 `BIGINT AUTO_INCREMENT`로 작성한다.
- JPA enum은 DB ENUM이 아니라 `VARCHAR`로 작성한다.
- MIME 타입 컬럼은 `VARCHAR(100)`으로 작성한다.
- `archive_records.exam_year`, `semester`, `exam_type`은 NULL 허용으로 작성한다.
- `archives(subject_name, professor_name)` 복합 unique를 포함한다.
- 파일 바이너리나 실제 사용자 데이터는 포함하지 않는다.
- Photo 관련 SQL은 최신 develop 코드 기준 `photo_posts`, `photo_post_images`, `photo_comments`를 우선 반영하고, ERD 초안과의 이름 차이는 주석으로 남긴다.
- 미확정 항목은 SQL 주석 `-- REVIEW:` 또는 `-- TODO:`로 표시한다.
