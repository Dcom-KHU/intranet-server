# DB Decisions

D.COM 인트라넷 개편 프로젝트의 DB/ERD 관련 회의 결정사항을 정리한 문서이다.

## 1. 회의 정보

- 회의명: 백엔드 1차 회의
- 날짜: 2026-06-29
- 주제: 백엔드 개발 진행상황 공유 및 DB 마이그레이션 방향 정리

## 2. DB 회의 목적

이번 DB 회의의 목적은 기존 D.COM 인트라넷 DB 구조를 파악하고, 신규 Spring Boot + React + MariaDB 기반 인트라넷의 ERD와 비교하여 족보 아카이브 데이터 마이그레이션 방식을 정리하는 것이다.

현재 프로젝트는 기존 Iwinv PHP/Laravel 기반 인트라넷을 신규 클라우드 서버 환경으로 이전하고, React + Spring Boot + MariaDB 구조로 재구축하는 것을 목표로 한다.

DB 파트는 다음을 담당한다.

- 기존 데이터 구조 분석
- 신규 ERD 매핑
- 족보 마이그레이션 기준 정리
- 백엔드 전달용 DB 설계 문서 정리
- 추후 MariaDB DDL 초안 작성

## 3. 마이그레이션 범위 결정

이번 마이그레이션은 기존 DB 전체 이전이 아니라, 족보 데이터 중심의 선별 이전으로 진행한다.

### 이전 대상

- `boards` 중 `boardid = 'jokbo'`인 족보 게시글
- 족보 게시글 본문 텍스트
- 족보 게시글 본문에 포함된 `/download/{filename}` 파일 링크
- 족보 게시글에 연결된 댓글 중 의미 있는 본문 또는 파일 링크가 있는 댓글
- 족보 게시글/댓글에서 실제로 참조된 `files` row
- 실제 파일 바이너리

### 이전 제외 대상

- 기존 `users`
- `viewers`
- `groups`
- `groups_users`
- `loggers`
- `password_resets`
- `migrations`
- `dcomfiles`
- 족보 게시글/댓글에서 참조되지 않은 파일
- 자유게시판, 공지사항 등 족보 외 게시글

## 4. 기존 DB 구조에 대한 판단

기존 DB에서 족보 데이터는 하나의 정규화된 테이블에 저장되어 있지 않고, `boards`, `comments`, `files`에 분산되어 있다.

기존 구조는 다음과 같이 해석한다.

| 데이터 | 기존 저장 위치 | 신규 ERD 매핑 |
|---|---|---|
| 족보 게시글 | `boards` | `archive_records` |
| 과목명/교수명 | `boards.title` | `archives.subject_name`, `archives.professor_name` |
| 게시글 본문 | `boards.content` | `archive_records.content` |
| 댓글형 족보 | `comments.content` | `archive_records.content` |
| 파일 링크 | `boards.content`, `comments.content` 내부 `/download/{filename}` | `archive_files` |
| 파일 메타데이터 | `files` | `archive_files` |
| 실제 파일 | 기존 서버 파일 시스템 | 신규 서버 로컬 저장소 |

기존 `boards`와 `files` 사이에는 명시적인 FK가 없으므로, 게시글/댓글 본문 내부의 `/download/{filename}` 패턴을 추출한 뒤 `files.filename`과 매칭해야 한다.

## 5. 신규 족보 ERD 구조

신규 족보 구조는 다음 3단계 구조를 기준으로 한다.

```text
archives
└── archive_records
    └── archive_files
```

각 테이블의 의미는 다음과 같다.

| 테이블 | 의미 |
|---|---|
| `archives` | 과목명 + 교수명 조합 단위의 족보 아카이브 |
| `archive_records` | 개별 족보 기록 |
| `archive_files` | 족보 기록에 첨부된 파일 메타데이터 |

마이그레이션 단위는 파일이 아니라 `archive_records`이다.

파일 없이 텍스트로만 작성된 족보도 있을 수 있으므로, `archive_records`를 기본 단위로 생성하고, 파일이 있는 경우에만 `archive_files`를 연결한다.

## 6. 과목명/교수명 unique 정책

`archives`에서 `subject_name`과 `professor_name`은 각각 unique가 아니다.

다음 복합 unique 제약조건을 사용한다.

```sql
UNIQUE (subject_name, professor_name)
```

의미는 다음과 같다.

- 같은 과목명 + 같은 교수명 조합은 중복 생성하지 않는다.
- 같은 과목명에 다른 교수명은 허용한다.
- 같은 교수명이 다른 과목을 담당하는 것도 허용한다.

## 7. 기존 유저 데이터 처리

기존 `users` 테이블은 신규 시스템으로 마이그레이션하지 않는다.

이유는 다음과 같다.

- 기존 계정 중 불필요한 계정이 많다.
- 개인정보 및 비밀번호 해시 이슈가 있다.
- 신규 시스템에서는 새로운 회원가입/승인 방식으로 운영한다.
- 기존 `boards.userid`, `comments.userid`, `files.userid`를 신규 `users.id`와 직접 매핑할 수 없다.

따라서 마이그레이션된 족보 데이터의 작성자는 다음 방식 중 하나로 처리한다.

| 방식 | 설명 | 판단 |
|---|---|---|
| `author_id = NULL` | 작성자 없는 데이터로 저장 | 데이터 의미상 안전하지만 화면 표시가 어려움 |
| `migration admin` 계정 연결 | 기존 족보를 관리자 계정이 올린 것으로 처리 | 현재 백엔드 구현과 충돌이 적어 우선 검토 |
| 기존 유저 일부 매칭 | 기존 유저와 신규 유저를 수동 매칭 | 복잡하므로 비추천 |

현재는 `migration admin` 계정으로 연결하는 방식을 우선 검토한다.

## 8. legacy 정보 관리 방식

운영 테이블에 `legacy_board_id`, `legacy_file_id` 같은 컬럼을 직접 넣지 않는다.

대신 별도 `legacy_migration_maps` 테이블 또는 CSV를 통해 기존 데이터와 신규 데이터의 매핑을 관리한다.

현재 ERD에서는 다음 컬럼을 가진 `legacy_migration_maps`를 선택적으로 둔다.

| 컬럼 | 의미 |
|---|---|
| `id` | PK |
| `archive_record_id` | 신규 `archive_records.record_id` |
| `archive_file_id` | 신규 `archive_files.archive_file_id` |
| `legacy_board_id` | 기존 `boards.id` |
| `legacy_comment_id` | 기존 `comments.id` |
| `legacy_file_id` | 기존 `files.id` |
| `legacy_filename` | 기존 `files.filename` |
| `legacy_original_filename` | 기존 `files.original_filename` |
| `created_at` | 매핑 생성 일시 |

다음 컬럼은 현재 범위에서는 제외한다.

| 제외 컬럼 | 제외 이유 |
|---|---|
| `legacy_uploader_id` | 기존 유저 데이터를 이전하지 않으므로 운영 DB에 필요하지 않음 |
| `migration_source` | 출처가 단일 레거시 DB로 고정되어 있음 |
| `legacy_table` | 족보 마이그레이션 대상 테이블이 `boards`, `comments`, `files`로 제한되어 있음 |
| `target_table`, `target_id` | 범용 구조보다 족보 전용 FK가 더 명확함 |

## 9. 파일 저장 정책

MVP 단계에서는 서버 로컬 저장 방식을 우선 사용한다.

S3/Object Storage는 장기 운영 또는 파일 규모 증가 시 검토한다.

### 파일 관련 DB 저장 원칙

DB에는 실제 파일 바이너리를 저장하지 않는다.  
DB에는 파일 메타데이터와 내부 저장 key/path만 저장한다.

파일 관련 주요 컬럼은 다음과 같다.

| 컬럼 | 의미 |
|---|---|
| `original_file_name` | 사용자에게 보여줄 원본 파일명 |
| `stored_file_name` | 서버에 저장된 파일명 |
| `object_key` | 서버 내부 저장 key/path |
| `file_url` | API 응답용 URL 또는 접근 경로 |
| `file_size` | 파일 크기 |
| `content_type` | MIME 타입 |
| `download_count` | 다운로드 수 |

`file_url`을 DB에 직접 저장할지, `object_key` 기반으로 다운로드 API에서 생성할지는 추가 논의가 필요하다.

## 10. MIME 타입 처리

`content_type`은 MIME 타입을 의미한다.

예시는 다음과 같다.

```text
application/pdf
image/png
image/jpeg
application/zip
application/x-zip-compressed
```

MIME 타입은 파일 종류와 클라이언트 환경에 따라 다양한 값이 들어올 수 있으므로 enum으로 제한하지 않고 `VARCHAR`로 저장한다.

## 11. Enum 저장 방식

JPA enum은 숫자 저장보다 문자열 저장 방식이 유지보수에 유리하므로 `EnumType.STRING` 사용을 권장한다.

Enum으로 관리할 컬럼은 다음과 같다.

| 컬럼 | 값 |
|---|---|
| `users.role` | `USER`, `ADMIN` |
| `users.status` | `PENDING`, `APPROVED`, `WITHDRAWN` |
| `archive_records.semester` | `FIRST`, `SECOND`, `SUMMER`, `WINTER` |
| `archive_records.exam_type` | `MIDTERM`, `FINAL`, `QUIZ`, `ASSIGNMENT`, `ETC` |

다만 `archive_records.semester`와 `archive_records.exam_type`은 선택값으로 결정되었으므로 NULL 허용을 검토한다.

또한 기존 족보 데이터에는 시험 연도, 학기, 시험 유형이 구조화되어 있지 않으므로 `exam_year`도 NULL 허용을 권장한다.

## 12. 삭제 정책

족보 데이터는 서비스 핵심 자료이므로 유저 삭제와 함께 삭제되면 안 된다.

추천 정책은 다음과 같다.

| 관계 | 추천 정책 | 설명 |
|---|---|---|
| `archives` → `archive_records` | CASCADE | 아카이브 삭제 시 하위 record 삭제 |
| `archive_records` → `archive_files` | CASCADE | record 삭제 시 파일 메타데이터 삭제 |
| `users` → `archive_records` | SET NULL 또는 soft delete | 유저 탈퇴 시 족보 데이터 유지 |
| `users` → `info_posts` | soft delete 우선 검토 | 작성자 삭제 시 게시글 보존 여부 결정 필요 |
| `users` → `info_comments` | soft delete 우선 검토 | 댓글 보존 여부 결정 필요 |

JPA에서는 `User`에서 `ArchiveRecord` 방향으로 `CascadeType.REMOVE`를 걸지 않는 것이 안전하다.

## 13. 텍스트 본문 처리

기존 `boards.content`, `comments.content`에는 HTML 태그, 파일 다운로드 링크, 일반 텍스트가 섞여 있을 수 있다.

마이그레이션 시 처리 방향은 다음과 같다.

1. `/download/{filename}` 링크를 추출한다.
2. 추출된 파일은 `files.filename`과 매칭한다.
3. 파일 메타데이터는 `archive_files`로 이전한다.
4. 본문에는 HTML 태그를 제거한 텍스트를 저장한다.
5. 필요하다면 줄바꿈 정도만 보존한다.

## 14. 다운로드 API 정책

파일은 프론트에서 실제 파일 경로에 직접 접근하는 방식이 아니라, 백엔드 다운로드 API를 통해 처리하는 방향을 검토한다.

추천 흐름은 다음과 같다.

1. file id로 `archive_files` 조회
2. 로그인/권한 확인
3. `object_key` 또는 저장 경로 기준으로 실제 파일 조회
4. `original_file_name`으로 다운로드 응답
5. `content_type`을 HTTP `Content-Type`으로 설정
6. `download_count` 증가

이를 통해 다음을 처리할 수 있다.

- 권한 체크
- 다운로드 수 집계
- 원본 파일명 유지
- MIME 타입 지정
- 저장소 변경 대응

## 15. 마이그레이션 방식

마이그레이션은 수작업보다 스크립트 기반으로 진행하는 것이 적절하다.

우선 검토 방식은 Python 스크립트이다.

Python 스크립트를 사용하는 이유는 다음과 같다.

- 게시글/댓글 본문에서 `/download/{filename}` 패턴 추출이 쉽다.
- `files.filename`과 매칭하기 쉽다.
- 대상 파일 목록 CSV 생성이 쉽다.
- 실제 파일 존재 여부 검증이 쉽다.
- 신규 DB insert용 CSV 또는 SQL 생성이 가능하다.

## 16. 마이그레이션 검증 기준

마이그레이션 후 다음 항목을 검증한다.

| 검증 항목 | 설명 |
|---|---|
| 족보 게시글 수 | 기존 `boards.boardid='jokbo'` 개수와 신규 record 개수 비교 |
| 댓글 record 수 | 이전 대상 댓글 개수와 신규 record 개수 비교 |
| 파일 매칭 수 | 본문/댓글에서 추출한 파일 수와 신규 file row 개수 비교 |
| 실제 파일 존재 여부 | 신규 저장소에 파일이 실제로 존재하는지 확인 |
| 다운로드 테스트 | API를 통해 다운로드 가능한지 확인 |
| 원본 파일명 유지 | 다운로드 시 `original_file_name`이 유지되는지 확인 |
| 텍스트 족보 보존 | 파일 없는 텍스트형 족보가 누락되지 않았는지 확인 |

## 17. 백엔드 공통 API 응답 관련 결정 필요

백엔드 1차 회의에서 API 응답 구조와 필드명 일관성도 중요하게 논의되었다.

결정 또는 통일이 필요한 항목은 다음과 같다.

- Swagger에는 실제 response body 예시까지 작성한다.
- API별 response body 형태를 일관되게 유지한다.
- 불필요한 데이터를 응답에 포함하지 않는다.
- 같은 의미의 필드명은 기능별로 다르게 쓰지 않는다.
- 작성일 필드는 `date`, `createdAt`, `createdDate` 중 하나로 통일한다.
- 작성자는 `author`로 통일하는 방향을 검토한다.
- 파일 목록은 `files` 또는 `attachments` 중 하나로 통일한다.
- 회원 권한은 `role`로 통일한다.
- 회원 승인 상태는 `status` 또는 `approvalStatus` 중 하나로 통일한다.

## 18. 추후 결정이 필요한 항목

다음 항목은 회의 또는 백엔드 팀 확인 후 확정한다.

1. `archive_records.exam_year` NULL 허용 여부
2. `archive_records.semester` NULL 허용 여부
3. `archive_records.exam_type` NULL 허용 여부
4. 마이그레이션된 족보의 `author_id`를 migration admin 계정으로 고정할지 여부
5. `file_url`을 DB에 저장할지, `object_key` 기반으로 API에서 생성할지 여부
6. `notice_files`를 `ElementCollection`으로 유지할지 별도 Entity로 분리할지 여부
7. `legacy_migration_maps`를 실제 DB 테이블로 둘지 CSV로만 관리할지 여부
8. MariaDB 전환 시점
9. `spring.jpa.hibernate.ddl-auto` 설정 변경 방식
10. 실제 파일 바이너리 백업 및 신규 저장소 이전 방식
11. 마이그레이션 스크립트를 Python으로 작성할지 Java 배치로 작성할지 여부

## 19. 다음 액션 아이템

| 담당 | 할 일 | 산출물 |
|---|---|---|
| DB 파트 | 기존 족보 게시글 목록 정리 | `boards.boardid='jokbo'` 결과 |
| DB 파트 | 족보 게시글별 댓글 확인 | 댓글형 족보 후보 목록 |
| DB 파트 | 본문/댓글에서 `/download/{filename}` 추출 | 파일 매핑표 |
| DB 파트 | `files.filename`과 매칭 | 족보 파일 후보 목록 |
| DB 파트 | 신규 ERD와 Entity 비교 | ERD/Entity 불일치 목록 |
| 백엔드 | JPA Entity 관계 검토 | Entity 수정 필요 목록 |
| 백엔드 | 파일 저장/다운로드 방식 결정 | 파일 API 정책 |
| 인프라 | 기존 `storage/app` 실제 파일 백업 | 파일 백업본 |
| 전체 | 유저 데이터 미이전 확정 | 회의 결정사항 |
| 전체 | 마이그레이션 검증 기준 확정 | 검증 체크리스트 |
