package com.dcom.intranet.mypage;

import com.dcom.intranet.jwt.JwtTokenProvider;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import com.dcom.intranet.user.UserRole;
import com.dcom.intranet.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyPageControllerTest {

    private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";
    private static final String UNAUTHORIZED_MESSAGE = "인증이 필요합니다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @BeforeEach
    void setUp() {
        emailVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Authenticated APPROVED USER token returns 200 common envelope and profile data")
    void authenticatedApprovedUserTokenReturns200CommonEnvelopeAndProfileData() throws Exception {
        User user = saveUser("member1", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.loginId").value("member1"))
                .andExpect(jsonPath("$.data.email").value("member1@dcom.org"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.studentId").value("20240001"));
    }

    @Test
    @DisplayName("Response data has loginId, email, name, phoneNumber, studentId")
    void responseDataHasProfileFields() throws Exception {
        User user = saveUser("member2", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.phoneNumber").exists())
                .andExpect(jsonPath("$.data.studentId").exists());
    }

    @Test
    @DisplayName("Response data does not expose userId or password")
    void responseDataDoesNotExposeUserIdOrPassword() throws Exception {
        User user = saveUser("member3", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("Missing token returns 401 common envelope with data null")
    void missingTokenReturns401CommonEnvelopeWithDataNull() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("Invalid token returns 401 common envelope with data null")
    void invalidTokenReturns401CommonEnvelopeWithDataNull() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer("invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("PENDING user token returns 401 common envelope with data null")
    void pendingUserTokenReturns401CommonEnvelopeWithDataNull() throws Exception {
        User user = saveUser("member4", UserStatus.PENDING, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("APPROVED ADMIN token returns 200 and profile data")
    void approvedAdminTokenReturns200AndProfileData() throws Exception {
        User user = saveUser("admin1", UserStatus.APPROVED, UserRole.ADMIN);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.loginId").value("admin1"))
                .andExpect(jsonPath("$.data.email").value("admin1@dcom.org"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.studentId").value(studentIdFor("admin1")));
    }

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

    private User saveUser(String loginId, UserStatus status, UserRole role) {
        User user = new User(
                loginId,
                studentIdFor(loginId),
                "encoded-password",
                "홍길동",
                "010-1234-5678",
                loginId + "@dcom.org",
                true,
                status,
                role
        );

        return userRepository.save(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

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

    private String studentIdFor(String loginId) {
        return switch (loginId) {
            case "member1" -> "20240001";
            case "member2" -> "20240002";
            case "member3" -> "20240003";
            case "member4" -> "20240004";
            case "admin1" -> "20240005";
            case "emailSend1" -> "20240012";
            case "emailSend2" -> "20240013";
            case "emailSend3" -> "20240014";
            case "emailSend4" -> "20240015";
            case "emailOwner" -> "20240016";
            case "emailVerify1" -> "20240017";
            case "emailVerify2" -> "20240018";
            case "emailVerify3" -> "20240019";
            case "emailVerify4" -> "20240020";
            case "settings1" -> "20240006";
            case "settings2" -> "20240007";
            case "settings3" -> "20240008";
            case "settings4" -> "20240009";
            case "settings5" -> "20240010";
            case "settings6" -> "20240011";
            default -> "20249999";
        };
    }
}
