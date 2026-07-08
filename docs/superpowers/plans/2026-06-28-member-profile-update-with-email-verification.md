# Member Profile Update With Email Verification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the mypage email-change verification flow and member profile update API in the safe order: send verification, verify code, then apply profile settings.

**Architecture:** Keep the existing mypage controller/service style and add a focused `EmailVerification` domain model under the mypage package. All APIs remain authenticated by the existing JWT filter, return the existing `ApiResponse<T>` envelope, and use a small global exception handler for `400`, `409`, `410`, and `429`. Actual email delivery is intentionally excluded until SMTP or a mail provider is selected.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, Jakarta Validation, Springdoc OpenAPI, JUnit 5, MockMvc.

---

## File Structure

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - Add MockMvc integration tests for the three new APIs.
- Create: `src/main/java/com/dcom/intranet/common/GlobalExceptionHandler.java`
  - Convert validation and mypage API exceptions to the common response envelope.
- Create: `src/main/java/com/dcom/intranet/mypage/MyPageApiException.java`
  - Carry the HTTP status and message for mypage domain failures.
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerification.java`
  - Persist email verification requests, codes, tokens, expiry, verified state, and used state.
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`
  - Query latest requests by user/email and query by email-change token.
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java`
  - Generate codes, enforce active-request `429`, verify codes, issue email-change tokens, and consume tokens.
- Modify: `src/main/java/com/dcom/intranet/user/User.java`
  - Add profile and email update methods.
- Modify: `src/main/java/com/dcom/intranet/user/UserRepository.java`
  - Add `existsByEmail`.
- Modify: `src/main/java/com/dcom/intranet/config/SecurityConfig.java`
  - Protect `/api/users/me/**` with USER or ADMIN role.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - Add send, verify, and settings endpoints plus Swagger response schemas.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - Add settings update orchestration.
- Create DTO files under `src/main/java/com/dcom/intranet/mypage/dto/`
  - `EmailVerificationSendRequest.java`
  - `EmailVerificationSendResponse.java`
  - `EmailVerificationVerifyRequest.java`
  - `EmailVerificationVerifyResponse.java`
  - `MyProfileUpdateRequest.java`
  - `MyProfileUpdateResponse.java`
  - `EmailVerificationSendApiResponse.java`
  - `EmailVerificationVerifyApiResponse.java`
  - `MyProfileUpdateApiResponse.java`
  - `BadRequestApiResponse.java`
  - `ConflictApiResponse.java`
  - `GoneApiResponse.java`
  - `TooManyRequestsApiResponse.java`

## Shared Constants

Use these response messages consistently in tests and implementation:

```java
private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";
private static final String BAD_REQUEST_MESSAGE = "요청값이 올바르지 않습니다.";
private static final String EMAIL_DUPLICATE_MESSAGE = "이미 사용 중인 이메일입니다.";
private static final String EMAIL_VERIFICATION_ACTIVE_MESSAGE = "이미 진행 중인 이메일 인증 요청이 있습니다.";
private static final String EMAIL_VERIFICATION_NOT_FOUND_MESSAGE = "이메일 인증 요청을 찾을 수 없습니다.";
private static final String EMAIL_VERIFICATION_CODE_MISMATCH_MESSAGE = "인증 코드가 올바르지 않습니다.";
private static final String EMAIL_VERIFICATION_EXPIRED_MESSAGE = "이메일 인증이 만료되었습니다.";
private static final String EMAIL_CHANGE_TOKEN_INVALID_MESSAGE = "이메일 변경 토큰이 올바르지 않습니다.";
private static final String EMAIL_VERIFICATION_CREATED_MESSAGE = "이메일 변경 인증 코드가 생성되었습니다.";
private static final String EMAIL_VERIFICATION_COMPLETED_MESSAGE = "이메일 변경 인증이 완료되었습니다.";
private static final String PROFILE_UPDATED_MESSAGE = "회원정보가 수정되었습니다.";
```

---

### Task 1: Email Verification Send API

**Files:**
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
- Create: `src/main/java/com/dcom/intranet/common/GlobalExceptionHandler.java`
- Create: `src/main/java/com/dcom/intranet/mypage/MyPageApiException.java`
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerification.java`
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`
- Create: `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java`
- Modify: `src/main/java/com/dcom/intranet/user/UserRepository.java`
- Modify: `src/main/java/com/dcom/intranet/config/SecurityConfig.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Create DTO files for send success and failure schemas.

- [ ] **Step 1: Write the failing send API tests**

Modify `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`.

Add these imports:

```java
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
```

Add this field:

```java
@Autowired
private EmailVerificationRepository emailVerificationRepository;
```

Update `setUp()` so email verifications are cleared before users:

```java
@BeforeEach
void setUp() {
    emailVerificationRepository.deleteAll();
    userRepository.deleteAll();
}
```

Add these tests:

```java
@Test
@DisplayName("Email verification send returns 200 and stores verification code")
void emailVerificationSendReturns200AndStoresVerificationCode() throws Exception {
    User user = saveUser("emailSend1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "new-email-send1@dcom.org"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.message").value("이메일 변경 인증 코드가 생성되었습니다."))
            .andExpect(jsonPath("$.data.expiresIn").value(300))
            .andExpect(jsonPath("$.data.verificationCode").doesNotExist());

    EmailVerification verification = emailVerificationRepository
            .findTopByLoginIdAndEmailOrderByCreatedAtDesc("emailSend1", "new-email-send1@dcom.org")
            .orElseThrow();
    assertThat(verification.getVerificationCode()).hasSize(6);
    assertThat(verification.getExpiresAt()).isAfter(LocalDateTime.now());
    assertThat(verification.isVerified()).isFalse();
    assertThat(verification.isUsed()).isFalse();
}

@Test
@DisplayName("Email verification send with blank email returns 400 common envelope")
void emailVerificationSendWithBlankEmailReturns400CommonEnvelope() throws Exception {
    User user = saveUser("emailSend2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": ""
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("요청값이 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification send with existing email returns 409 common envelope")
void emailVerificationSendWithExistingEmailReturns409CommonEnvelope() throws Exception {
    User user = saveUser("emailSend3", UserStatus.APPROVED, UserRole.USER);
    User otherUser = saveUser("emailOwner", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s"
                            }
                            """.formatted(otherUser.getEmail())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification send with active request returns 429 common envelope")
void emailVerificationSendWithActiveRequestReturns429CommonEnvelope() throws Exception {
    User user = saveUser("emailSend4", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String requestBody = """
            {
              "newEmail": "new-email-send4@dcom.org"
            }
            """;

    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk());

    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(429))
            .andExpect(jsonPath("$.message").value("이미 진행 중인 이메일 인증 요청이 있습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification send without token returns 401 common envelope")
void emailVerificationSendWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "new-email-no-token@dcom.org"
                            }
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

- [ ] **Step 2: Run the send tests to verify RED**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL during compilation because `EmailVerificationRepository` and send API classes do not exist.

- [ ] **Step 3: Add common exception handling**

Create `src/main/java/com/dcom/intranet/mypage/MyPageApiException.java`:

```java
package com.dcom.intranet.mypage;

import org.springframework.http.HttpStatus;

public class MyPageApiException extends RuntimeException {

    private final HttpStatus status;

    public MyPageApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
```

Create `src/main/java/com/dcom/intranet/common/GlobalExceptionHandler.java`:

```java
package com.dcom.intranet.common;

import com.dcom.intranet.mypage.MyPageApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(400, "요청값이 올바르지 않습니다."));
    }

    @ExceptionHandler(MyPageApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleMyPageApiException(MyPageApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.failure(exception.getStatus().value(), exception.getMessage()));
    }
}
```

- [ ] **Step 4: Add email verification persistence and send service**

Create `src/main/java/com/dcom/intranet/mypage/EmailVerification.java`:

```java
package com.dcom.intranet.mypage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Column(name = "email_change_token", unique = true, length = 100)
    private String emailChangeToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected EmailVerification() {
    }

    private EmailVerification(String loginId, String email, String verificationCode, LocalDateTime expiresAt) {
        this.loginId = loginId;
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.used = false;
    }

    public static EmailVerification create(
            String loginId,
            String email,
            String verificationCode,
            LocalDateTime expiresAt
    ) {
        return new EmailVerification(loginId, email, verificationCode, expiresAt);
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isActive(LocalDateTime now) {
        return !used && expiresAt.isAfter(now);
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getEmailChangeToken() {
        return emailChangeToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

Create `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`:

```java
package com.dcom.intranet.mypage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByLoginIdAndEmailOrderByCreatedAtDesc(String loginId, String email);
}
```

Modify `src/main/java/com/dcom/intranet/user/UserRepository.java`:

```java
package com.dcom.intranet.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByEmail(String email);
}
```

Create `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java`:

```java
package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.EmailVerificationSendResponse;
import com.dcom.intranet.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private static final int EXPIRES_IN_SECONDS = 300;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            UserRepository userRepository
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EmailVerificationSendResponse sendEmailChangeVerification(String loginId, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new MyPageApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        emailVerificationRepository.findTopByLoginIdAndEmailOrderByCreatedAtDesc(loginId, newEmail)
                .filter(verification -> verification.isActive(now))
                .ifPresent(verification -> {
                    throw new MyPageApiException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "이미 진행 중인 이메일 인증 요청이 있습니다."
                    );
                });

        EmailVerification verification = EmailVerification.create(
                loginId,
                newEmail,
                generateVerificationCode(),
                now.plusSeconds(EXPIRES_IN_SECONDS)
        );
        emailVerificationRepository.save(verification);

        return new EmailVerificationSendResponse(
                "이메일 변경 인증 코드가 생성되었습니다.",
                EXPIRES_IN_SECONDS
        );
    }

    private String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
```

- [ ] **Step 5: Add send request and response DTOs**

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendRequest.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 변경 인증 메일 발송 요청")
public record EmailVerificationSendRequest(
        @NotBlank
        @Email
        @Schema(description = "새 이메일", example = "newhong@khu.ac.kr")
        String newEmail
) {
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 변경 인증 메일 발송 응답 데이터")
public record EmailVerificationSendResponse(
        @Schema(description = "처리 메시지", example = "이메일 변경 인증 코드가 생성되었습니다.")
        String message,

        @Schema(description = "인증 코드 만료까지 남은 초", example = "300")
        long expiresIn
) {
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 변경 인증 메일 발송 성공 응답")
public class EmailVerificationSendApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "이메일 변경 인증 메일 발송 결과")
    public EmailVerificationSendResponse data;
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/BadRequestApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요청값 오류 응답")
public class BadRequestApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "400")
    public int status;

    @Schema(description = "응답 메시지", example = "요청값이 올바르지 않습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/ConflictApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "충돌 응답")
public class ConflictApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "409")
    public int status;

    @Schema(description = "응답 메시지", example = "이미 사용 중인 이메일입니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/TooManyRequestsApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "재요청 제한 응답")
public class TooManyRequestsApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "429")
    public int status;

    @Schema(description = "응답 메시지", example = "이미 진행 중인 이메일 인증 요청이 있습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
```

- [ ] **Step 6: Add send endpoint and security rule**

Modify `src/main/java/com/dcom/intranet/config/SecurityConfig.java` request matcher:

```java
.requestMatchers("/api/users/me", "/api/users/me/**").hasAnyRole("USER", "ADMIN")
```

Modify `src/main/java/com/dcom/intranet/mypage/MyPageController.java` by adding imports:

```java
import com.dcom.intranet.mypage.dto.BadRequestApiResponse;
import com.dcom.intranet.mypage.dto.ConflictApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationSendApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationSendRequest;
import com.dcom.intranet.mypage.dto.EmailVerificationSendResponse;
import com.dcom.intranet.mypage.dto.TooManyRequestsApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
```

Add an `EmailVerificationService` field and constructor parameter:

```java
private final EmailVerificationService emailVerificationService;

public MyPageController(MyPageService myPageService, EmailVerificationService emailVerificationService) {
    this.myPageService = myPageService;
    this.emailVerificationService = emailVerificationService;
}
```

Add this method:

```java
@Operation(
        summary = "이메일 변경 인증 메일 발송",
        description = "새 이메일에 대한 변경 인증 코드를 생성한다. 실제 메일 발송은 추후 메일 서비스 연동 시 연결한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "이메일 변경 인증 코드 생성 성공",
                        content = @Content(schema = @Schema(implementation = EmailVerificationSendApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "요청값 오류",
                        content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "이미 사용 중인 이메일",
                        content = @Content(schema = @Schema(implementation = ConflictApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "429",
                        description = "진행 중인 인증 요청 존재",
                        content = @Content(schema = @Schema(implementation = TooManyRequestsApiResponse.class))
                )
        }
)
@PostMapping("/me/email/verification/send")
public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(
        Authentication authentication,
        @Valid @RequestBody EmailVerificationSendRequest request
) {
    EmailVerificationSendResponse response =
            emailVerificationService.sendEmailChangeVerification(authentication.getName(), request.newEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 7: Run send tests to verify GREEN**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS for existing profile lookup tests and new send API tests.

- [ ] **Step 8: Commit Task 1**

Run:

```bash
git add src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/common/GlobalExceptionHandler.java src/main/java/com/dcom/intranet/mypage/MyPageApiException.java src/main/java/com/dcom/intranet/mypage/EmailVerification.java src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java src/main/java/com/dcom/intranet/user/UserRepository.java src/main/java/com/dcom/intranet/config/SecurityConfig.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendRequest.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendResponse.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationSendApiResponse.java src/main/java/com/dcom/intranet/mypage/dto/BadRequestApiResponse.java src/main/java/com/dcom/intranet/mypage/dto/ConflictApiResponse.java src/main/java/com/dcom/intranet/mypage/dto/TooManyRequestsApiResponse.java
git commit -m "feat: add email verification send API"
```

Expected: commit succeeds.

---

### Task 2: Email Verification Confirm API

**Files:**
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerification.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Create DTO files for verify success and `410` schemas.

- [ ] **Step 1: Write the failing verify API tests**

Add these tests to `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`:

```java
@Test
@DisplayName("Email verification verify returns 200 and emailChangeToken")
void emailVerificationVerifyReturns200AndEmailChangeToken() throws Exception {
    User user = saveUser("emailVerify1", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String newEmail = "new-email-verify1@dcom.org";

    requestEmailVerification(token, newEmail);
    String verificationCode = latestVerification(user.getLoginId(), newEmail).getVerificationCode();

    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s",
                              "verificationCode": "%s"
                            }
                            """.formatted(newEmail, verificationCode)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.emailChangeToken").isNotEmpty())
            .andExpect(jsonPath("$.data.message").value("이메일 변경 인증이 완료되었습니다."))
            .andExpect(jsonPath("$.data.verifiedEmail").value(newEmail));

    EmailVerification verification = latestVerification(user.getLoginId(), newEmail);
    assertThat(verification.isVerified()).isTrue();
    assertThat(verification.getEmailChangeToken()).isNotBlank();
}

@Test
@DisplayName("Email verification verify with wrong code returns 400 common envelope")
void emailVerificationVerifyWithWrongCodeReturns400CommonEnvelope() throws Exception {
    User user = saveUser("emailVerify2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String newEmail = "new-email-verify2@dcom.org";

    requestEmailVerification(token, newEmail);

    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s",
                              "verificationCode": "000000"
                            }
                            """.formatted(newEmail)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("인증 코드가 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification verify without request returns 400 common envelope")
void emailVerificationVerifyWithoutRequestReturns400CommonEnvelope() throws Exception {
    User user = saveUser("emailVerify3", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "not-requested@dcom.org",
                              "verificationCode": "123456"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("이메일 인증 요청을 찾을 수 없습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification verify with expired code returns 410 common envelope")
void emailVerificationVerifyWithExpiredCodeReturns410CommonEnvelope() throws Exception {
    User user = saveUser("emailVerify4", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String newEmail = "new-email-verify4@dcom.org";
    emailVerificationRepository.save(EmailVerification.create(
            user.getLoginId(),
            newEmail,
            "123456",
            LocalDateTime.now().minusSeconds(1)
    ));

    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s",
                              "verificationCode": "123456"
                            }
                            """.formatted(newEmail)))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(410))
            .andExpect(jsonPath("$.message").value("이메일 인증이 만료되었습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Email verification verify without token returns 401 common envelope")
void emailVerificationVerifyWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "new-email-no-token-verify@dcom.org",
                              "verificationCode": "123456"
                            }
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

Add these helper methods to the test class:

```java
private void requestEmailVerification(String token, String newEmail) throws Exception {
    mockMvc.perform(post("/api/users/me/email/verification/send")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s"
                            }
                            """.formatted(newEmail)))
            .andExpect(status().isOk());
}

private EmailVerification latestVerification(String loginId, String email) {
    return emailVerificationRepository
            .findTopByLoginIdAndEmailOrderByCreatedAtDesc(loginId, email)
            .orElseThrow();
}
```

- [ ] **Step 2: Run the verify tests to verify RED**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL because `/api/users/me/email/verification/verify` does not exist and verify DTOs are missing.

- [ ] **Step 3: Add verify behavior to the domain and service**

Modify `src/main/java/com/dcom/intranet/mypage/EmailVerification.java` by adding these methods:

```java
public boolean isExpired(LocalDateTime now) {
    return !expiresAt.isAfter(now);
}

public boolean matchesCode(String verificationCode) {
    return this.verificationCode.equals(verificationCode);
}

public void verify(String emailChangeToken) {
    this.emailChangeToken = emailChangeToken;
    this.verified = true;
}
```

Modify `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java` by adding imports:

```java
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyResponse;

import java.util.UUID;
```

Add this method:

```java
@Transactional
public EmailVerificationVerifyResponse verifyEmailChangeCode(
        String loginId,
        String newEmail,
        String verificationCode
) {
    EmailVerification verification = emailVerificationRepository
            .findTopByLoginIdAndEmailOrderByCreatedAtDesc(loginId, newEmail)
            .orElseThrow(() -> new MyPageApiException(
                    HttpStatus.BAD_REQUEST,
                    "이메일 인증 요청을 찾을 수 없습니다."
            ));

    LocalDateTime now = LocalDateTime.now();
    if (verification.isExpired(now)) {
        throw new MyPageApiException(HttpStatus.GONE, "이메일 인증이 만료되었습니다.");
    }

    if (!verification.matchesCode(verificationCode)) {
        throw new MyPageApiException(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다.");
    }

    String emailChangeToken = UUID.randomUUID().toString();
    verification.verify(emailChangeToken);

    return new EmailVerificationVerifyResponse(
            emailChangeToken,
            "이메일 변경 인증이 완료되었습니다.",
            verification.getEmail()
    );
}
```

- [ ] **Step 4: Add verify request and response DTOs**

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyRequest.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 변경 인증 확인 요청")
public record EmailVerificationVerifyRequest(
        @NotBlank
        @Email
        @Schema(description = "새 이메일", example = "newhong@khu.ac.kr")
        String newEmail,

        @NotBlank
        @Schema(description = "인증 코드", example = "123456")
        String verificationCode
) {
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 변경 인증 확인 응답 데이터")
public record EmailVerificationVerifyResponse(
        @Schema(description = "회원정보 수정 API에서 사용할 이메일 변경 토큰", example = "generated-email-change-token")
        String emailChangeToken,

        @Schema(description = "처리 메시지", example = "이메일 변경 인증이 완료되었습니다.")
        String message,

        @Schema(description = "인증 완료된 이메일", example = "newhong@khu.ac.kr")
        String verifiedEmail
) {
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 변경 인증 확인 성공 응답")
public class EmailVerificationVerifyApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "이메일 변경 인증 확인 결과")
    public EmailVerificationVerifyResponse data;
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/GoneApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "만료 응답")
public class GoneApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "410")
    public int status;

    @Schema(description = "응답 메시지", example = "이메일 인증이 만료되었습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
```

- [ ] **Step 5: Add verify endpoint**

Modify `src/main/java/com/dcom/intranet/mypage/MyPageController.java` by adding imports:

```java
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyRequest;
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyResponse;
import com.dcom.intranet.mypage.dto.GoneApiResponse;
```

Add this method:

```java
@Operation(
        summary = "이메일 변경 인증 확인",
        description = "새 이메일과 인증 코드를 검증하고 회원정보 수정에서 사용할 이메일 변경 토큰을 발급한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "이메일 변경 인증 성공",
                        content = @Content(schema = @Schema(implementation = EmailVerificationVerifyApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "요청값 오류 또는 인증 코드 불일치",
                        content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "410",
                        description = "인증 코드 만료",
                        content = @Content(schema = @Schema(implementation = GoneApiResponse.class))
                )
        }
)
@PostMapping("/me/email/verification/verify")
public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyEmailVerification(
        Authentication authentication,
        @Valid @RequestBody EmailVerificationVerifyRequest request
) {
    EmailVerificationVerifyResponse response = emailVerificationService.verifyEmailChangeCode(
            authentication.getName(),
            request.newEmail(),
            request.verificationCode()
    );
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 6: Run verify tests to verify GREEN**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS for lookup, send, and verify tests.

- [ ] **Step 7: Commit Task 2**

Run:

```bash
git add src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/mypage/EmailVerification.java src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyRequest.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyResponse.java src/main/java/com/dcom/intranet/mypage/dto/EmailVerificationVerifyApiResponse.java src/main/java/com/dcom/intranet/mypage/dto/GoneApiResponse.java
git commit -m "feat: add email verification confirm API"
```

Expected: commit succeeds.

---

### Task 3: Member Profile Settings Update API

**Files:**
- Test: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
- Modify: `src/main/java/com/dcom/intranet/user/User.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerification.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
- Create DTO files for settings update success.

- [ ] **Step 1: Write the failing settings update tests**

Add this import to `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`:

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
```

Add these tests:

```java
@Test
@DisplayName("Profile settings update changes name and phone number only")
void profileSettingsUpdateChangesNameAndPhoneNumberOnly() throws Exception {
    User user = saveUser("settings1", UserStatus.APPROVED, UserRole.USER);
    String originalEmail = user.getEmail();
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "김수정",
                              "phoneNumber": "010-9999-8888"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
            .andExpect(jsonPath("$.data.name").value("김수정"))
            .andExpect(jsonPath("$.data.studentId").value(studentIdFor("settings1")))
            .andExpect(jsonPath("$.data.email").value(originalEmail))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-9999-8888"))
            .andExpect(jsonPath("$.data.message").value("회원정보가 수정되었습니다."))
            .andExpect(jsonPath("$.data.loginId").doesNotExist())
            .andExpect(jsonPath("$.data.userId").doesNotExist());

    User updatedUser = userRepository.findByLoginId("settings1").orElseThrow();
    assertThat(updatedUser.getName()).isEqualTo("김수정");
    assertThat(updatedUser.getPhoneNumber()).isEqualTo("010-9999-8888");
    assertThat(updatedUser.getEmail()).isEqualTo(originalEmail);
}

@Test
@DisplayName("Profile settings update with verified emailChangeToken changes email")
void profileSettingsUpdateWithVerifiedEmailChangeTokenChangesEmail() throws Exception {
    User user = saveUser("settings2", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String newEmail = "new-email-settings2@dcom.org";

    String emailChangeToken = verifiedEmailChangeToken(token, user.getLoginId(), newEmail);

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "이메일수정",
                              "phoneNumber": "010-2222-3333",
                              "emailChangeToken": "%s"
                            }
                            """.formatted(emailChangeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("이메일수정"))
            .andExpect(jsonPath("$.data.studentId").value(studentIdFor("settings2")))
            .andExpect(jsonPath("$.data.email").value(newEmail))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-2222-3333"))
            .andExpect(jsonPath("$.data.message").value("회원정보가 수정되었습니다."));

    User updatedUser = userRepository.findByLoginId("settings2").orElseThrow();
    assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
    assertThat(emailVerificationRepository.findByEmailChangeToken(emailChangeToken).orElseThrow().isUsed()).isTrue();
}

@Test
@DisplayName("Profile settings update with invalid emailChangeToken returns 400 common envelope")
void profileSettingsUpdateWithInvalidEmailChangeTokenReturns400CommonEnvelope() throws Exception {
    User user = saveUser("settings3", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "토큰오류",
                              "phoneNumber": "010-3333-4444",
                              "emailChangeToken": "invalid-token"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("이메일 변경 토큰이 올바르지 않습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Profile settings update with expired emailChangeToken returns 410 common envelope")
void profileSettingsUpdateWithExpiredEmailChangeTokenReturns410CommonEnvelope() throws Exception {
    User user = saveUser("settings4", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    EmailVerification verification = EmailVerification.create(
            user.getLoginId(),
            "new-email-settings4@dcom.org",
            "123456",
            LocalDateTime.now().minusSeconds(1)
    );
    verification.verify("expired-email-change-token");
    emailVerificationRepository.save(verification);

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "만료토큰",
                              "phoneNumber": "010-4444-5555",
                              "emailChangeToken": "expired-email-change-token"
                            }
                            """))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(410))
            .andExpect(jsonPath("$.message").value("이메일 인증이 만료되었습니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Profile settings update with email already taken after verification returns 409 common envelope")
void profileSettingsUpdateWithEmailAlreadyTakenAfterVerificationReturns409CommonEnvelope() throws Exception {
    User user = saveUser("settings5", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
    String newEmail = "new-email-settings5@dcom.org";
    String emailChangeToken = verifiedEmailChangeToken(token, user.getLoginId(), newEmail);
    userRepository.save(new User(
            "settings5owner",
            "20995555",
            "encoded-password",
            "이메일소유자",
            "010-5555-0000",
            newEmail,
            true,
            UserStatus.APPROVED,
            UserRole.USER
    ));

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "중복이메일",
                              "phoneNumber": "010-5555-6666",
                              "emailChangeToken": "%s"
                            }
                            """.formatted(emailChangeToken)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
            .andExpect(jsonPath("$.data").value(nullValue()));
}

@Test
@DisplayName("Profile settings update does not change loginId or studentId from request body")
void profileSettingsUpdateDoesNotChangeLoginIdOrStudentIdFromRequestBody() throws Exception {
    User user = saveUser("settings6", UserStatus.APPROVED, UserRole.USER);
    String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

    mockMvc.perform(patch("/api/users/me/settings")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "loginId": "changed-login",
                              "studentId": "99999999",
                              "name": "불변필드",
                              "phoneNumber": "010-6666-7777"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.studentId").value(studentIdFor("settings6")));

    User updatedUser = userRepository.findByLoginId("settings6").orElseThrow();
    assertThat(updatedUser.getLoginId()).isEqualTo("settings6");
    assertThat(updatedUser.getStudentId()).isEqualTo(studentIdFor("settings6"));
}

@Test
@DisplayName("Profile settings update without token returns 401 common envelope")
void profileSettingsUpdateWithoutTokenReturns401CommonEnvelope() throws Exception {
    mockMvc.perform(patch("/api/users/me/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name": "인증없음",
                              "phoneNumber": "010-7777-8888"
                            }
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
            .andExpect(jsonPath("$.data").value(nullValue()));
}
```

Add this helper method:

```java
private String verifiedEmailChangeToken(String token, String loginId, String newEmail) throws Exception {
    requestEmailVerification(token, newEmail);
    String verificationCode = latestVerification(loginId, newEmail).getVerificationCode();

    mockMvc.perform(post("/api/users/me/email/verification/verify")
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "newEmail": "%s",
                              "verificationCode": "%s"
                            }
                            """.formatted(newEmail, verificationCode)))
            .andExpect(status().isOk());

    return latestVerification(loginId, newEmail).getEmailChangeToken();
}
```

Extend `studentIdFor` switch with the new login IDs:

```java
case "settings1" -> "20240006";
case "settings2" -> "20240007";
case "settings3" -> "20240008";
case "settings4" -> "20240009";
case "settings5" -> "20240010";
case "settings6" -> "20240011";
case "emailSend1" -> "20240012";
case "emailSend2" -> "20240013";
case "emailSend3" -> "20240014";
case "emailSend4" -> "20240015";
case "emailOwner" -> "20240016";
case "emailVerify1" -> "20240017";
case "emailVerify2" -> "20240018";
case "emailVerify3" -> "20240019";
case "emailVerify4" -> "20240020";
```

- [ ] **Step 2: Run the settings tests to verify RED**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: FAIL because `/api/users/me/settings`, `findByEmailChangeToken`, settings DTOs, and update methods do not exist.

- [ ] **Step 3: Add user update methods**

Modify `src/main/java/com/dcom/intranet/user/User.java` by adding these methods:

```java
public void updateProfile(String name, String phoneNumber) {
    this.name = name;
    this.phoneNumber = phoneNumber;
}

public void changeEmail(String email) {
    this.email = email;
}
```

- [ ] **Step 4: Add email-change token consumption**

Modify `src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java`:

```java
package com.dcom.intranet.mypage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByLoginIdAndEmailOrderByCreatedAtDesc(String loginId, String email);

    Optional<EmailVerification> findByEmailChangeToken(String emailChangeToken);
}
```

Modify `src/main/java/com/dcom/intranet/mypage/EmailVerification.java` by adding:

```java
public boolean belongsTo(String loginId) {
    return this.loginId.equals(loginId);
}

public boolean canChangeEmail() {
    return verified && !used;
}

public void markUsed() {
    this.used = true;
}
```

Modify `src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java` by adding:

```java
@Transactional
public EmailVerification consumeEmailChangeToken(String loginId, String emailChangeToken) {
    EmailVerification verification = emailVerificationRepository.findByEmailChangeToken(emailChangeToken)
            .filter(candidate -> candidate.belongsTo(loginId))
            .filter(EmailVerification::canChangeEmail)
            .orElseThrow(() -> new MyPageApiException(
                    HttpStatus.BAD_REQUEST,
                    "이메일 변경 토큰이 올바르지 않습니다."
            ));

    if (verification.isExpired(LocalDateTime.now())) {
        throw new MyPageApiException(HttpStatus.GONE, "이메일 인증이 만료되었습니다.");
    }

    verification.markUsed();
    return verification;
}
```

- [ ] **Step 5: Add settings request and response DTOs**

Create `src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateRequest.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원정보 수정 요청")
public record MyProfileUpdateRequest(
        @NotBlank
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotBlank
        @Schema(description = "전화번호", example = "010-9999-8888")
        String phoneNumber,

        @Schema(description = "이메일 변경 인증 확인 API에서 발급받은 토큰", example = "generated-email-change-token")
        String emailChangeToken
) {
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 수정 응답 데이터")
public record MyProfileUpdateResponse(
        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "전체 학번", example = "2026123456")
        String studentId,

        @Schema(description = "이메일", example = "newhong@khu.ac.kr")
        String email,

        @Schema(description = "전화번호", example = "010-9999-8888")
        String phoneNumber,

        @Schema(description = "처리 메시지", example = "회원정보가 수정되었습니다.")
        String message
) {

    public static MyProfileUpdateResponse from(User user) {
        return new MyProfileUpdateResponse(
                user.getName(),
                user.getStudentId(),
                user.getEmail(),
                user.getPhoneNumber(),
                "회원정보가 수정되었습니다."
        );
    }
}
```

Create `src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateApiResponse.java`:

```java
package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 수정 성공 응답")
public class MyProfileUpdateApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    public String message;

    @Schema(description = "회원정보 수정 결과")
    public MyProfileUpdateResponse data;
}
```

- [ ] **Step 6: Add settings service orchestration**

Modify `src/main/java/com/dcom/intranet/mypage/MyPageService.java` by adding imports:

```java
import com.dcom.intranet.mypage.dto.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.MyProfileUpdateResponse;
import org.springframework.util.StringUtils;
```

Add an `EmailVerificationService` field and constructor parameter:

```java
private final EmailVerificationService emailVerificationService;

public MyPageService(UserRepository userRepository, EmailVerificationService emailVerificationService) {
    this.userRepository = userRepository;
    this.emailVerificationService = emailVerificationService;
}
```

Add this method:

```java
@Transactional
public MyProfileUpdateResponse updateMyProfile(String loginId, MyProfileUpdateRequest request) {
    User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));

    user.updateProfile(request.name(), request.phoneNumber());

    if (StringUtils.hasText(request.emailChangeToken())) {
        EmailVerification verification =
                emailVerificationService.consumeEmailChangeToken(loginId, request.emailChangeToken());
        if (userRepository.existsByEmail(verification.getEmail())) {
            throw new MyPageApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        user.changeEmail(verification.getEmail());
    }

    return MyProfileUpdateResponse.from(user);
}
```

- [ ] **Step 7: Add settings endpoint**

Modify `src/main/java/com/dcom/intranet/mypage/MyPageController.java` by adding imports:

```java
import com.dcom.intranet.mypage.dto.MyProfileUpdateApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.MyProfileUpdateResponse;
import org.springframework.web.bind.annotation.PatchMapping;
```

Add this method:

```java
@Operation(
        summary = "회원정보 수정",
        description = "인증된 사용자의 이름, 전화번호를 수정하고 검증된 이메일 변경 토큰이 있으면 이메일도 변경한다.",
        responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "회원정보 수정 성공",
                        content = @Content(schema = @Schema(implementation = MyProfileUpdateApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "요청값 오류 또는 이메일 변경 토큰 오류",
                        content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "이미 사용 중인 이메일",
                        content = @Content(schema = @Schema(implementation = ConflictApiResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "410",
                        description = "이메일 변경 토큰 만료",
                        content = @Content(schema = @Schema(implementation = GoneApiResponse.class))
                )
        }
)
@PatchMapping("/me/settings")
public ResponseEntity<ApiResponse<MyProfileUpdateResponse>> updateMyProfile(
        Authentication authentication,
        @Valid @RequestBody MyProfileUpdateRequest request
) {
    MyProfileUpdateResponse response = myPageService.updateMyProfile(authentication.getName(), request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 8: Run settings tests to verify GREEN**

Run:

```bash
./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest
```

Expected: PASS for lookup, send, verify, and settings tests.

- [ ] **Step 9: Run all tests**

Run:

```bash
./gradlew test
```

Expected: BUILD SUCCESS.

- [ ] **Step 10: Commit Task 3**

Run:

```bash
git add src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java src/main/java/com/dcom/intranet/user/User.java src/main/java/com/dcom/intranet/mypage/EmailVerification.java src/main/java/com/dcom/intranet/mypage/EmailVerificationRepository.java src/main/java/com/dcom/intranet/mypage/EmailVerificationService.java src/main/java/com/dcom/intranet/mypage/MyPageService.java src/main/java/com/dcom/intranet/mypage/MyPageController.java src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateRequest.java src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateResponse.java src/main/java/com/dcom/intranet/mypage/dto/MyProfileUpdateApiResponse.java
git commit -m "feat: add member profile update API"
```

Expected: commit succeeds.

---

## Final Verification

- [ ] Run all tests:

```bash
./gradlew test
```

Expected: BUILD SUCCESS.

- [ ] Inspect git status:

```bash
git status --short
```

Expected: only pre-existing untracked files remain, or a clean tree if those files were handled separately.

- [ ] Optional Swagger contract check after starting the app:

```bash
./gradlew bootRun
```

Then open `/v3/api-docs` and confirm the three new endpoints expose success and failure response schemas with `success`, `status`, `message`, and `data`.

## Plan Self-Review

- Spec coverage: send, verify, settings, status codes `200`, `400`, `401`, `409`, `410`, and `429` are each mapped to tests and implementation steps.
- Scope check: actual SMTP or external mail delivery is not implemented; the plan keeps the agreed server-internal verification flow.
- Type consistency: request/response DTO names used in controller, service, and tests match the files created in the plan.
- TDD check: every production change is preceded by a failing MockMvc test step.
