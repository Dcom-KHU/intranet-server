# Home Dashboard API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `GET /api/home` so the frontend can fetch the Home dashboard data through Swagger-documented backend response DTOs.

**Architecture:** Add a focused `com.dcom.intranet.home` package with a controller, service, and response DTO records. The service returns fixed mock-based data for now because the related Notice, Archive, Info Post, and Photo Album domain modules do not exist yet.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Springdoc OpenAPI, JUnit 5, MockMvc.

---

## File Structure

- Create: `src/test/java/com/dcom/intranet/home/HomeControllerTest.java`
  - Web MVC slice test for the Home API response contract.
- Create: `src/main/java/com/dcom/intranet/home/HomeController.java`
  - REST controller for `GET /api/home` and Swagger endpoint annotations.
- Create: `src/main/java/com/dcom/intranet/home/HomeService.java`
  - Builds fixed mock-based dashboard data, limited to 5 items per section.
- Create: `src/main/java/com/dcom/intranet/home/dto/HomeDashboardResponse.java`
  - Top-level response DTO.
- Create: `src/main/java/com/dcom/intranet/home/dto/NoticeSummaryResponse.java`
  - Notice summary DTO.
- Create: `src/main/java/com/dcom/intranet/home/dto/ArchiveSummaryResponse.java`
  - Archive summary DTO, including `author`.
- Create: `src/main/java/com/dcom/intranet/home/dto/AuthorResponse.java`
  - Shared author DTO for archives and info posts.
- Create: `src/main/java/com/dcom/intranet/home/dto/InfoPostSummaryResponse.java`
  - Information board post summary DTO.
- Create: `src/main/java/com/dcom/intranet/home/dto/PhotoAlbumSummaryResponse.java`
  - Photo album summary DTO.

Do not modify `src/main/java/com/dcom/intranet/dto/auth.java` or `src/main/java/com/dcom/intranet/dto/user.java`; they are unrelated empty placeholder files.

## Verification Prerequisite

The repository currently requires Java 21 through Gradle toolchains, but the local machine does not have Java 21 configured. Before executing test commands, install or configure a Java 21 JDK.

Expected verification command after Java 21 is available:

```bash
./gradlew test
```

Expected final result:

```text
BUILD SUCCESSFUL
```

---

### Task 1: Write Failing Home Controller Test

**Files:**
- Create: `src/test/java/com/dcom/intranet/home/HomeControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.dcom.intranet.home;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
@Import(HomeService.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getHomeDashboardReturnsRecentMockDataWithoutUnusedFields() throws Exception {
        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentNotices", hasSize(5)))
                .andExpect(jsonPath("$.recentArchives", hasSize(5)))
                .andExpect(jsonPath("$.recentInfoPosts", hasSize(5)))
                .andExpect(jsonPath("$.recentPhotoAlbums", hasSize(5)))
                .andExpect(jsonPath("$.recentNotices[0].title").value("2026 D.COM 여름 프로젝트 팀 모집 안내"))
                .andExpect(jsonPath("$.recentArchives[0].subject").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.recentArchives[0].author.name").value("하성준"))
                .andExpect(jsonPath("$.recentArchives[0].author.studentNumber").value("20230001"))
                .andExpect(jsonPath("$.recentArchives[0].date").value("2026.05.25"))
                .andExpect(jsonPath("$.recentArchives[0].semester").doesNotExist())
                .andExpect(jsonPath("$.recentInfoPosts[0].author.name").value("표지훈"))
                .andExpect(jsonPath("$.recentPhotoAlbums[0].title").value("2026-1 D.COM 커리어세션"))
                .andExpect(jsonPath("$.recentAnnouncements").doesNotExist())
                .andExpect(jsonPath("$.mainMenu").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```bash
./gradlew test --tests com.dcom.intranet.home.HomeControllerTest
```

Expected after Java 21 is configured:

```text
Compilation failed; HomeController and HomeService do not exist yet.
```

If the command fails before compilation because Java 21 is missing, stop and fix the Java 21 environment first. Do not write production code until the test can fail for the expected missing-feature reason.

---

### Task 2: Add Home Response DTOs

**Files:**
- Create: `src/main/java/com/dcom/intranet/home/dto/HomeDashboardResponse.java`
- Create: `src/main/java/com/dcom/intranet/home/dto/NoticeSummaryResponse.java`
- Create: `src/main/java/com/dcom/intranet/home/dto/ArchiveSummaryResponse.java`
- Create: `src/main/java/com/dcom/intranet/home/dto/AuthorResponse.java`
- Create: `src/main/java/com/dcom/intranet/home/dto/InfoPostSummaryResponse.java`
- Create: `src/main/java/com/dcom/intranet/home/dto/PhotoAlbumSummaryResponse.java`

- [ ] **Step 1: Create the top-level response DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "메인 대시보드 응답")
public record HomeDashboardResponse(
        @Schema(description = "최근 공지 목록")
        List<NoticeSummaryResponse> recentNotices,

        @Schema(description = "최근 족보 목록")
        List<ArchiveSummaryResponse> recentArchives,

        @Schema(description = "최근 정보 공유 게시글 목록")
        List<InfoPostSummaryResponse> recentInfoPosts,

        @Schema(description = "최근 활동 사진 앨범 목록")
        List<PhotoAlbumSummaryResponse> recentPhotoAlbums
) {
}
```

- [ ] **Step 2: Create the notice summary DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 요약")
public record NoticeSummaryResponse(
        @Schema(description = "공지 ID", example = "1")
        Long id,

        @Schema(description = "공지 제목", example = "2026 D.COM 여름 프로젝트 팀 모집 안내")
        String title,

        @Schema(description = "작성자", example = "ADMIN")
        String author,

        @Schema(description = "작성일", example = "2026.06.20")
        String date,

        @Schema(description = "첨부파일 존재 여부", example = "true")
        boolean hasAttachment
) {
}
```

- [ ] **Step 3: Create the shared author DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작성자 요약")
public record AuthorResponse(
        @Schema(description = "학번", example = "20230001")
        String studentNumber,

        @Schema(description = "이름", example = "하성준")
        String name
) {
}
```

- [ ] **Step 4: Create the archive summary DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "족보 요약")
public record ArchiveSummaryResponse(
        @Schema(description = "족보 ID", example = "1")
        Long id,

        @Schema(description = "과목명", example = "오픈소스SW개발방법및도구")
        String subject,

        @Schema(description = "교수명", example = "이성원")
        String professor,

        @Schema(description = "작성자")
        AuthorResponse author,

        @Schema(description = "등록일", example = "2026.05.25")
        String date
) {
}
```

- [ ] **Step 5: Create the info post summary DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정보 공유 게시글 요약")
public record InfoPostSummaryResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 제목", example = "시간 복잡도 Big-O 핵심 정리 (면접 필수)")
        String title,

        @Schema(description = "작성자")
        AuthorResponse author,

        @Schema(description = "작성일", example = "2026.06.20.")
        String date,

        @Schema(description = "첨부파일 존재 여부", example = "true")
        boolean hasAttachment
) {
}
```

- [ ] **Step 6: Create the photo album summary DTO**

```java
package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "활동 사진 앨범 요약")
public record PhotoAlbumSummaryResponse(
        @Schema(description = "앨범 ID", example = "1")
        Long id,

        @Schema(description = "대표 이미지 URL 또는 프론트 목업 이미지 키", example = "khuBg1")
        String imageUrl,

        @Schema(description = "앨범 제목", example = "2026-1 D.COM 커리어세션")
        String title,

        @Schema(description = "활동일", example = "2026.05.16")
        String date,

        @Schema(description = "이미지 개수", example = "5")
        int imageCount
) {
}
```

- [ ] **Step 7: Run the failing controller test again**

Run:

```bash
./gradlew test --tests com.dcom.intranet.home.HomeControllerTest
```

Expected:

```text
Compilation failed; HomeController and HomeService do not exist yet.
```

---

### Task 3: Add Home Service With Fixed Mock-Based Data

**Files:**
- Create: `src/main/java/com/dcom/intranet/home/HomeService.java`

- [ ] **Step 1: Create the service**

```java
package com.dcom.intranet.home;

import com.dcom.intranet.home.dto.ArchiveSummaryResponse;
import com.dcom.intranet.home.dto.AuthorResponse;
import com.dcom.intranet.home.dto.HomeDashboardResponse;
import com.dcom.intranet.home.dto.InfoPostSummaryResponse;
import com.dcom.intranet.home.dto.NoticeSummaryResponse;
import com.dcom.intranet.home.dto.PhotoAlbumSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public HomeDashboardResponse getHomeDashboard() {
        return new HomeDashboardResponse(
                recentNotices(),
                recentArchives(),
                recentInfoPosts(),
                recentPhotoAlbums()
        );
    }

    private List<NoticeSummaryResponse> recentNotices() {
        return List.of(
                new NoticeSummaryResponse(1L, "2026 D.COM 여름 프로젝트 팀 모집 안내", "ADMIN", "2026.06.20", true),
                new NoticeSummaryResponse(2L, "정기 세미나 발표자 신청 안내", "ADMIN", "2026.06.14", false),
                new NoticeSummaryResponse(3L, "동아리방 이용 수칙 변경 안내", "ADMIN", "2026.06.05", false),
                new NoticeSummaryResponse(4L, "신입 부원 Git 기초 워크숍 일정", "ADMIN", "2026.05.29", true),
                new NoticeSummaryResponse(5L, "기말고사 기간 활동 일정 조정 안내", "ADMIN", "2026.05.18", true)
        );
    }

    private List<ArchiveSummaryResponse> recentArchives() {
        return List.of(
                new ArchiveSummaryResponse(1L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20230001", "하성준"), "2026.05.25"),
                new ArchiveSummaryResponse(2L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20209999", "곽민서"), "2026.04.25"),
                new ArchiveSummaryResponse(3L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20210012", "신정안"), "2025.05.05"),
                new ArchiveSummaryResponse(4L, "자료구조", "박제만", new AuthorResponse("20220014", "최진영"), "2026.05.20"),
                new ArchiveSummaryResponse(5L, "자료구조", "박제만", new AuthorResponse("20210032", "최진영"), "2026.05.15")
        );
    }

    private List<InfoPostSummaryResponse> recentInfoPosts() {
        return List.of(
                new InfoPostSummaryResponse(1L, "시간 복잡도 Big-O 핵심 정리 (면접 필수)", new AuthorResponse("20201234", "표지훈"), "2026.06.20.", true),
                new InfoPostSummaryResponse(2L, "TCP 3-way handshake 동작 원리 정리", new AuthorResponse("20201111", "허남준"), "2026.06.21.", true),
                new InfoPostSummaryResponse(3L, "운영체제: 프로세스 vs 스레드 완벽 비교", new AuthorResponse("20201333", "안유진"), "2026.06.22.", false),
                new InfoPostSummaryResponse(4L, "DB 인덱스(B-Tree) 구조 이해하기", new AuthorResponse("20201444", "김선호"), "2026.06.23.", false),
                new InfoPostSummaryResponse(5L, "동기/비동기 & Blocking/Non-blocking 차이", new AuthorResponse("20201555", "지창욱"), "2026.06.24.", true)
        );
    }

    private List<PhotoAlbumSummaryResponse> recentPhotoAlbums() {
        return List.of(
                new PhotoAlbumSummaryResponse(1L, "khuBg1", "2026-1 D.COM 커리어세션", "2026.05.16", 5),
                new PhotoAlbumSummaryResponse(2L, "khuBg2", "2026-1 D.COM 정기 세미나", "2026.05.09", 8),
                new PhotoAlbumSummaryResponse(3L, "khuBg3", "2026-1 D.COM 네트워킹 데이", "2026.04.26", 6),
                new PhotoAlbumSummaryResponse(4L, "khuBg1", "2026-1 D.COM 프로젝트 발표회", "2026.04.12", 12),
                new PhotoAlbumSummaryResponse(5L, "khuBg2", "2026-1 D.COM MT", "2026.03.29", 10)
        );
    }
}
```

- [ ] **Step 2: Run the failing controller test again**

Run:

```bash
./gradlew test --tests com.dcom.intranet.home.HomeControllerTest
```

Expected:

```text
Compilation failed; HomeController does not exist yet.
```

---

### Task 4: Add Home Controller With Swagger Documentation

**Files:**
- Create: `src/main/java/com/dcom/intranet/home/HomeController.java`

- [ ] **Step 1: Create the controller**

```java
package com.dcom.intranet.home;

import com.dcom.intranet.home.dto.HomeDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "메인 페이지 API")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @Operation(
            summary = "메인 대시보드 조회",
            description = "홈 화면에 표시할 최근 공지, 족보, 정보 공유 게시글, 활동 사진을 조회합니다."
    )
    @GetMapping
    public HomeDashboardResponse getHomeDashboard() {
        return homeService.getHomeDashboard();
    }
}
```

- [ ] **Step 2: Run the controller test and verify it passes**

Run:

```bash
./gradlew test --tests com.dcom.intranet.home.HomeControllerTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: Run the full test suite**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: Commit the green implementation**

Run:

```bash
git add src/test/java/com/dcom/intranet/home/HomeControllerTest.java src/main/java/com/dcom/intranet/home
git commit -m "feat: add home dashboard api"
```

Expected:

```text
[feature/home <sha>] feat: add home dashboard api
```

---

### Task 5: Review Swagger Contract Manually

**Files:**
- Inspect: `src/main/java/com/dcom/intranet/home/HomeController.java`
- Inspect: `src/main/java/com/dcom/intranet/home/dto/*.java`

- [ ] **Step 1: Start the application**

Run:

```bash
./gradlew bootRun
```

Expected:

```text
Started DcomIntranetServerApplication
```

- [ ] **Step 2: Open Swagger UI**

Use this URL:

```text
http://localhost:8080/swagger-ui/index.html
```

Expected:

```text
Swagger UI shows the Home tag and GET /api/home.
```

- [ ] **Step 3: Confirm the response schema**

Check that the `GET /api/home` response schema includes:

```text
recentNotices
recentArchives
recentInfoPosts
recentPhotoAlbums
```

Check that the response schema does not include:

```text
recentAnnouncements
mainMenu
userId
role
```

Check that archive items include:

```text
author.name
author.studentNumber
```

Check that archive items do not include:

```text
semester
```

Check that archive item dates use the same mock-data format as the other sections:

```text
2026.05.25
```

- [ ] **Step 4: Stop the application**

Press:

```text
Ctrl+C
```

---

## Plan Self-Review

- Spec coverage: The plan implements only `GET /api/home`, excludes `GET /api/home/sidebar`, uses Notice naming, removes unused fields, returns 5 items per section, includes archive author data, excludes archive semester, and formats archive dates as `yyyy.MM.dd`.
- Placeholder scan: No TBD, TODO, or unspecified implementation steps remain.
- Type consistency: DTO names and method signatures are consistent across test, service, and controller tasks.
