# 내가 쓴 댓글 목록 조회 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 최종 API 명세서 기준으로 `GET /api/users/me/comments` 내가 쓴 댓글 목록 조회 API를 구현한다.

**Architecture:** 기존 내가 쓴 글 목록 API와 같은 포트 패턴을 사용한다. `MyPageController`와 `MyPageService`는 인증 사용자 확인과 공통 응답 생성을 담당하고, 실제 정보 공유/활동 사진 댓글 조회는 `MyWrittenCommentReader` 구현체가 담당한다.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-07-01-my-written-comments-list-design.md`
- 최종 API 명세서 기준: `GET /api/users/me/comments`, query `page`, `size`, `type`, 응답 `commentList(commentId,type,targetId,targetTitle,content,createdAt)`, `pageInfo`, 상태코드 `200`, `401`
- 지원 type: `INFO_POST`, `PHOTO_ALBUM`
- 제외 범위: 내가 쓴 댓글 상세 이동 API, 내가 쓴 댓글 삭제 API, 정보 공유/활동 사진 댓글 도메인 엔티티와 저장소 구현

## 파일 구조

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 댓글 목록 조회 API의 성공, 빈 목록, query 전달, 인증 실패 테스트를 추가한다.
  - 실제 댓글 도메인 구현체 대신 테스트용 `MyWrittenCommentReader` 빈을 등록한다.
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java`
  - 다른 도메인 조회 구현체가 연결될 mypage 댓글 조회 포트다.
- Create: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java`
  - 현재 로컬 애플리케이션 실행을 위한 기본 구현체다.
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReaderConfig.java`
  - 실제 다른 도메인 구현체가 없을 때만 기본 구현체를 빈으로 등록한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListResponse.java`
  - 성공 응답 `data`의 최상위 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentResponse.java`
  - `commentList`의 단일 항목 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - 사용자 조회 후 `MyWrittenCommentReader`에 `userId`, `page`, `size`, `type`을 전달한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `GET /api/users/me/comments` endpoint와 Swagger response annotation을 추가한다.
- Modify: `docs/openapi.json`
  - 구현 후 댓글 목록 조회 API operation과 schema를 반영한다.

## Task 1: RED - 내가 쓴 댓글 목록 조회 테스트 추가

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: 테스트 import를 추가한다**

`src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`의 import 영역에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentResponse;
```

- [ ] **Step 2: 테스트용 reader 필드를 추가한다**

`TestMyWrittenPostReader myWrittenPostReader` 주입 필드 아래에 테스트용 댓글 reader 필드를 추가한다.

```java
@Autowired
private TestMyWrittenCommentReader myWrittenCommentReader;
```

- [ ] **Step 3: `setUp`에서 테스트용 reader를 초기화한다**

`setUp()` 메서드에서 `myWrittenPostReader.reset();` 바로 아래에 아래 코드를 추가한다.

```java
myWrittenCommentReader.reset();
```

변경 후 `setUp()`은 아래 형태가 된다.

```java
@BeforeEach
void setUp() {
    myWrittenPostReader.reset();
    myWrittenCommentReader.reset();
    emailVerificationRepository.deleteAll();
    userRepository.deleteAll();
}
```

- [ ] **Step 4: 성공 응답과 query 전달 테스트를 추가한다**

내가 쓴 글 목록 테스트와 내가 쓴 글 상세 이동 테스트 사이에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written comments list returns 200 common envelope and comment list")
void myWrittenCommentsListReturns200CommonEnvelopeAndCommentList() throws Exception {
    User user = saveUser("comments1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    myWrittenCommentReader.givenResponse(new MyWrittenCommentListResponse(
            List.of(new MyWrittenCommentResponse(
                    101L,
                    "INFO_POST",
                    12L,
                    "React 참고 자료 모음",
                    "좋은 자료 감사합니다.",
                    LocalDateTime.of(2026, 6, 1, 13, 0)
            )),
            new PageInfoResponse(0, 10, 1, 1L)
    ));

    mockMvc.perform(get("/api/users/me/comments")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .param("page", "0")
                    .param("size", "10")
                    .param("type", "INFO_POST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.commentList[0].commentId").value(101))
            .andExpect(jsonPath("$.data.commentList[0].type").value("INFO_POST"))
            .andExpect(jsonPath("$.data.commentList[0].targetId").value(12))
            .andExpect(jsonPath("$.data.commentList[0].targetTitle").value("React 참고 자료 모음"))
            .andExpect(jsonPath("$.data.commentList[0].content").value("좋은 자료 감사합니다."))
            .andExpect(jsonPath("$.data.commentList[0].createdAt").value("2026-06-01T13:00:00"))
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

    assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
    assertThat(myWrittenCommentReader.lastPage()).isEqualTo(0);
    assertThat(myWrittenCommentReader.lastSize()).isEqualTo(10);
    assertThat(myWrittenCommentReader.lastType()).isEqualTo("INFO_POST");
}
```

- [ ] **Step 5: `type` 생략과 기본 paging 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written comments list without type uses all types and default paging")
void myWrittenCommentsListWithoutTypeUsesAllTypesAndDefaultPaging() throws Exception {
    User user = saveUser("comments2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(get("/api/users/me/comments")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.commentList").isArray())
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10));

    assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
    assertThat(myWrittenCommentReader.lastPage()).isEqualTo(0);
    assertThat(myWrittenCommentReader.lastSize()).isEqualTo(10);
    assertThat(myWrittenCommentReader.lastType()).isNull();
}
```

- [ ] **Step 6: 빈 목록 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written comments list with no comments returns empty list")
void myWrittenCommentsListWithNoCommentsReturnsEmptyList() throws Exception {
    User user = saveUser("comments3", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    myWrittenCommentReader.givenResponse(MyWrittenCommentListResponse.empty(0, 10));

    mockMvc.perform(get("/api/users/me/comments")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .param("page", "0")
                    .param("size", "10")
                    .param("type", "PHOTO_ALBUM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.commentList").isArray())
            .andExpect(jsonPath("$.data.commentList").isEmpty())
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(0))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));

    assertThat(myWrittenCommentReader.lastType()).isEqualTo("PHOTO_ALBUM");
}
```

- [ ] **Step 7: 인증 실패 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written comments list without token returns 401 common envelope")
void myWrittenCommentsListWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(get("/api/users/me/comments"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("My written comments list with PENDING user token returns 401 common envelope")
void myWrittenCommentsListWithPendingUserTokenReturns401CommonEnvelope() throws Exception {
    User user = saveUser("commentsPending", UserStatus.PENDING, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(get("/api/users/me/comments")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

- [ ] **Step 8: 테스트용 reader와 config를 추가한다**

`TestMyWrittenPostReader` 클래스가 끝난 뒤, `studentIdFor` helper 위에 아래 테스트용 구현체와 설정을 추가한다.

```java
@TestConfiguration
static class MyWrittenCommentReaderTestConfig {

    @Bean
    @Primary
    TestMyWrittenCommentReader testMyWrittenCommentReader() {
        return new TestMyWrittenCommentReader();
    }
}

static class TestMyWrittenCommentReader implements MyWrittenCommentReader {

    private MyWrittenCommentListResponse response = MyWrittenCommentListResponse.empty(0, 10);
    private Long lastUserId;
    private int lastPage;
    private int lastSize;
    private String lastType;

    @Override
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        this.lastUserId = userId;
        this.lastPage = page;
        this.lastSize = size;
        this.lastType = type;
        return response;
    }

    void givenResponse(MyWrittenCommentListResponse response) {
        this.response = response;
    }

    void reset() {
        this.response = MyWrittenCommentListResponse.empty(0, 10);
        this.lastUserId = null;
        this.lastPage = -1;
        this.lastSize = -1;
        this.lastType = null;
    }

    Long lastUserId() {
        return lastUserId;
    }

    int lastPage() {
        return lastPage;
    }

    int lastSize() {
        return lastSize;
    }

    String lastType() {
        return lastType;
    }
}
```

- [ ] **Step 9: `studentIdFor` test fixture 값을 추가한다**

`studentIdFor` switch에 아래 case를 추가한다.

```java
case "comments1" -> "20240121";
case "comments2" -> "20240122";
case "comments3" -> "20240123";
case "commentsPending" -> "20240124";
```

- [ ] **Step 10: RED 확인 테스트를 실행한다**

Run:

```bash
./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL. `MyWrittenCommentReader`, `MyWrittenCommentListResponse`, `MyWrittenCommentResponse`가 아직 없어서 컴파일 실패하거나, endpoint가 없어 테스트가 실패한다.

## Task 2: GREEN - 최소 구현 추가

**Files:**
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java`
- Create: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java`
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReaderConfig.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListApiResponse.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: 댓글 조회 포트 인터페이스를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java`:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;

public interface MyWrittenCommentReader {

    MyWrittenCommentListResponse read(Long userId, int page, int size, String type);
}
```

- [ ] **Step 2: 빈 댓글 조회 기본 구현체를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java`:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;

public class EmptyMyWrittenCommentReader implements MyWrittenCommentReader {

    @Override
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenCommentListResponse.empty(page, size);
    }
}
```

- [ ] **Step 3: 기본 구현체 config를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReaderConfig.java`:

```java
package com.dcom.intranet.mypage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyWrittenCommentReaderConfig {

    @Bean
    @ConditionalOnMissingBean(MyWrittenCommentReader.class)
    public MyWrittenCommentReader emptyMyWrittenCommentReader() {
        return new EmptyMyWrittenCommentReader();
    }
}
```

- [ ] **Step 4: 댓글 목록 응답 DTO를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 쓴 댓글 목록 조회 응답 데이터")
public record MyWrittenCommentListResponse(
        @Schema(description = "내가 쓴 댓글 목록")
        List<MyWrittenCommentResponse> commentList,

        @Schema(description = "페이지 정보")
        PageInfoResponse pageInfo
) {

    public static MyWrittenCommentListResponse empty(int page, int size) {
        return new MyWrittenCommentListResponse(
                List.of(),
                new PageInfoResponse(page, size, 0, 0)
        );
    }
}
```

- [ ] **Step 5: 댓글 목록 항목 DTO를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 쓴 댓글 목록 항목")
public record MyWrittenCommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "댓글 유형", example = "INFO_POST")
        String type,

        @Schema(description = "댓글이 달린 원본 대상 ID", example = "12")
        Long targetId,

        @Schema(description = "댓글이 달린 원본 대상 제목", example = "React 참고 자료 모음")
        String targetTitle,

        @Schema(description = "댓글 내용", example = "좋은 자료 감사합니다.")
        String content,

        @Schema(description = "작성일시", example = "2026-06-01T13:00:00")
        LocalDateTime createdAt
) {
}
```

- [ ] **Step 6: Swagger 성공 응답 wrapper를 추가한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentListApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 목록 조회 성공 응답")
public class MyWrittenCommentListApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "내가 쓴 댓글 목록 조회 결과")
    public MyWrittenCommentListResponse data;
}
```

- [ ] **Step 7: `MyPageService`에 댓글 reader 의존성과 service method를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageService.java`에 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
```

`myWrittenPostReader` 필드 아래에 필드를 추가한다.

```java
private final MyWrittenCommentReader myWrittenCommentReader;
```

생성자 파라미터에 `MyWrittenCommentReader myWrittenCommentReader`를 추가하고 필드에 할당한다.

```java
public MyPageService(
        UserRepository userRepository,
        EmailVerificationService emailVerificationService,
        PasswordEncoder passwordEncoder,
        MyWrittenPostReader myWrittenPostReader,
        MyWrittenCommentReader myWrittenCommentReader
) {
    this.userRepository = userRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordEncoder = passwordEncoder;
    this.myWrittenPostReader = myWrittenPostReader;
    this.myWrittenCommentReader = myWrittenCommentReader;
}
```

`getMyPosts` 아래에 아래 메서드를 추가한다.

```java
@Transactional(readOnly = true)
public MyWrittenCommentListResponse getMyComments(String loginId, int page, int size, String type) {
    User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
    return myWrittenCommentReader.read(user.getId(), page, size, type);
}
```

- [ ] **Step 8: `MyPageController`에 댓글 API import와 endpoint를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageController.java`에 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentListApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
```

`getMyPosts` 메서드 아래에 아래 endpoint를 추가한다.

```java
@Operation(
        summary = "내가 쓴 댓글 목록 조회",
        description = "인증된 사용자가 본인이 작성한 정보 공유 게시판 댓글, 활동 사진 댓글 목록을 조회한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "내가 쓴 댓글 목록 조회 성공",
                        content = @Content(schema = @Schema(implementation = MyWrittenCommentListApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                )
        }
)
@GetMapping("/me/comments")
public ResponseEntity<ApiResponse<MyWrittenCommentListResponse>> getMyComments(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String type
) {
    MyWrittenCommentListResponse response = myPageService.getMyComments(
            authentication.getName(),
            page,
            size,
            type
    );
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 9: GREEN 확인 테스트를 실행한다**

Run:

```bash
./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS. 댓글 목록 조회 테스트와 기존 마이페이지 컨트롤러 테스트가 통과한다.

## Task 3: OpenAPI 반영

**Files:**
- Modify: `docs/openapi.json`
- Test: `docs/openapi.json`

- [ ] **Step 1: OpenAPI 문서를 직접 갱신한다**

`docs/openapi.json`에 아래 항목을 직접 반영한다.

- path: `/api/users/me/comments`
- method: `get`
- query parameters: `page`, `size`, `type`
- responses: `200` -> `MyWrittenCommentListApiResponse`, `401` -> `UnauthorizedApiResponse`
- schemas: `MyWrittenCommentListApiResponse`, `MyWrittenCommentListResponse`, `MyWrittenCommentResponse`

`docs/openapi.json`의 path 추가 위치는 `/api/users/me/posts` 근처로 둔다. JSON formatting은 기존처럼 들여쓰기와 줄바꿈을 유지한다.

- [ ] **Step 2: OpenAPI schema 핵심 구조를 확인한다**

`docs/openapi.json`에서 아래 필드가 확인되어야 한다.

```json
"/api/users/me/comments": {
  "get": {
    "summary": "내가 쓴 댓글 목록 조회",
    "parameters": [
      { "name": "page" },
      { "name": "size" },
      { "name": "type" }
    ],
    "responses": {
      "200": {
        "content": {
          "*/*": {
            "schema": {
              "$ref": "#/components/schemas/MyWrittenCommentListApiResponse"
            }
          }
        }
      },
      "401": {
        "content": {
          "*/*": {
            "schema": {
              "$ref": "#/components/schemas/UnauthorizedApiResponse"
            }
          }
        }
      }
    }
  }
}
```

`components.schemas.MyWrittenCommentResponse`에는 아래 properties가 있어야 한다.

```json
{
  "commentId": {
    "type": "integer",
    "format": "int64"
  },
  "type": {
    "type": "string"
  },
  "targetId": {
    "type": "integer",
    "format": "int64"
  },
  "targetTitle": {
    "type": "string"
  },
  "content": {
    "type": "string"
  },
  "createdAt": {
    "type": "string",
    "format": "date-time"
  }
}
```

- [ ] **Step 3: OpenAPI JSON 문법을 확인한다**

Run:

```bash
node -e 'JSON.parse(require("fs").readFileSync("docs/openapi.json", "utf8")); console.log("openapi json ok")'
```

Expected: `openapi json ok`

## Task 4: Full Verification and Review

**Files:**
- Test: full project test suite
- Review: git diff

- [ ] **Step 1: 전체 테스트를 실행한다**

Run:

```bash
./gradlew test --rerun-tasks
```

Expected: PASS.

- [ ] **Step 2: 변경 범위를 확인한다**

Run:

```bash
git diff --stat
```

Expected: 변경 파일이 이번 API 범위에 한정된다. 사용자 작업으로 보이는 `docs/profile_update-openapi.json`, `AGENTS.md`, 기존 `docs/superpowers/plans/2026-06-28-member-profile-lookup.md`, `mypage_front/`는 이번 작업 diff에 포함하지 않는다.

- [ ] **Step 3: 명세 상태코드와 응답 필드를 검토한다**

확인 항목:

- `GET /api/users/me/comments`만 구현했다.
- 상세 이동과 삭제 API는 추가하지 않았다.
- 성공 응답은 `success`, `status`, `message`, `data.commentList`, `data.pageInfo` 구조다.
- 실패 응답은 명세에 있는 `401`만 Swagger와 테스트에 포함했다.
- `commentList` 항목은 `commentId`, `type`, `targetId`, `targetTitle`, `content`, `createdAt`만 포함한다.
- `type` 필드명은 승인된 설계대로 `targetType`이 아니라 `type`이다.

- [ ] **Step 4: code-review 단계에서 발견한 중요 이슈를 수정한다**

검토 중 버그나 명세 위반을 발견하면 해당 케이스를 먼저 테스트로 추가하고 실패를 확인한 뒤 최소 구현으로 통과시킨다. 새 기능이나 명세 외 상태코드는 추가하지 않는다.

## Self-Review

- Spec coverage: 설계 문서의 endpoint, query, response, 상태코드, 제외 범위, OpenAPI 요구사항이 Task 1-4에 반영되어 있다.
- Placeholder scan: 계획에 빈 요구사항이나 미정 항목은 없다.
- Type consistency: `MyWrittenCommentReader`, `MyWrittenCommentListResponse`, `MyWrittenCommentResponse`, `MyWrittenCommentListApiResponse`, `getMyComments` 이름을 모든 작업에서 동일하게 사용한다.
