# Legacy DB Schema

D.COM 기존 PHP/Laravel 기반 인트라넷의 레거시 DB 스키마 요약 문서이다.

> 이 문서는 마이그레이션 설계 참고용이며, 실제 사용자 데이터나 게시글 데이터 dump는 포함하지 않는다.

## 원본 스키마 개요

- 원본 DB: `dcomkhu`
- Dump 도구: phpMyAdmin
- DB 서버 버전: MariaDB 10.1.20
- PHP 버전: 5.6.40
- 문자셋: `utf8`
- 데이터 row dump: 포함하지 않음
- 신규 마이그레이션 대상: 전체 DB가 아니라 족보 게시판 데이터 일부

## 레거시 테이블 목록

| 테이블 | 마이그레이션 사용 여부 | 비고 |
|---|---:|---|
| `boards` | 일부 사용 | `boardid = 'jokbo'`인 족보 게시글만 대상 |
| `comments` | 일부 사용 | 족보 게시글에 연결된 댓글 중 의미 있는 본문/파일 링크만 대상 |
| `files` | 일부 사용 | 족보 게시글/댓글에서 참조된 파일만 대상 |
| `users` | 제외 | 신규 회원 체계 사용, 기존 유저 데이터 이전 안 함 |
| `viewers` | 제외 | 조회 기록 이전 안 함 |
| `groups` | 제외 | MVP 족보 마이그레이션과 무관 |
| `groups_users` | 제외 | MVP 족보 마이그레이션과 무관 |
| `loggers` | 제외 | 운영 로그성 데이터 |
| `password_resets` | 제외 | 인증 체계 변경으로 이전 불필요 |
| `migrations` | 제외 | Laravel 내부 마이그레이션 기록 |
| `dcomfiles` | 제외 | 기존 족보 이전 기준에서는 직접 사용하지 않음 |

## 마이그레이션 범위

레거시 DB 전체를 이전하지 않는다.

마이그레이션 대상은 다음으로 한정한다.

1. `boards.boardid = 'jokbo'`인 게시글
2. 위 족보 게시글에 연결된 `comments`
3. 게시글 본문 또는 댓글 본문에서 `/download/{filename}` 형태로 참조되는 `files`
4. 실제 파일 바이너리

기존 `users` 테이블은 마이그레이션 대상이 아니므로, 기존 `boards.userid`, `comments.userid`, `files.userid`는 신규 `users.id`와 FK로 연결하지 않는다.

마이그레이션된 족보 데이터의 작성자는 별도 `migration admin` 계정으로 연결하는 방식을 우선 검토한다.

---

## 테이블 구조

### `boards`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `boardid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `title` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `content` | `longtext COLLATE utf8_unicode_ci NOT NULL` |
| `anonymous` | `int(11) DEFAULT NULL` |
| `viewer` | `int(11) DEFAULT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `comments`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `boardid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `content` | `longtext COLLATE utf8_unicode_ci NOT NULL` |
| `frontcomment` | `int(11) NOT NULL` |
| `anonymous` | `int(11) NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `dcomfiles`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `filename` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `realname` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `original_filename` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `download` | `int(11) NOT NULL DEFAULT '0'` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `files`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `filename` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `mime` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `original_filename` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `type` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `download` | `int(11) NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `groups`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `boardid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `type` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `title` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `description` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `participation` | `int(11) NOT NULL` |
| `git` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `groups_users`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `groupid` | `int(11) NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `loggers`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `command` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `target` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `type` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `who` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `migrations`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `migration` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `batch` | `int(11) NOT NULL` |

### `password_resets`

| 컬럼 | 타입 / 제약 |
|---|---|
| `email` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `token` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |

### `users`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `email` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `password` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `realname` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `phone` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `admissionyear` | `int(11) NOT NULL` |
| `confirm` | `int(11) NOT NULL DEFAULT '0'` |
| `admin` | `int(11) NOT NULL DEFAULT '0'` |
| `logintime` | `timestamp NULL DEFAULT NULL` |
| `remember_token` | `varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

### `viewers`

| 컬럼 | 타입 / 제약 |
|---|---|
| `id` | `int(10) UNSIGNED NOT NULL` |
| `boardid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `userid` | `varchar(255) COLLATE utf8_unicode_ci NOT NULL` |
| `created_at` | `timestamp NULL DEFAULT NULL` |
| `updated_at` | `timestamp NULL DEFAULT NULL` |

---

## 족보 마이그레이션에 필요한 핵심 테이블

### 1. `boards`

족보 게시글의 원본이다.

주요 컬럼:

| 컬럼 | 사용 방식 |
|---|---|
| `id` | `legacy_migration_maps.legacy_board_id` |
| `boardid` | `jokbo` 필터링 기준 |
| `userid` | 신규 users와 연결하지 않음 |
| `title` | `과목명|교수명` 형태 파싱 |
| `content` | 족보 본문 및 `/download/{filename}` 링크 추출 |
| `created_at` | 신규 record 생성일 참고 가능 |
| `updated_at` | 신규 record 수정일 참고 가능 |

### 2. `comments`

족보 게시글에 달린 댓글 중 의미 있는 본문 또는 파일 링크가 있는 경우 신규 `archive_records`로 이전할 수 있다.

주요 컬럼:

| 컬럼 | 사용 방식 |
|---|---|
| `id` | `legacy_migration_maps.legacy_comment_id` |
| `boardid` | 기존 `boards.id`와 연결되는 값으로 확인 필요 |
| `userid` | 신규 users와 연결하지 않음 |
| `content` | 댓글 본문 및 `/download/{filename}` 링크 추출 |
| `created_at` | 신규 record 생성일 참고 가능 |
| `updated_at` | 신규 record 수정일 참고 가능 |

### 3. `files`

족보 게시글/댓글에서 참조된 파일의 메타데이터이다.

전체 `files`를 이전하지 않고, 족보 본문/댓글에서 참조된 파일만 이전한다.

주요 컬럼:

| 컬럼 | 사용 방식 |
|---|---|
| `id` | `legacy_migration_maps.legacy_file_id` |
| `userid` | 신규 users와 연결하지 않음 |
| `filename` | 서버 저장 파일명, `/download/{filename}` 매칭 기준 |
| `mime` | 신규 `archive_files.content_type` |
| `original_filename` | 신규 `archive_files.original_file_name` |
| `type` | 참고용 |
| `download` | 신규 `archive_files.download_count`로 이전 가능 |
| `created_at` | 신규 file 생성일 참고 가능 |
| `updated_at` | 참고용 |

---

## 신규 ERD와의 매핑 초안

| Legacy | New ERD | 설명 |
|---|---|---|
| `boards.id` | `legacy_migration_maps.legacy_board_id` | 기존 족보 게시글 ID 추적 |
| `comments.id` | `legacy_migration_maps.legacy_comment_id` | 기존 댓글 기반 족보 record 추적 |
| `files.id` | `legacy_migration_maps.legacy_file_id` | 기존 파일 ID 추적 |
| `files.filename` | `legacy_migration_maps.legacy_filename` / `archive_files.stored_file_name` 또는 `object_key` | 기존 서버 저장 파일명 |
| `files.original_filename` | `legacy_migration_maps.legacy_original_filename` / `archive_files.original_file_name` | 원본 파일명 |
| `files.mime` | `archive_files.content_type` | MIME 타입 |
| `files.download` | `archive_files.download_count` | 기존 다운로드 수 이전 여부 결정 필요 |
| `boards.title` | `archives.subject_name`, `archives.professor_name` | `과목명|교수명` 기준 파싱 |
| `boards.content` | `archive_records.content` | HTML 제거 또는 텍스트 정제 필요 |
| `comments.content` | `archive_records.content` | 의미 있는 댓글만 이전 |

---

## 제외 테이블 사유

| 테이블 | 제외 사유 |
|---|---|
| `users` | 신규 회원 체계와 충돌 가능, 개인정보 포함 가능 |
| `viewers` | 조회 이력은 MVP 마이그레이션 대상 아님 |
| `groups` | 족보 이전과 직접 관련 없음 |
| `groups_users` | 유저/그룹 관계 이전 대상 아님 |
| `loggers` | 운영 로그성 데이터 |
| `password_resets` | 기존 인증 체계 데이터로 신규 서비스에서 사용하지 않음 |
| `migrations` | Laravel 내부 스키마 관리용 |
| `dcomfiles` | 현재 족보 게시글/댓글 파일 매핑 기준에서는 직접 사용하지 않음 |

---

## 회의에서 결정할 항목

1. `files.download`를 신규 `archive_files.download_count`로 이전할지 여부
2. `boards.content`, `comments.content`의 HTML 태그 제거 기준
3. 댓글을 별도 `archive_records`로 만들 기준
4. 파일이 없는 텍스트형 족보도 이전할지 여부
5. 파일은 있지만 본문이 없는 경우 `archive_records.content`를 어떻게 둘지
6. 마이그레이션된 족보의 `author_id`에 사용할 `migration admin` 계정
7. `legacy_migration_maps`를 실제 DB 테이블로 둘지, CSV로만 관리할지
8. 실제 파일 바이너리 복사 경로와 `object_key` 생성 규칙

