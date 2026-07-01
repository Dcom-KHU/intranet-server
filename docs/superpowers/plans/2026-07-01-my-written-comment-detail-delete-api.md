# My Written Comment Detail And Delete API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `GET /api/users/me/comments/{commentId}` and `DELETE /api/users/me/comments/{commentId}` for mypage, following the final API spec and current mypage response/type conventions.

**Architecture:** Keep mypage as an orchestration layer. `MyPageController` exposes the endpoints, `MyPageService` authenticates the current user by `loginId` and normalizes `type` with `MyPageRouteType`, and `MyWrittenCommentReader` remains the boundary to the future information-board/photo-album comment domains.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## Reference Documents

- Design spec: `docs/superpowers/specs/2026-07-01-my-written-comment-detail-delete-design.md`
- Final API spec:
  - `GET /api/users/me/comments/{commentId}` with `commentId`, `type`, response `targetType`, `targetId`, `commentId`, status `200`, `401`, `404`
  - `DELETE /api/users/me/comments/{commentId}` with `commentId`, `type`, response `message`, status `200`, `401`, `403`, `404`
- Current mypage conventions:
  - Success message: `요청에 성공했습니다.`
  - Routing type values exposed to frontend: `info-posts`, `archives`, `photo-posts`
  - Type normalization lives in `src/main/java/com/dcom/intranet/mypage/MyPageRouteType.java`
  - Comment list already returns `data.total` and `data.comments`

## File Structure

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - Add RED tests for comment target navigation and comment deletion.
  - Extend the in-test `TestMyWrittenCommentReader` to capture `commentId`, return target/delete DTOs, and simulate `403`/`404`.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetResponse.java`
  - Success `data` shape for comment detail navigation.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteResponse.java`
  - Success `data` shape for comment deletion.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetApiResponse.java`
  - Swagger wrapper for `200` comment detail-navigation response.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteApiResponse.java`
  - Swagger wrapper for `200` comment deletion response.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java`
  - Add `readTarget` and `delete` methods.
- Modify: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java`
  - Return `404` for target/delete until real comment-domain implementation is connected.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - Add service methods and normalize `type` before calling the port.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - Add `GET` and `DELETE` endpoints with Swagger response annotations.
- Modify: `docs/openapi.json`
  - Add `/api/users/me/comments/{commentId}` operations and new schemas with readable formatting.

## Task 1: RED - Add Controller Tests For Comment Detail Navigation And Delete

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: Add DTO imports for the new response types**

Add these imports near the existing mypage DTO imports:

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;
```

- [ ] **Step 2: Add comment target navigation tests**

Insert these tests after `myWrittenCommentsListWithPendingUserTokenReturns401CommonEnvelope()` and before the existing written-post target tests:

```java
    @Test
    @DisplayName("My written comment detail target returns info-posts route target")
    void myWrittenCommentDetailTargetReturnsInfoPostsRouteTarget() throws Exception {
        User user = saveUser("commentTargetInfo", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenTargetResponse(new MyWrittenCommentTargetResponse("INFO_POST", 12L, 101L));

        mockMvc.perform(get("/api/users/me/comments/101")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "INFO_POST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.targetType").value("info-posts"))
                .andExpect(jsonPath("$.data.targetId").value(12))
                .andExpect(jsonPath("$.data.commentId").value(101));

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastCommentId()).isEqualTo(101L);
        assertThat(myWrittenCommentReader.lastType()).isEqualTo("info-posts");
    }

    @Test
    @DisplayName("My written comment detail target returns photo-posts route target")
    void myWrittenCommentDetailTargetReturnsPhotoPostsRouteTarget() throws Exception {
        User user = saveUser("commentTargetPhoto", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenTargetResponse(new MyWrittenCommentTargetResponse("PHOTO_ALBUM", 13L, 102L));

        mockMvc.perform(get("/api/users/me/comments/102")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "PHOTO_ALBUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.targetType").value("photo-posts"))
                .andExpect(jsonPath("$.data.targetId").value(13))
                .andExpect(jsonPath("$.data.commentId").value(102));

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastCommentId()).isEqualTo(102L);
        assertThat(myWrittenCommentReader.lastType()).isEqualTo("photo-posts");
    }

    @Test
    @DisplayName("My written comment detail target without token returns 401 common envelope")
    void myWrittenCommentDetailTargetWithoutTokenReturns401CommonEnvelope() throws Exception {
        mockMvc.perform(get("/api/users/me/comments/101")
                        .param("type", "info-posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written comment detail target not found returns 404 common envelope")
    void myWrittenCommentDetailTargetNotFoundReturns404CommonEnvelope() throws Exception {
        User user = saveUser("commentTargetMissing", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenTargetNotFound();

        mockMvc.perform(get("/api/users/me/comments/999")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("작성한 댓글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
```

- [ ] **Step 3: Add comment delete tests**

Insert these tests after the comment target tests from Step 2:

```java
    @Test
    @DisplayName("My written comment delete returns 200 common envelope and message")
    void myWrittenCommentDeleteReturns200CommonEnvelopeAndMessage() throws Exception {
        User user = saveUser("commentDeleteInfo", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenDeleteResponse(new MyWrittenCommentDeleteResponse("작성한 댓글이 삭제되었습니다."));

        mockMvc.perform(delete("/api/users/me/comments/201")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "INFO_POST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.message").value("작성한 댓글이 삭제되었습니다."));

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastCommentId()).isEqualTo(201L);
        assertThat(myWrittenCommentReader.lastType()).isEqualTo("info-posts");
    }

    @Test
    @DisplayName("My written comment delete passes photo-posts type")
    void myWrittenCommentDeletePassesPhotoPostsType() throws Exception {
        User user = saveUser("commentDeletePhoto", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(delete("/api/users/me/comments/202")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "PHOTO_ALBUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.message").value("작성한 댓글이 삭제되었습니다."));

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastCommentId()).isEqualTo(202L);
        assertThat(myWrittenCommentReader.lastType()).isEqualTo("photo-posts");
    }

    @Test
    @DisplayName("My written comment delete without token returns 401 common envelope")
    void myWrittenCommentDeleteWithoutTokenReturns401CommonEnvelope() throws Exception {
        mockMvc.perform(delete("/api/users/me/comments/201")
                        .param("type", "info-posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written comment delete forbidden returns 403 common envelope")
    void myWrittenCommentDeleteForbiddenReturns403CommonEnvelope() throws Exception {
        User user = saveUser("commentDeleteForbidden", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenDeleteForbidden();

        mockMvc.perform(delete("/api/users/me/comments/203")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written comment delete not found returns 404 common envelope")
    void myWrittenCommentDeleteNotFoundReturns404CommonEnvelope() throws Exception {
        User user = saveUser("commentDeleteMissing", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenCommentReader.givenDeleteNotFound();

        mockMvc.perform(delete("/api/users/me/comments/999")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("작성한 댓글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
```

- [ ] **Step 4: Extend `TestMyWrittenCommentReader` for the new tests**

Replace the current `TestMyWrittenCommentReader` class body with:

```java
    static class TestMyWrittenCommentReader implements MyWrittenCommentReader {

        private MyWrittenCommentListResponse response = MyWrittenCommentListResponse.empty(0, 10);
        private MyWrittenCommentTargetResponse targetResponse = new MyWrittenCommentTargetResponse("info-posts", 1L, 1L);
        private MyWrittenCommentDeleteResponse deleteResponse = new MyWrittenCommentDeleteResponse("작성한 댓글이 삭제되었습니다.");
        private boolean targetNotFound;
        private boolean deleteForbidden;
        private boolean deleteNotFound;
        private Long lastUserId;
        private Long lastCommentId;
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

        @Override
        public MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type) {
            this.lastUserId = userId;
            this.lastCommentId = commentId;
            this.lastType = type;
            if (targetNotFound) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
            }
            return targetResponse;
        }

        @Override
        public MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type) {
            this.lastUserId = userId;
            this.lastCommentId = commentId;
            this.lastType = type;
            if (deleteForbidden) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
            }
            if (deleteNotFound) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
            }
            return deleteResponse;
        }

        void givenResponse(MyWrittenCommentListResponse response) {
            this.response = response;
        }

        void givenTargetResponse(MyWrittenCommentTargetResponse targetResponse) {
            this.targetResponse = targetResponse;
            this.targetNotFound = false;
        }

        void givenTargetNotFound() {
            this.targetNotFound = true;
        }

        void givenDeleteResponse(MyWrittenCommentDeleteResponse deleteResponse) {
            this.deleteResponse = deleteResponse;
            this.deleteForbidden = false;
            this.deleteNotFound = false;
        }

        void givenDeleteForbidden() {
            this.deleteForbidden = true;
            this.deleteNotFound = false;
        }

        void givenDeleteNotFound() {
            this.deleteForbidden = false;
            this.deleteNotFound = true;
        }

        void reset() {
            this.response = MyWrittenCommentListResponse.empty(0, 10);
            this.targetResponse = new MyWrittenCommentTargetResponse("info-posts", 1L, 1L);
            this.deleteResponse = new MyWrittenCommentDeleteResponse("작성한 댓글이 삭제되었습니다.");
            this.targetNotFound = false;
            this.deleteForbidden = false;
            this.deleteNotFound = false;
            this.lastUserId = null;
            this.lastCommentId = null;
            this.lastPage = -1;
            this.lastSize = -1;
            this.lastType = null;
        }

        Long lastUserId() {
            return lastUserId;
        }

        Long lastCommentId() {
            return lastCommentId;
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

- [ ] **Step 5: Add student IDs for new test users**

In `studentIdFor(String loginId)`, add these cases before the `default` case:

```java
            case "commentTargetInfo" -> "20240125";
            case "commentTargetPhoto" -> "20240126";
            case "commentTargetMissing" -> "20240127";
            case "commentDeleteInfo" -> "20240128";
            case "commentDeletePhoto" -> "20240129";
            case "commentDeleteForbidden" -> "20240130";
            case "commentDeleteMissing" -> "20240131";
```

- [ ] **Step 6: Run the focused tests and verify RED**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL during compilation because `MyWrittenCommentTargetResponse`, `MyWrittenCommentDeleteResponse`, `readTarget`, and `delete` do not exist yet. A valid RED failure contains one or more of:

```text
cannot find symbol
method does not override or implement a method from a supertype
```

## Task 2: GREEN - Add Comment Target/Delete Contract And Endpoints

**Files:**
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetApiResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteApiResponse.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: Create comment target response DTO**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.mypage.MyPageRouteType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 상세 이동 응답 데이터")
public record MyWrittenCommentTargetResponse(
        @Schema(description = "상세 페이지 이동 대상 URL segment", example = "info-posts")
        String targetType,

        @Schema(description = "상세 페이지 이동 대상 ID", example = "12")
        Long targetId,

        @Schema(description = "댓글 ID", example = "101")
        Long commentId
) {
    public MyWrittenCommentTargetResponse {
        targetType = MyPageRouteType.normalize(targetType);
    }
}
```

- [ ] **Step 2: Create comment delete response DTO**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 삭제 응답 데이터")
public record MyWrittenCommentDeleteResponse(
        @Schema(description = "처리 메시지", example = "작성한 댓글이 삭제되었습니다.")
        String message
) {
}
```

- [ ] **Step 3: Create Swagger wrapper for comment target**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 상세 이동 성공 응답")
public class MyWrittenCommentTargetApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    public String message;

    @Schema(description = "내가 쓴 댓글 상세 이동 대상")
    public MyWrittenCommentTargetResponse data;
}
```

- [ ] **Step 4: Create Swagger wrapper for comment delete**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 삭제 성공 응답")
public class MyWrittenCommentDeleteApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    public String message;

    @Schema(description = "내가 쓴 댓글 삭제 결과")
    public MyWrittenCommentDeleteResponse data;
}
```

- [ ] **Step 5: Extend `MyWrittenCommentReader`**

Replace `src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java` with:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;

public interface MyWrittenCommentReader {

    MyWrittenCommentListResponse read(Long userId, int page, int size, String type);

    MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type);

    MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type);
}
```

- [ ] **Step 6: Extend `EmptyMyWrittenCommentReader`**

Replace `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java` with:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;
import org.springframework.http.HttpStatus;

public class EmptyMyWrittenCommentReader implements MyWrittenCommentReader {

    @Override
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenCommentListResponse.empty(page, size);
    }

    @Override
    public MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
    }

    @Override
    public MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
    }
}
```

- [ ] **Step 7: Add imports to `MyPageService`**

Add these imports in `src/main/java/com/dcom/intranet/mypage/MyPageService.java`:

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;
```

- [ ] **Step 8: Add service methods**

In `MyPageService`, add these methods after `getMyComments(...)` and before `getMyPostTarget(...)`:

```java
    @Transactional(readOnly = true)
    public MyWrittenCommentTargetResponse getMyCommentTarget(String loginId, Long commentId, String type) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return myWrittenCommentReader.readTarget(user.getId(), commentId, MyPageRouteType.normalize(type));
    }

    @Transactional
    public MyWrittenCommentDeleteResponse deleteMyComment(String loginId, Long commentId, String type) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return myWrittenCommentReader.delete(user.getId(), commentId, MyPageRouteType.normalize(type));
    }
```

- [ ] **Step 9: Add imports to `MyPageController`**

Add these imports in `src/main/java/com/dcom/intranet/mypage/MyPageController.java`:

```java
import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;
```

- [ ] **Step 10: Add controller endpoints**

In `MyPageController`, add these methods after `getMyComments(...)` and before `getMyPostTarget(...)`:

```java
    @Operation(
            summary = "내가 쓴 댓글 상세 이동",
            description = "인증된 사용자가 본인이 작성한 댓글을 선택하면 원본 상세 페이지 이동 대상 정보를 반환한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 댓글 상세 이동 대상 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenCommentTargetApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "작성한 댓글 없음",
                            content = @Content(schema = @Schema(implementation = NotFoundApiResponse.class))
                    )
            }
    )
    @GetMapping("/me/comments/{commentId}")
    public ResponseEntity<ApiResponse<MyWrittenCommentTargetResponse>> getMyCommentTarget(
            Authentication authentication,
            @PathVariable Long commentId,
            @Parameter(description = "라우팅 타입: info-posts, photo-posts")
            @RequestParam String type
    ) {
        MyWrittenCommentTargetResponse response = myPageService.getMyCommentTarget(
                authentication.getName(),
                commentId,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 쓴 댓글 삭제",
            description = "인증된 사용자가 본인이 작성한 댓글을 삭제한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 댓글 삭제 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenCommentDeleteApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "삭제 권한 없음",
                            content = @Content(schema = @Schema(implementation = ForbiddenApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "작성한 댓글 없음",
                            content = @Content(schema = @Schema(implementation = NotFoundApiResponse.class))
                    )
            }
    )
    @DeleteMapping("/me/comments/{commentId}")
    public ResponseEntity<ApiResponse<MyWrittenCommentDeleteResponse>> deleteMyComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @Parameter(description = "라우팅 타입: info-posts, photo-posts")
            @RequestParam String type
    ) {
        MyWrittenCommentDeleteResponse response = myPageService.deleteMyComment(
                authentication.getName(),
                commentId,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
```

- [ ] **Step 11: Run focused tests and verify GREEN**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS with:

```text
BUILD SUCCESSFUL
```

## Task 3: DOCS - Update OpenAPI JSON

**Files:**
- Modify: `docs/openapi.json`
- Test: `docs/openapi.json`

- [ ] **Step 1: Start the app to expose `/v3/api-docs`**

Run:

```bash
./gradlew bootRun
```

Expected: the server starts and logs a line containing:

```text
Started DcomIntranetServerApplication
```

Keep this process running until Step 2 finishes.

- [ ] **Step 2: Regenerate formatted OpenAPI JSON from another terminal/session**

Run:

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool > docs/openapi.json
```

Expected: command exits with status `0` and rewrites `docs/openapi.json` with indented JSON.

- [ ] **Step 3: Stop the bootRun process**

Stop the running `./gradlew bootRun` process with `Ctrl-C`.

Expected: the server process exits cleanly.

- [ ] **Step 4: Verify the new OpenAPI path and schemas exist**

Run:

```bash
rg '"/api/users/me/comments/\\{commentId\\}"|MyWrittenCommentTargetApiResponse|MyWrittenCommentDeleteApiResponse|MyWrittenCommentTargetResponse|MyWrittenCommentDeleteResponse' docs/openapi.json
```

Expected output includes all of:

```text
"/api/users/me/comments/{commentId}"
MyWrittenCommentTargetApiResponse
MyWrittenCommentDeleteApiResponse
MyWrittenCommentTargetResponse
MyWrittenCommentDeleteResponse
```

- [ ] **Step 5: Verify OpenAPI JSON syntax**

Run:

```bash
node -e 'JSON.parse(require("fs").readFileSync("docs/openapi.json", "utf8")); console.log("openapi json ok")'
```

Expected:

```text
openapi json ok
```

## Task 4: VERIFY - Run Tests And Review Diff

**Files:**
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
- Test: `docs/openapi.json`

- [ ] **Step 1: Run the mypage controller test suite**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 2: Run the full test suite**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: Review changed files**

Run:

```bash
git diff -- src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/mypage src/main/java/com/dcom/intranet/mypage/dto docs/openapi.json
```

Expected: diff only contains the comment detail/delete API implementation, tests, DTOs, and OpenAPI updates. It must not include unrelated edits to user-owned files such as:

```text
docs/profile_update-openapi.json
AGENTS.md
docs/superpowers/plans/2026-06-28-member-profile-lookup.md
mypage_front/
```

- [ ] **Step 4: Review status before commit**

Run:

```bash
git status --short
```

Expected: implementation files are modified/untracked, and the pre-existing unrelated user changes may still appear. Only the files from this plan should be staged in the next task.

## Task 5: COMMIT - Commit The Implementation

**Files:**
- Stage only the files changed by this plan.

- [ ] **Step 1: Stage implementation files**

Run:

```bash
git add \
  src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java \
  src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java \
  src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java \
  src/main/java/com/dcom/intranet/mypage/MyPageService.java \
  src/main/java/com/dcom/intranet/mypage/MyPageController.java \
  src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetResponse.java \
  src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteResponse.java \
  src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetApiResponse.java \
  src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteApiResponse.java \
  docs/openapi.json \
  docs/superpowers/plans/2026-07-01-my-written-comment-detail-delete-api.md
```

Expected: command exits with status `0`.

- [ ] **Step 2: Confirm staged files**

Run:

```bash
git diff --cached --name-only
```

Expected output:

```text
docs/openapi.json
docs/superpowers/plans/2026-07-01-my-written-comment-detail-delete-api.md
src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenCommentReader.java
src/main/java/com/dcom/intranet/mypage/MyPageController.java
src/main/java/com/dcom/intranet/mypage/MyPageService.java
src/main/java/com/dcom/intranet/mypage/MyWrittenCommentReader.java
src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteApiResponse.java
src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentDeleteResponse.java
src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetApiResponse.java
src/main/java/com/dcom/intranet/mypage/dto/MyWrittenCommentTargetResponse.java
src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java
```

- [ ] **Step 3: Commit implementation**

Run:

```bash
git commit -m "feat: add my written comment detail and delete API"
```

Expected: commit succeeds and output contains:

```text
feat: add my written comment detail and delete API
```

## Self-Review Checklist

- Spec coverage: The plan covers both endpoints, `200`/`401`/`403`/`404` where specified, routing type normalization, response DTOs, tests, OpenAPI, and no implementation of external comment domains.
- Placeholder scan: Each task has concrete files, code, commands, and expected results.
- Type consistency: `commentId`, `targetType`, `targetId`, and `message` names match the design spec and controller test expectations. `MyWrittenCommentTargetResponse` normalizes route type the same way `MyWrittenPostTargetResponse` does.
