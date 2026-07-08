# 비밀번호 변경 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**목표:** 최종 API 명세서 기준으로 `PATCH /api/users/me/password` 비밀번호 변경 API를 구현한다.

**아키텍처:** 기존 마이페이지 컨트롤러, 서비스, DTO, 도메인 엔티티 패턴을 그대로 확장한다. 컨트롤러는 공통 `ApiResponse<T>` envelope를 반환하고, 서비스는 `PasswordEncoder`로 현재 비밀번호를 확인한 뒤 새 비밀번호를 BCrypt 해시로 저장한다.

**기술 스택:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Validation, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-06-29-password-change-design.md`
- 최종 API 명세서 기준: `PATCH /api/users/me/password`, 요청 `currentPassword`, `newPassword`, 응답 `message`, 상태코드 `200`, `400`, `401`
- 구현 범위: 비밀번호 변경 API만 포함한다.

## 파일 구조

- 수정: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 비밀번호 변경 API의 성공, 실패, 인증 실패 응답 테스트를 추가한다.
  - 테스트 fixture 사용자의 기본 비밀번호를 BCrypt 해시로 저장하도록 바꾼다.
- 생성: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java`
  - `currentPassword`, `newPassword` 요청 필드와 `@NotBlank` 검증을 정의한다.
- 생성: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java`
  - 성공 응답 `data.message` 구조를 정의한다.
- 생성: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- 수정: `src/main/java/com/dcom/intranet/user/User.java`
  - 해시된 새 비밀번호를 저장하는 도메인 메서드 하나를 추가한다.
- 수정: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - `PasswordEncoder`를 주입하고 비밀번호 변경 유스케이스를 구현한다.
- 수정: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `PATCH /api/users/me/password` endpoint와 Swagger response annotation을 추가한다.
- 수정: `docs/openapi.json`
  - 구현 후 `/v3/api-docs`에서 재생성해 비밀번호 변경 API schema를 반영한다.

## Task 1: RED - 비밀번호 변경 API 테스트 추가

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: 테스트에 `PasswordEncoder` import를 추가한다**

`src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`의 import 영역에 아래 import를 추가한다.

```java
import org.springframework.security.crypto.password.PasswordEncoder;
```

- [ ] **Step 2: 테스트 상수와 `PasswordEncoder` 주입 필드를 추가한다**

기존 상수 아래에 비밀번호 테스트용 상수를 추가한다.

```java
private static final String CURRENT_PASSWORD = "current-password";
private static final String NEW_PASSWORD = "changed-password";
```

기존 `EmailVerificationRepository` 주입 필드 아래에 `PasswordEncoder`를 추가한다.

```java
@Autowired
private PasswordEncoder passwordEncoder;
```

- [ ] **Step 3: 성공 및 실패 테스트를 추가한다**

`Profile settings update without token returns 401 common envelope` 테스트와 `saveUser` helper 사이에 아래 테스트를 추가한다.

```java
@Test
@DisplayName("Password change with correct current password returns 200 and stores encoded new password")
void passwordChangeWithCorrectCurrentPasswordReturns200AndStoresEncodedNewPassword() throws Exception {
    User user = saveUser("password1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/password")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "currentPassword": "%s",
                              "newPassword": "%s"
                            }
                            """.formatted(CURRENT_PASSWORD, NEW_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.message").value("비밀번호가 변경되었습니다."))
            .andExpect(jsonPath("$.data.password").doesNotExist())
            .andExpect(jsonPath("$.data.currentPassword").doesNotExist())
            .andExpect(jsonPath("$.data.newPassword").doesNotExist());

    User updatedUser = userRepository.findByLoginId("password1").orElseThrow();
    assertThat(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword())).isTrue();
    assertThat(passwordEncoder.matches(CURRENT_PASSWORD, updatedUser.getPassword())).isFalse();
    assertThat(updatedUser.getPassword()).isNotEqualTo(NEW_PASSWORD);
}

@Test
@DisplayName("Password change with wrong current password returns 400 common envelope")
void passwordChangeWithWrongCurrentPasswordReturns400CommonEnvelope() throws Exception {
    User user = saveUser("password2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/password")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "currentPassword": "wrong-password",
                              "newPassword": "%s"
                            }
                            """.formatted(NEW_PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("현재 비밀번호가 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Password change with blank current password returns 400 common envelope")
void passwordChangeWithBlankCurrentPasswordReturns400CommonEnvelope() throws Exception {
    User user = saveUser("password3", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/password")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "currentPassword": "",
                              "newPassword": "%s"
                            }
                            """.formatted(NEW_PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("요청값이 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Password change with blank new password returns 400 common envelope")
void passwordChangeWithBlankNewPasswordReturns400CommonEnvelope() throws Exception {
    User user = saveUser("password4", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/password")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "currentPassword": "%s",
                              "newPassword": ""
                            }
                            """.formatted(CURRENT_PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("요청값이 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

- [ ] **Step 4: `saveUser` helper가 BCrypt 해시를 저장하도록 바꾼다**

`saveUser` helper의 `User` 생성자 세 번째 인자를 아래처럼 변경한다.

```java
passwordEncoder.encode(CURRENT_PASSWORD),
```

- [ ] **Step 5: RED 테스트를 실행해 실패를 확인한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD FAILED
```

예상 실패 이유:

- 성공 테스트는 endpoint가 아직 없어서 `Status expected:<200> but was:<404>`로 실패한다.
- `400` 테스트들도 endpoint가 아직 없어서 기대한 `400` 대신 `404`로 실패한다.
- 컴파일 오류가 나면 production code를 작성하지 말고 테스트 코드 오타를 먼저 고친 뒤 같은 명령을 다시 실행한다.

## Task 2: GREEN - DTO와 도메인 메서드 추가

**Files:**
- Create: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java`
- Create: `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java`
- Modify: `src/main/java/com/dcom/intranet/user/User.java`

- [ ] **Step 1: `PasswordChangeRequest`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(
        @NotBlank
        @Schema(description = "현재 비밀번호", example = "current-password")
        String currentPassword,

        @NotBlank
        @Schema(description = "새 비밀번호", example = "changed-password")
        String newPassword
) {
}
```

- [ ] **Step 2: `PasswordChangeResponse`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 응답 데이터")
public record PasswordChangeResponse(
        @Schema(description = "처리 메시지", example = "비밀번호가 변경되었습니다.")
        String message
) {
}
```

- [ ] **Step 3: `PasswordChangeApiResponse`를 생성한다**

Create `src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 성공 응답")
public class PasswordChangeApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "비밀번호 변경 결과")
    public PasswordChangeResponse data;
}
```

- [ ] **Step 4: `User`에 비밀번호 변경 도메인 메서드를 추가한다**

`src/main/java/com/dcom/intranet/user/User.java`의 `changeEmail` 메서드 아래에 추가한다.

```java
public void changePassword(String encodedPassword) {
    this.password = encodedPassword;
}
```

- [ ] **Step 5: 컴파일을 실행한다**

Run:

```bash
./gradlew compileJava
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 3: GREEN - 서비스와 컨트롤러 최소 구현

**Files:**
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`

- [ ] **Step 1: `MyPageService` import를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageService.java`에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.PasswordChangeRequest;
import com.dcom.intranet.mypage.dto.PasswordChangeResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
```

- [ ] **Step 2: `MyPageService`에 `PasswordEncoder` 필드와 생성자 인자를 추가한다**

필드 영역을 아래처럼 만든다.

```java
private final UserRepository userRepository;
private final EmailVerificationService emailVerificationService;
private final PasswordEncoder passwordEncoder;
```

생성자를 아래 형태로 바꾼다.

```java
public MyPageService(
        UserRepository userRepository,
        EmailVerificationService emailVerificationService,
        PasswordEncoder passwordEncoder
) {
    this.userRepository = userRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordEncoder = passwordEncoder;
}
```

- [ ] **Step 3: `MyPageService`에 `changePassword`를 추가한다**

`updateMyProfile` 메서드 아래에 추가한다.

```java
@Transactional
public PasswordChangeResponse changePassword(String loginId, PasswordChangeRequest request) {
    User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
        throw new MyPageApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
    }

    user.changePassword(passwordEncoder.encode(request.newPassword()));
    return new PasswordChangeResponse("비밀번호가 변경되었습니다.");
}
```

- [ ] **Step 4: `MyPageController` import를 추가한다**

`src/main/java/com/dcom/intranet/mypage/MyPageController.java`에 아래 import를 추가한다.

```java
import com.dcom.intranet.mypage.dto.PasswordChangeApiResponse;
import com.dcom.intranet.mypage.dto.PasswordChangeRequest;
import com.dcom.intranet.mypage.dto.PasswordChangeResponse;
```

- [ ] **Step 5: `MyPageController`에 endpoint를 추가한다**

`updateMyProfile` 메서드 아래에 추가한다.

```java
@Operation(
        summary = "비밀번호 변경",
        description = "인증된 사용자의 현재 비밀번호를 확인한 뒤 새 비밀번호로 변경한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "비밀번호 변경 성공",
                        content = @Content(schema = @Schema(implementation = PasswordChangeApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "요청값 오류 또는 현재 비밀번호 불일치",
                        content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                )
        }
)
@PatchMapping("/me/password")
public ResponseEntity<ApiResponse<PasswordChangeResponse>> changePassword(
        Authentication authentication,
        @Valid @RequestBody PasswordChangeRequest request
) {
    PasswordChangeResponse response = myPageService.changePassword(authentication.getName(), request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 6: GREEN 테스트를 실행한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 4: 인증 실패 상태코드 테스트 추가

**Files:**
- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`

- [ ] **Step 1: 비밀번호 변경 API의 401 테스트를 추가한다**

비밀번호 변경 테스트 묶음 아래에 추가한다.

```java
@Test
@DisplayName("Password change without token returns 401 common envelope")
void passwordChangeWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(patch("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "currentPassword": "%s",
                              "newPassword": "%s"
                            }
                            """.formatted(CURRENT_PASSWORD, NEW_PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

- [ ] **Step 2: 마이페이지 컨트롤러 테스트를 실행한다**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected:

```text
BUILD SUCCESSFUL
```

설명: 이 테스트는 기존 `/api/users/me/**` 보안 규칙이 비밀번호 변경 endpoint에도 적용되는지 확인한다. 인증 실패 처리는 기존 공통 보안 흐름이 담당한다.

## Task 5: OpenAPI 문서 갱신

**Files:**
- Modify: `docs/openapi.json`

- [ ] **Step 1: 애플리케이션을 실행한다**

Run:

```bash
./gradlew bootRun
```

Expected:

```text
Tomcat started on port 8080
```

- [ ] **Step 2: 다른 터미널에서 OpenAPI JSON을 갱신한다**

Run:

```bash
curl -s http://localhost:8080/v3/api-docs -o docs/openapi.json
```

Expected:

```text
```

`curl` 명령은 성공 시 출력이 없다.

- [ ] **Step 3: OpenAPI 문서에 비밀번호 변경 endpoint가 들어갔는지 확인한다**

Run:

```bash
rg '"/api/users/me/password"|PasswordChangeApiResponse|PasswordChangeRequest|PasswordChangeResponse' docs/openapi.json
```

Expected:

```text
"/api/users/me/password"
"PasswordChangeApiResponse"
"PasswordChangeRequest"
"PasswordChangeResponse"
```

- [ ] **Step 4: 실행 중인 `bootRun`을 종료한다**

`./gradlew bootRun`을 실행한 터미널에서 `Ctrl+C`를 입력한다.

Expected:

```text
BUILD SUCCESSFUL
```

## Task 6: 전체 검증 및 커밋

**Files:**
- Verify all modified files

- [ ] **Step 1: 전체 테스트를 실행한다**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 2: 변경 파일을 확인한다**

Run:

```bash
git status --short
```

Expected includes:

```text
 M docs/superpowers/specs/2026-06-29-password-change-design.md
 M src/main/java/com/dcom/intranet/mypage/MyPageController.java
 M src/main/java/com/dcom/intranet/mypage/MyPageService.java
 M src/main/java/com/dcom/intranet/user/User.java
 M src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java
?? docs/openapi.json
?? docs/superpowers/plans/2026-06-29-password-change-api.md
?? src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java
?? src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java
?? src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java
```

주의: 이미 존재하던 다른 미추적 파일이나 삭제 파일은 이번 작업에서 만든 변경이 아니면 stage하지 않는다.

- [ ] **Step 3: 이번 작업 파일만 stage한다**

Run:

```bash
git add docs/superpowers/specs/2026-06-29-password-change-design.md docs/superpowers/plans/2026-06-29-password-change-api.md docs/openapi.json src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/MyPageService.java src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java src/main/java/com/dcom/intranet/user/User.java src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java
```

Expected:

```text
```

`git add`는 성공 시 출력이 없다.

- [ ] **Step 4: staged diff를 확인한다**

Run:

```bash
git diff --cached --stat
```

Expected includes:

```text
docs/superpowers/plans/2026-06-29-password-change-api.md
docs/superpowers/specs/2026-06-29-password-change-design.md
docs/openapi.json
src/main/java/com/dcom/intranet/mypage/MyPageController.java
src/main/java/com/dcom/intranet/mypage/MyPageService.java
src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeApiResponse.java
src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeRequest.java
src/main/java/com/dcom/intranet/mypage/dto/PasswordChangeResponse.java
src/main/java/com/dcom/intranet/user/User.java
src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java
```

- [ ] **Step 5: 커밋한다**

Run:

```bash
git commit -m "feat: add password change API"
```

Expected:

```text
[feature/mypage <sha>] feat: add password change API
```

## Self-Review Checklist

- 설계 문서의 범위와 계획의 작업 범위가 일치한다.
- `PATCH /api/users/me/password` 외의 새 API를 추가하지 않는다.
- 요청 필드는 `currentPassword`, `newPassword`뿐이다.
- `newPasswordConfirm`은 서버 요청 필드로 추가하지 않는다.
- 비밀번호 복잡도 정책은 `@NotBlank`를 넘어서 추가하지 않는다.
- `200`, `400`, `401` 상태코드가 테스트와 Swagger response annotation에 모두 반영된다.
- 성공 응답은 `ApiResponse<PasswordChangeResponse>`이고 `data.message`가 보인다.
- 실패 응답은 기존 `ApiResponse.failure` 구조와 nullable `data`를 유지한다.
- 테스트 fixture의 기본 비밀번호는 BCrypt 해시로 저장된다.
- 현재 비밀번호 불일치는 `현재 비밀번호가 올바르지 않습니다.` 메시지로 `400`을 반환한다.
- 전체 테스트 명령 `./gradlew test`가 통과한 뒤에만 완료로 판단한다.
