# 회원 탈퇴 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 원클릭 방식의 `PATCH /api/users/me/withdraw` 회원 탈퇴 API를 구현한다.

**Architecture:** Controller는 인증 사용자만 받아 공통 응답을 반환하고, Service는 login ID로 사용자를 조회해 `User.withdraw(LocalDateTime)`만 호출한다. `User` 엔티티가 `WITHDRAWN` 상태와 `withdrawnAt` 저장 책임을 가진다.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-07-01-member-withdraw-design.md`
- API 계약: `PATCH /api/users/me/withdraw`, request body 없음, response data `userId`, `status`, `withdrawnAt`
- 상태코드: `200`, `401`
- 제외 범위: 비밀번호 재확인, `400`, refresh token 무효화, 2년 후 삭제 배치, 재가입 정책

## 파일 구조

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 원클릭 탈퇴 성공, 탈퇴 후 재접근 실패, 토큰 없음, 이미 탈퇴한 사용자 접근 실패 테스트를 추가한다.
- Modify: `src/main/java/com/dcom/intranet/user/User.java`
  - `withdrawnAt` 필드, getter, `withdraw(LocalDateTime)` 메서드를 추가한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawResponse.java`
  - 성공 응답 `data` 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - `withdraw(String loginId)`를 추가한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `PATCH /api/users/me/withdraw` endpoint와 Swagger response annotation을 추가한다.
- Modify: `docs/openapi.json`
  - 회원 탈퇴 API operation과 schema를 반영한다.

## Task 1: RED - 회원 탈퇴 API 테스트 추가

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: Write failing tests**

Add this import:

```java
import com.dcom.intranet.mypage.dto.MemberWithdrawResponse;
```

Add these tests after the password change tests and before the my written posts tests:

```java
@Test
@DisplayName("Member withdraw returns 200 and stores WITHDRAWN status")
void memberWithdrawReturns200AndStoresWithdrawnStatus() throws Exception {
    User user = saveUser("withdraw1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/withdraw")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.userId").value(user.getId()))
            .andExpect(jsonPath("$.data.status").value("WITHDRAWN"))
            .andExpect(jsonPath("$.data.withdrawnAt").isNotEmpty());

    User withdrawnUser = userRepository.findByLoginId("withdraw1").orElseThrow();
    assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    assertThat(withdrawnUser.getWithdrawnAt()).isNotNull();
}

@Test
@DisplayName("Member withdraw makes the same token unauthorized afterwards")
void memberWithdrawMakesSameTokenUnauthorizedAfterwards() throws Exception {
    User user = saveUser("withdraw2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/withdraw")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk());

    mockMvc.perform(get("/api/users/me")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Member withdraw without token returns 401 common envelope")
void memberWithdrawWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(patch("/api/users/me/withdraw"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Member withdraw with WITHDRAWN user token returns 401 common envelope")
void memberWithdrawWithWithdrawnUserTokenReturns401CommonEnvelope() throws Exception {
    User user = saveUser("withdrawnUser", UserStatus.WITHDRAWN, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/withdraw")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

Extend `studentIdFor`:

```java
case "withdraw1" -> "20240021";
case "withdraw2" -> "20240022";
case "withdrawnUser" -> "20240023";
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```bash
./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL because `MemberWithdrawResponse` and `User#getWithdrawnAt()` do not exist, and `/api/users/me/withdraw` is not implemented.

## Task 2: GREEN - 최소 구현 추가

**Files:**
- Modify: `src/main/java/com/dcom/intranet/user/User.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawApiResponse.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: Add withdraw state to User**

In `User`, add the field near `lastLoginAt`:

```java
@Column(name = "withdrawn_at")
private LocalDateTime withdrawnAt;
```

Add the getter near `getLastLoginAt()`:

```java
public LocalDateTime getWithdrawnAt() {
    return withdrawnAt;
}
```

Add the state change method near the other mutation methods:

```java
public void withdraw(LocalDateTime withdrawnAt) {
    this.status = UserStatus.WITHDRAWN;
    this.withdrawnAt = withdrawnAt;
}
```

- [ ] **Step 2: Add response DTOs**

Create `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "회원 탈퇴 응답 데이터")
public record MemberWithdrawResponse(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "회원 상태", example = "WITHDRAWN")
        UserStatus status,

        @Schema(description = "탈퇴 일시", example = "2026-07-01T10:30:00")
        LocalDateTime withdrawnAt
) {
    public static MemberWithdrawResponse from(User user) {
        return new MemberWithdrawResponse(user.getId(), user.getStatus(), user.getWithdrawnAt());
    }
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 성공 응답")
public class MemberWithdrawApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "회원 탈퇴 결과")
    public MemberWithdrawResponse data;
}
```

- [ ] **Step 3: Add service method**

In `MyPageService`, add this import:

```java
import com.dcom.intranet.mypage.dto.MemberWithdrawResponse;
```

Add this method after `changePassword`:

```java
@Transactional
public MemberWithdrawResponse withdraw(String loginId) {
    User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));

    user.withdraw(LocalDateTime.now());
    return MemberWithdrawResponse.from(user);
}
```

If `LocalDateTime` is not already imported in `MyPageService`, keep the existing import:

```java
import java.time.LocalDateTime;
```

- [ ] **Step 4: Add controller endpoint**

In `MyPageController`, add these imports:

```java
import com.dcom.intranet.mypage.dto.MemberWithdrawApiResponse;
import com.dcom.intranet.mypage.dto.MemberWithdrawResponse;
```

Add this endpoint after `changePassword`:

```java
@Operation(
        summary = "회원 탈퇴",
        description = "인증된 사용자의 회원 상태를 WITHDRAWN으로 변경한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "회원 탈퇴 성공",
                        content = @Content(schema = @Schema(implementation = MemberWithdrawApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                )
        }
)
@PatchMapping("/me/withdraw")
public ResponseEntity<ApiResponse<MemberWithdrawResponse>> withdraw(Authentication authentication) {
    MemberWithdrawResponse response = myPageService.withdraw(authentication.getName());
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 5: Run targeted tests to verify GREEN**

Run:

```bash
./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS.

## Task 3: OpenAPI 반영과 전체 검증

**Files:**
- Modify: `docs/openapi.json`
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: Add OpenAPI path and schemas**

Update `docs/openapi.json` so it contains:

```json
"/api/users/me/withdraw": {
  "patch": {
    "tags": [
      "my-page-controller"
    ],
    "summary": "회원 탈퇴",
    "description": "인증된 사용자의 회원 상태를 WITHDRAWN으로 변경한다.",
    "operationId": "withdraw",
    "responses": {
      "200": {
        "description": "회원 탈퇴 성공",
        "content": {
          "*/*": {
            "schema": {
              "$ref": "#/components/schemas/MemberWithdrawApiResponse"
            }
          }
        }
      },
      "401": {
        "description": "인증 실패",
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

Add schemas:

```json
"MemberWithdrawApiResponse": {
  "type": "object",
  "description": "회원 탈퇴 성공 응답",
  "properties": {
    "success": {
      "type": "boolean",
      "description": "요청 성공 여부",
      "example": true
    },
    "status": {
      "type": "integer",
      "format": "int32",
      "description": "HTTP 상태 코드",
      "example": 200
    },
    "message": {
      "type": "string",
      "description": "응답 메시지",
      "example": "요청이 성공적으로 처리되었습니다."
    },
    "data": {
      "$ref": "#/components/schemas/MemberWithdrawResponse"
    }
  }
},
"MemberWithdrawResponse": {
  "type": "object",
  "description": "회원 탈퇴 응답 데이터",
  "properties": {
    "userId": {
      "type": "integer",
      "format": "int64",
      "description": "회원 ID",
      "example": 1
    },
    "status": {
      "type": "string",
      "description": "회원 상태",
      "enum": [
        "PENDING",
        "APPROVED",
        "WITHDRAWN"
      ],
      "example": "WITHDRAWN"
    },
    "withdrawnAt": {
      "type": "string",
      "format": "date-time",
      "description": "탈퇴 일시",
      "example": "2026-07-01T10:30:00"
    }
  }
}
```

- [ ] **Step 2: Validate OpenAPI JSON formatting**

Run:

```bash
node -e 'JSON.parse(require("fs").readFileSync("docs/openapi.json", "utf8")); console.log("openapi json ok")'
```

Expected: `openapi json ok`.

- [ ] **Step 3: Run full test suite**

Run:

```bash
./gradlew test --rerun-tasks
```

Expected: PASS.

## Task 4: Review and commit implementation

**Files:**
- Review: all modified files

- [ ] **Step 1: Review diff for scope**

Run:

```bash
git diff -- src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/user/User.java src/main/java/com/dcom/intranet/mypage/MyPageService.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawResponse.java src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawApiResponse.java docs/openapi.json
```

Confirm:

- No password request body was added.
- No `400` response was added for withdraw.
- `WITHDRAWN` is stored on the user.
- `withdrawnAt` is stored and returned.
- `200` and `401` are covered by tests and OpenAPI.

- [ ] **Step 2: Commit implementation**

Run:

```bash
git add src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/user/User.java src/main/java/com/dcom/intranet/mypage/MyPageService.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawResponse.java src/main/java/com/dcom/intranet/mypage/dto/MemberWithdrawApiResponse.java docs/openapi.json
git commit -m "feat: add member withdraw API"
```

Expected: commit succeeds with only the member withdraw API implementation.

