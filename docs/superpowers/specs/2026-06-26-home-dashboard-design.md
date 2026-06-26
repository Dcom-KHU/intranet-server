# Home Dashboard API Design

## Context

The Home part of the D.COM intranet MVP has two API candidates in the final API specification:

- `GET /api/home`: main dashboard lookup
- `GET /api/home/sidebar`: admin menu visibility lookup

Only `GET /api/home` will be implemented in this scope. The admin sidebar API will not be implemented because the frontend does not use it.

The frontend team will connect to this backend API through Swagger/OpenAPI documentation. The API response must therefore be explicit, stable, and close to the frontend mock data shape.

## Requirements

`GET /api/home` returns the data needed by the home dashboard.

The response must exclude these fields from the original API spec:

- `userId`
- `role`
- `mainMenu`
- `recentAnnouncements`

`recentAnnouncements` is replaced with `recentNotices` because the frontend domain name is changing from Announcement to Notice.

Each home section returns up to 5 items:

- `recentNotices`
- `recentArchives`
- `recentInfoPosts`
- `recentPhotoAlbums`

The current backend has no domain entities or repositories for notices, archives, info posts, or photo albums. For this first implementation, the service returns fixed data based on the provided frontend mock data.

## API Contract

### Endpoint

```http
GET /api/home
```

### Authentication

The API specification marks this endpoint as `USER`, but the current `SecurityConfig` permits all requests. This implementation will not introduce authentication changes. Authentication can be enforced later when the project has a request authentication filter and user domain model.

### Response Shape

```json
{
  "recentNotices": [],
  "recentArchives": [],
  "recentInfoPosts": [],
  "recentPhotoAlbums": []
}
```

### Notice Item

Uses `notice_mock` from the frontend mock data.

```json
{
  "id": 1,
  "title": "2026 D.COM 여름 프로젝트 팀 모집 안내",
  "author": "ADMIN",
  "date": "2026.06.20",
  "hasAttachment": true
}
```

### Archive Item

Uses `exam_mock` instead of `exam_archives_mock` because the home archive card must show the writer. The writer is returned as an `author` object, matching the author shape used by info posts.

```json
{
  "id": 1,
  "subject": "오픈소스SW개발방법및도구",
  "professor": "이성원",
  "semester": "2024-1",
  "author": {
    "studentNumber": "20230001",
    "name": "하성준"
  },
  "date": "2026-05-25"
}
```

### Info Post Item

Uses `infoPostList` from the frontend mock data.

```json
{
  "id": 1,
  "title": "시간 복잡도 Big-O 핵심 정리 (면접 필수)",
  "author": {
    "studentNumber": "20201234",
    "name": "표지훈"
  },
  "date": "2026.06.20.",
  "hasAttachment": true
}
```

### Photo Album Item

Uses `galleryPosts` from the frontend mock data.

```json
{
  "id": 1,
  "imageUrl": "khuBg1",
  "title": "2026-1 D.COM 커리어세션",
  "date": "2026.05.16",
  "imageCount": 5
}
```

## Architecture

Add a small Home feature package under `com.dcom.intranet.home`.

- `HomeController`: exposes `GET /api/home` and owns Swagger annotations.
- `HomeService`: builds the fixed home dashboard response.
- `HomeDashboardResponse`: top-level response DTO.
- `NoticeSummaryResponse`: notice card DTO.
- `ArchiveSummaryResponse`: archive card DTO.
- `AuthorResponse`: reusable author DTO for archives and info posts.
- `InfoPostSummaryResponse`: information board card DTO.
- `PhotoAlbumSummaryResponse`: photo album card DTO.

No database, JPA entity, repository, or security change is needed for this first Home API.

## Swagger

The endpoint should be documented with Springdoc annotations:

- `@Tag(name = "Home", description = "메인 페이지 API")`
- `@Operation(summary = "메인 대시보드 조회", description = "홈 화면에 표시할 최근 공지, 족보, 정보 공유 게시글, 활동 사진을 조회합니다.")`
- `@Schema` descriptions on response DTO fields

Swagger must show the Notice terminology. It must not expose `recentAnnouncements`, `userId`, `role`, or `mainMenu`.

## Testing

Use TDD for implementation.

First write a `@WebMvcTest(HomeController.class)` test that fails because the Home API does not exist yet.

The controller test should verify:

- `GET /api/home` returns HTTP 200.
- `recentNotices` exists and has 5 items.
- `recentArchives` exists and has 5 items.
- `recentInfoPosts` exists and has 5 items.
- `recentPhotoAlbums` exists and has 5 items.
- `recentArchives[0].author.name` exists.
- `recentArchives[0].author.studentNumber` exists.
- `recentAnnouncements` does not exist.
- `mainMenu` does not exist.
- `userId` does not exist.
- `role` does not exist.

After the failing test is confirmed, implement the minimal controller, service, and DTOs needed to pass.

## Known Environment Issue

The repository currently requires Java 21 through the Gradle toolchain:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

The local test run failed because Java 21 is not installed or configured for Gradle. The available default Java is 17, and `/usr/libexec/java_home -V` showed Java 25 and Java 11. This is an environment issue, not a Home API implementation issue.

Before final verification, install or configure Java 21 so `./gradlew test` can run.
