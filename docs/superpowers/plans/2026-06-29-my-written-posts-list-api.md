# 내가 쓴 글 목록 조회 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 최종 API 명세서 기준으로 `GET /api/users/me/posts` 내가 쓴 글 목록 조회 API를 구현한다.

**Architecture:** `MyPageController`와 `MyPageService`는 기존 마이페이지 패턴을 확장하고, 실제 게시글 목록 조회는 `MyWrittenPostReader` 포트에 위임한다. 현재 로컬에는 정보 공유/족보/활동 사진 도메인 구현체가 없으므로 기본 구현체는 빈 목록을 반환하고, 테스트에서는 `@Primary` 테스트 구현체로 query 전달과 응답 구조를 검증한다.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-06-29-my-written-posts-list-design.md`
- 최종 API 명세서 기준: `GET /api/users/me/posts`, query `page`, `size`, `type`, 응답 `postList(postId,title,type,createdAt)`, `pageInfo`, 상태코드 `200`, `401`
- 구현 범위: 내가 쓴 글 목록 조회 API만 포함한다.

## 파일 구조

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 내가 쓴 글 목록 조회 API의 성공, 빈 목록, query 전달, 인증 실패 테스트를 추가한다.
  - 실제 다른 도메인 구현체 대신 테스트용 `MyWrittenPostReader` 빈을 등록한다.
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java`
  - 다른 도메인 조회 구현체가 연결될 mypage 조회 포트다.
- Create: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java`
  - 현재 로컬 애플리케이션 실행을 위한 기본 구현체다.
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReaderConfig.java`
  - 실제 다른 도메인 구현체가 없을 때만 기본 구현체를 빈으로 등록한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListResponse.java`
  - 성공 응답 `data`의 최상위 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostResponse.java`
  - `postList`의 단일 항목 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/PageInfoResponse.java`
  - `pageInfo` 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - 사용자 조회 후 `MyWrittenPostReader`에 `userId`, `page`, `size`, `type`을 전달한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `GET /api/users/me/posts` endpoint와 Swagger response annotation을 추가한다.
- Modify: `docs/openapi.json`
  - 구현 후 `/v3/api-docs`에서 재생성해 내가 쓴 글 목록 조회 API schema를 반영한다.

## Task 1: RED - 내가 쓴 글 목록 조회 테스트 추가

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: 테스트 import를 추가한다**

`src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`의 import 영역에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostResponse;
import com.dcom.intranet.mypage.dto.PageInfoResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
```

- [ ] **Step 2: 테스트용 reader 필드를 추가한다**

`PasswordEncoder` 주입 필드 아래에 테스트용 reader 필드를 추가한다.

```java
@Autowired
private TestMyWrittenPostReader myWrittenPostReader;
```

- [ ] **Step 3: `setUp`에서 테스트용 reader를 초기화한다**

`setUp()` 메서드 첫 줄에 아래 코드를 추가한다.

```java
myWrittenPostReader.reset();
```

변경 후 `setUp()`은 아래 형태가 된다.

```java
@BeforeEach
void setUp() {
    myWrittenPostReader.reset();
    emailVerificationRepository.deleteAll();
    userRepository.deleteAll();
}
```

- [ ] **Step 4: 성공 응답과 query 전달 테스트를 추가한다**

`Password change without token returns 401 common envelope` 테스트와 `saveUser` helper 사이에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written posts list returns 200 common envelope and post list")
void myWrittenPostsListReturns200CommonEnvelopeAndPostList() throws Exception {
    User user = saveUser("posts1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    myWrittenPostReader.givenResponse(new MyWrittenPostListResponse(
            List.of(new MyWrittenPostResponse(
                    11L,
                    "오픈소스SW개발방법및도구",
                    "ARCHIVE",
                    LocalDateTime.of(2026, 5, 25, 10, 30)
            )),
            new PageInfoResponse(0, 10, 1, 1L)
    ));

    mockMvc.perform(get("/api/users/me/posts")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .param("page", "0")
                    .param("size", "10")
                    .param("type", "ARCHIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.postList[0].postId").value(11))
            .andExpect(jsonPath("$.data.postList[0].title").value("오픈소스SW개발방법및도구"))
            .andExpect(jsonPath("$.data.postList[0].type").value("ARCHIVE"))
            .andExpect(jsonPath("$.data.postList[0].createdAt").value("2026-05-25T10:30:00"))
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

    assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
    assertThat(myWrittenPostReader.lastPage()).isEqualTo(0);
    assertThat(myWrittenPostReader.lastSize()).isEqualTo(10);
    assertThat(myWrittenPostReader.lastType()).isEqualTo("ARCHIVE");
}
```

- [ ] **Step 5: `type` 생략과 기본 paging 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written posts list without type uses all types and default paging")
void myWrittenPostsListWithoutTypeUsesAllTypesAndDefaultPaging() throws Exception {
    User user = saveUser("posts2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(get("/api/users/me/posts")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.postList").isArray())
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10));

    assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
    assertThat(myWrittenPostReader.lastPage()).isEqualTo(0);
    assertThat(myWrittenPostReader.lastSize()).isEqualTo(10);
    assertThat(myWrittenPostReader.lastType()).isNull();
}
```

- [ ] **Step 6: 빈 목록 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written posts list with no posts returns empty list")
void myWrittenPostsListWithNoPostsReturnsEmptyList() throws Exception {
    User user = saveUser("posts3", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    myWrittenPostReader.givenResponse(MyWrittenPostListResponse.empty(0, 10));

    mockMvc.perform(get("/api/users/me/posts")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.postList").isArray())
            .andExpect(jsonPath("$.data.postList").isEmpty())
            .andExpect(jsonPath("$.data.pageInfo.page").value(0))
            .andExpect(jsonPath("$.data.pageInfo.size").value(10))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(0))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));
}
```

- [ ] **Step 7: 인증 실패 테스트를 추가한다**

같은 위치에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("My written posts list without token returns 401 common envelope")
void myWrittenPostsListWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(get("/api/users/me/posts"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("My written posts list with PENDING user token returns 401 common envelope")
void myWrittenPostsListWithPendingUserTokenReturns401CommonEnvelope() throws Exception {
    User user = saveUser("postsPending", UserStatus.PENDING, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(get("/api/users/me/posts")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

- [ ] **Step 8: 테스트용 reader와 config를 추가한다**

`studentIdFor` helper 위에 아래 테스트용 구현체와 설정을 추가한다.

```java
@TestConfiguration
static class MyWrittenPostReaderTestConfig {

    @Bean
    @Primary
    TestMyWrittenPostReader testMyWrittenPostReader() {
        return new TestMyWrittenPostReader();
    }
}

static class TestMyWrittenPostReader implements MyWrittenPostReader {

    private MyWrittenPostListResponse response = MyWrittenPostListResponse.empty(0, 10);
    private Long lastUserId;
    private int lastPage;
    private int lastSize;
    private String lastType;

    @Override
    public MyWrittenPostListResponse read(Long userId, int page, int size, String type) {
        this.lastUserId = userId;
        this.lastPage = page;
        this.lastSize = size;
        this.lastType = type;
        return response;
    }

    void givenResponse(MyWrittenPostListResponse response) {
        this.response = response;
    }

    void reset() {
        this.response = MyWrittenPostListResponse.empty(0, 10);
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

- [ ] **Step 9: RED 테스트를 실행해 실패를 확인한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD FAILED
```

예상 실패 이유:

```text
cannot find symbol
  symbol:   class MyWrittenPostReader
```

또는 아래 DTO 중 하나가 아직 없다는 컴파일 실패가 발생한다.

```text
cannot find symbol
  symbol:   class MyWrittenPostListResponse
```

이 실패는 production code가 아직 없기 때문에 기대한 RED 상태다.

## Task 2: GREEN - 조회 포트와 응답 DTO 추가

**Files:**
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java`
- Create: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java`
- Create: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReaderConfig.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/PageInfoResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListApiResponse.java`

- [ ] **Step 1: `MyWrittenPostReader` 포트를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java`:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;

public interface MyWrittenPostReader {

    MyWrittenPostListResponse read(Long userId, int page, int size, String type);
}
```

- [ ] **Step 2: `PageInfoResponse`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/PageInfoResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "페이지 정보")
public record PageInfoResponse(
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "전체 요소 수", example = "1")
        long totalElements
) {
}
```

- [ ] **Step 3: `MyWrittenPostResponse`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 쓴 글 목록 항목")
public record MyWrittenPostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "오픈소스SW개발방법및도구")
        String title,

        @Schema(description = "게시글 유형", example = "ARCHIVE")
        String type,

        @Schema(description = "작성일시", example = "2026-05-25T10:30:00")
        LocalDateTime createdAt
) {
}
```

- [ ] **Step 4: `MyWrittenPostListResponse`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 쓴 글 목록 조회 응답 데이터")
public record MyWrittenPostListResponse(
        @Schema(description = "내가 쓴 글 목록")
        List<MyWrittenPostResponse> postList,

        @Schema(description = "페이지 정보")
        PageInfoResponse pageInfo
) {

    public static MyWrittenPostListResponse empty(int page, int size) {
        return new MyWrittenPostListResponse(
                List.of(),
                new PageInfoResponse(page, size, 0, 0)
        );
    }
}
```

- [ ] **Step 5: Swagger wrapper를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 글 목록 조회 성공 응답")
public class MyWrittenPostListApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "내가 쓴 글 목록 조회 결과")
    public MyWrittenPostListResponse data;
}
```

- [ ] **Step 6: 기본 빈 목록 reader를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java`:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;

public class EmptyMyWrittenPostReader implements MyWrittenPostReader {

    @Override
    public MyWrittenPostListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenPostListResponse.empty(page, size);
    }
}
```

- [ ] **Step 7: 기본 reader 조건부 등록 설정을 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReaderConfig.java`:

```java
package com.dcom.intranet.mypage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyWrittenPostReaderConfig {

    @Bean
    @ConditionalOnMissingBean(MyWrittenPostReader.class)
    public MyWrittenPostReader emptyMyWrittenPostReader() {
        return new EmptyMyWrittenPostReader();
    }
}
```

- [ ] **Step 8: 테스트를 실행해 다음 실패를 확인한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD FAILED
```

예상 실패 이유:

```text
Status expected:<200> but was:<404>
```

이 실패는 endpoint가 아직 없기 때문에 기대한 RED 상태다.

## Task 3: GREEN - 서비스와 컨트롤러 endpoint 추가

**Files:**
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`

- [ ] **Step 1: `MyPageService` import를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageService.java`에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
```

- [ ] **Step 2: `MyPageService`에 reader 필드를 추가한다**

기존 필드 아래에 `MyWrittenPostReader` 필드를 추가한다.

```java
private final MyWrittenPostReader myWrittenPostReader;
```

- [ ] **Step 3: `MyPageService` 생성자에 reader를 주입한다**

기존 생성자를 아래 형태로 변경한다.

```java
public MyPageService(
        UserRepository userRepository,
        EmailVerificationService emailVerificationService,
        PasswordEncoder passwordEncoder,
        MyWrittenPostReader myWrittenPostReader
) {
    this.userRepository = userRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordEncoder = passwordEncoder;
    this.myWrittenPostReader = myWrittenPostReader;
}
```

- [ ] **Step 4: `MyPageService`에 목록 조회 메서드를 추가한다**

`getMyProfile` 메서드 아래에 아래 메서드를 추가한다.

```java
@Transactional(readOnly = true)
public MyWrittenPostListResponse getMyPosts(String loginId, int page, int size, String type) {
    User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
    return myWrittenPostReader.read(user.getId(), page, size, type);
}
```

- [ ] **Step 5: `MyPageController` import를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageController.java`에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.MyWrittenPostListApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import org.springframework.web.bind.annotation.RequestParam;
```

- [ ] **Step 6: `MyPageController`에 endpoint를 추가한다**

`getMyProfile` 메서드 아래에 아래 endpoint를 추가한다.

```java
@Operation(
        summary = "내가 쓴 글 목록 조회",
        description = "인증된 사용자가 본인이 작성한 정보 공유 게시글, 족보 글, 활동 사진 댓글 목록을 조회한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "내가 쓴 글 목록 조회 성공",
                        content = @Content(schema = @Schema(implementation = MyWrittenPostListApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                )
        }
)
@GetMapping("/me/posts")
public ResponseEntity<ApiResponse<MyWrittenPostListResponse>> getMyPosts(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String type
) {
    MyWrittenPostListResponse response = myPageService.getMyPosts(
            authentication.getName(),
            page,
            size,
            type
    );
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 7: mypage 컨트롤러 테스트를 실행해 통과를 확인한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 4: REFACTOR - 범위 검증과 전체 테스트

**Files:**
- No planned source edits.

- [ ] **Step 1: 명세서에 없는 endpoint가 추가되지 않았는지 확인한다**

Run:

```bash
rg -n '"/me/posts"|"/api/users/me/posts"|posts/\\{postId\\}|deleteMy|detailUrl|targetType' src/main/java src/test/java docs/superpowers/specs/2026-06-29-my-written-posts-list-design.md
```

Expected:

```text
At least one matching line from MyPageController.java, MyPageControllerTest.java, and the design spec.
```

If the output shows a new detail 이동 API or delete API implementation under `src/main/java`, remove that implementation before continuing.

- [ ] **Step 2: 전체 테스트를 실행한다**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 5: DOCS - OpenAPI JSON 재생성

**Files:**
- Modify: `docs/openapi.json`

- [ ] **Step 1: 애플리케이션을 실행한다**

Run:

```bash
./gradlew bootRun
```

Expected:

```text
Started DcomIntranetServerApplication
```

Keep this process running until Step 3 is complete.

- [ ] **Step 2: 다른 터미널에서 OpenAPI JSON을 pretty format으로 저장한다**

Run:

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool > docs/openapi.json
```

Expected:

```text
```

The command writes formatted JSON to `docs/openapi.json`.

- [ ] **Step 3: 실행 중인 `bootRun`을 종료한다**

Press `Ctrl+C` in the terminal running `./gradlew bootRun`.

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: OpenAPI에 endpoint와 schema가 반영됐는지 확인한다**

Run:

```bash
rg '"/api/users/me/posts"|MyWrittenPostListApiResponse|MyWrittenPostListResponse|MyWrittenPostResponse|PageInfoResponse' docs/openapi.json
```

Expected:

```text
"/api/users/me/posts"
"MyWrittenPostListApiResponse"
"MyWrittenPostListResponse"
"MyWrittenPostResponse"
"PageInfoResponse"
```

- [ ] **Step 5: OpenAPI JSON formatting을 확인한다**

Run:

```bash
python3 -m json.tool docs/openapi.json > /tmp/my-written-posts-openapi-check.json
```

Expected:

```text
```

No parse error should be printed.

## Task 6: REVIEW - 최종 검증과 커밋 준비

**Files:**
- Review all modified files.

- [ ] **Step 1: git diff를 확인한다**

Run:

```bash
git diff -- src/main/java/com/dcom/intranet/mypage src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java docs/openapi.json
```

Expected:

```text
diff --git
```

Review checklist:

- `GET /api/users/me/posts`만 구현되어 있다.
- 상세 이동 API와 삭제 API가 production code에 없다.
- 성공 응답 `data`는 `postList`와 `pageInfo`를 포함한다.
- 실패 응답은 기존 `401` 공통 envelope를 사용한다.
- 명세서에 없는 `400`, `403`, `404` Swagger 응답을 추가하지 않았다.
- 다른 도메인 엔티티나 Repository를 새로 만들지 않았다.

- [ ] **Step 2: 최종 테스트를 다시 실행한다**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: 구현 커밋을 만든다**

Run:

```bash
git add src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java src/main/java/com/dcom/intranet/mypage/MyWrittenPostReaderConfig.java src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListResponse.java src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostResponse.java src/main/java/com/dcom/intranet/mypage/dto/PageInfoResponse.java src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostListApiResponse.java src/main/java/com/dcom/intranet/mypage/MyPageService.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java docs/openapi.json docs/superpowers/plans/2026-06-29-my-written-posts-list-api.md
git commit -m "feat: add my written posts list API"
```

Expected:

```text
The commit output contains "feat: add my written posts list API".
```

Do not stage unrelated existing changes:

- `docs/profile_update-openapi.json`
- `AGENTS.md`
- `docs/superpowers/plans/2026-06-28-member-profile-lookup.md`
- `mypage_front/`

## Self-Review

- Spec coverage: The plan implements `GET /api/users/me/posts`, `page`, `size`, `type`, `postList`, `pageInfo`, `200`, and `401`.
- Scope check: The plan excludes detail 이동, delete, comments API, and other domain entity creation.
- TDD check: Task 1 writes tests first and expects compilation failure before production code exists. Task 2 and Task 3 move from RED to GREEN with minimal implementation.
- Type consistency: `MyWrittenPostReader.read(Long userId, int page, int size, String type)` is used consistently by tests, service, and default implementation.
- OpenAPI check: Task 5 regenerates formatted `docs/openapi.json` and verifies the new endpoint and schemas.
