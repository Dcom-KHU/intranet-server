package com.dcom.intranet.mypage;

import com.dcom.intranet.jwt.JwtTokenProvider;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostTargetResponse;
import com.dcom.intranet.mypage.dto.PageInfoResponse;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyPageControllerTest {

    private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";
    private static final String UNAUTHORIZED_MESSAGE = "인증이 필요합니다.";
    private static final String CURRENT_PASSWORD = "current-password";
    private static final String NEW_PASSWORD = "changed-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestMyWrittenPostReader myWrittenPostReader;

    @Autowired
    private TestMyWrittenCommentReader myWrittenCommentReader;

    @BeforeEach
    void setUp() {
        myWrittenPostReader.reset();
        myWrittenCommentReader.reset();
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
                        .param("type", "archives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.posts[0].id").value(11))
                .andExpect(jsonPath("$.data.posts[0].number").value(1))
                .andExpect(jsonPath("$.data.posts[0].title").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.data.posts[0].type").value("archives"))
                .andExpect(jsonPath("$.data.posts[0].createdAt").value("2026-05-25T10:30:00"))
                .andExpect(jsonPath("$.data.postList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPage()).isEqualTo(0);
        assertThat(myWrittenPostReader.lastSize()).isEqualTo(10);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("archives");
    }

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
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.postList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPage()).isEqualTo(0);
        assertThat(myWrittenPostReader.lastSize()).isEqualTo(10);
        assertThat(myWrittenPostReader.lastType()).isNull();
    }

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
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.data.posts").isEmpty())
                .andExpect(jsonPath("$.data.postList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());
    }

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
                        .param("type", "info-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.comments[0].id").value(101))
                .andExpect(jsonPath("$.data.comments[0].number").value(1))
                .andExpect(jsonPath("$.data.comments[0].type").value("info-posts"))
                .andExpect(jsonPath("$.data.comments[0].targetId").value(12))
                .andExpect(jsonPath("$.data.comments[0].targetTitle").value("React 참고 자료 모음"))
                .andExpect(jsonPath("$.data.comments[0].content").value("좋은 자료 감사합니다."))
                .andExpect(jsonPath("$.data.comments[0].createdAt").value("2026-06-01T13:00:00"))
                .andExpect(jsonPath("$.data.commentList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastPage()).isEqualTo(0);
        assertThat(myWrittenCommentReader.lastSize()).isEqualTo(10);
        assertThat(myWrittenCommentReader.lastType()).isEqualTo("info-posts");
    }

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
                .andExpect(jsonPath("$.data.comments").isArray())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.commentList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());

        assertThat(myWrittenCommentReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenCommentReader.lastPage()).isEqualTo(0);
        assertThat(myWrittenCommentReader.lastSize()).isEqualTo(10);
        assertThat(myWrittenCommentReader.lastType()).isNull();
    }

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
                        .param("type", "photo-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.comments").isArray())
                .andExpect(jsonPath("$.data.comments").isEmpty())
                .andExpect(jsonPath("$.data.commentList").doesNotExist())
                .andExpect(jsonPath("$.data.pageInfo").doesNotExist());

        assertThat(myWrittenCommentReader.lastType()).isEqualTo("photo-posts");
    }

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

    @Test
    @DisplayName("My written post detail target returns info-posts route target")
    void myWrittenPostDetailTargetReturnsInfoPostsRouteTarget() throws Exception {
        User user = saveUser("postTargetInfo", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenTargetResponse(new MyWrittenPostTargetResponse("info-posts", 21L));

        mockMvc.perform(get("/api/users/me/posts/21")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.targetType").value("info-posts"))
                .andExpect(jsonPath("$.data.targetId").value(21));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(21L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("info-posts");
    }

    @Test
    @DisplayName("My written post detail target returns archives route target")
    void myWrittenPostDetailTargetReturnsArchivesRouteTarget() throws Exception {
        User user = saveUser("postTargetArchive", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenTargetResponse(new MyWrittenPostTargetResponse("archives", 22L));

        mockMvc.perform(get("/api/users/me/posts/22")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "archives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("archives"))
                .andExpect(jsonPath("$.data.targetId").value(22));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(22L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("archives");
    }

    @Test
    @DisplayName("My written post detail target returns photo-posts route target")
    void myWrittenPostDetailTargetReturnsPhotoPostsRouteTarget() throws Exception {
        User user = saveUser("postTargetPhoto", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenTargetResponse(new MyWrittenPostTargetResponse("photo-posts", 23L));

        mockMvc.perform(get("/api/users/me/posts/23")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "photo-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("photo-posts"))
                .andExpect(jsonPath("$.data.targetId").value(23));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(23L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("photo-posts");
    }

    @Test
    @DisplayName("My written post detail target returns notices route target for admin")
    void myWrittenPostDetailTargetReturnsNoticesRouteTargetForAdmin() throws Exception {
        User admin = saveUser("postTargetNotice", UserStatus.APPROVED, UserRole.ADMIN);
        String token = jwtTokenProvider.createAccessToken(admin.getLoginId(), admin.getRole().name());
        myWrittenPostReader.givenTargetResponse(new MyWrittenPostTargetResponse("notices", 24L));

        mockMvc.perform(get("/api/users/me/posts/24")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("notices"))
                .andExpect(jsonPath("$.data.targetId").value(24));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(admin.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(24L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("notices");
    }

    @Test
    @DisplayName("My written post detail target without token returns 401 common envelope")
    void myWrittenPostDetailTargetWithoutTokenReturns401CommonEnvelope() throws Exception {
        mockMvc.perform(get("/api/users/me/posts/21")
                        .param("type", "info-posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written post detail target not found returns 404 common envelope")
    void myWrittenPostDetailTargetNotFoundReturns404CommonEnvelope() throws Exception {
        User user = saveUser("postTargetMissing", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenTargetNotFound();

        mockMvc.perform(get("/api/users/me/posts/999")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("작성한 글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written post delete returns 200 common envelope and message")
    void myWrittenPostDeleteReturns200CommonEnvelopeAndMessage() throws Exception {
        User user = saveUser("postDeleteInfo", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenDeleteResponse(new MyWrittenPostDeleteResponse("작성한 글이 삭제되었습니다."));

        mockMvc.perform(delete("/api/users/me/posts/31")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.message").value("작성한 글이 삭제되었습니다."));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(31L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("info-posts");
    }

    @Test
    @DisplayName("My written post delete passes archives type")
    void myWrittenPostDeletePassesArchivesType() throws Exception {
        User user = saveUser("postDeleteArchive", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(delete("/api/users/me/posts/32")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "archives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("작성한 글이 삭제되었습니다."));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(32L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("archives");
    }

    @Test
    @DisplayName("My written post delete passes photo-posts type")
    void myWrittenPostDeletePassesPhotoPostsType() throws Exception {
        User user = saveUser("postDeletePhoto", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(delete("/api/users/me/posts/33")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "photo-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("작성한 글이 삭제되었습니다."));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(user.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(33L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("photo-posts");
    }

    @Test
    @DisplayName("My written post delete passes notices type for admin")
    void myWrittenPostDeletePassesNoticesTypeForAdmin() throws Exception {
        User admin = saveUser("postDeleteNotice", UserStatus.APPROVED, UserRole.ADMIN);
        String token = jwtTokenProvider.createAccessToken(admin.getLoginId(), admin.getRole().name());

        mockMvc.perform(delete("/api/users/me/posts/34")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("작성한 글이 삭제되었습니다."));

        assertThat(myWrittenPostReader.lastUserId()).isEqualTo(admin.getId());
        assertThat(myWrittenPostReader.lastPostId()).isEqualTo(34L);
        assertThat(myWrittenPostReader.lastType()).isEqualTo("notices");
    }

    @Test
    @DisplayName("My written post delete without token returns 401 common envelope")
    void myWrittenPostDeleteWithoutTokenReturns401CommonEnvelope() throws Exception {
        mockMvc.perform(delete("/api/users/me/posts/31")
                        .param("type", "info-posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written post delete forbidden returns 403 common envelope")
    void myWrittenPostDeleteForbiddenReturns403CommonEnvelope() throws Exception {
        User user = saveUser("postDeleteForbidden", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenDeleteForbidden();

        mockMvc.perform(delete("/api/users/me/posts/35")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("My written post delete not found returns 404 common envelope")
    void myWrittenPostDeleteNotFoundReturns404CommonEnvelope() throws Exception {
        User user = saveUser("postDeleteMissing", UserStatus.APPROVED, UserRole.USER);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        myWrittenPostReader.givenDeleteNotFound();

        mockMvc.perform(delete("/api/users/me/posts/999")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("type", "info-posts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("작성한 글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    private User saveUser(String loginId, UserStatus status, UserRole role) {
        User user = new User(
                loginId,
                studentIdFor(loginId),
                passwordEncoder.encode(CURRENT_PASSWORD),
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
        private MyWrittenPostTargetResponse targetResponse = new MyWrittenPostTargetResponse("info-posts", 1L);
        private MyWrittenPostDeleteResponse deleteResponse = new MyWrittenPostDeleteResponse("작성한 글이 삭제되었습니다.");
        private boolean targetNotFound;
        private boolean deleteForbidden;
        private boolean deleteNotFound;
        private Long lastUserId;
        private Long lastPostId;
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

        @Override
        public MyWrittenPostTargetResponse readTarget(Long userId, Long postId, String type) {
            this.lastUserId = userId;
            this.lastPostId = postId;
            this.lastType = type;
            if (targetNotFound) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.");
            }
            return targetResponse;
        }

        @Override
        public MyWrittenPostDeleteResponse delete(Long userId, Long postId, String type) {
            this.lastUserId = userId;
            this.lastPostId = postId;
            this.lastType = type;
            if (deleteForbidden) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
            }
            if (deleteNotFound) {
                throw new MyPageApiException(org.springframework.http.HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.");
            }
            return deleteResponse;
        }

        void givenResponse(MyWrittenPostListResponse response) {
            this.response = response;
        }

        void givenTargetResponse(MyWrittenPostTargetResponse targetResponse) {
            this.targetResponse = targetResponse;
            this.targetNotFound = false;
        }

        void givenTargetNotFound() {
            this.targetNotFound = true;
        }

        void givenDeleteResponse(MyWrittenPostDeleteResponse deleteResponse) {
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
            this.response = MyWrittenPostListResponse.empty(0, 10);
            this.targetResponse = new MyWrittenPostTargetResponse("info-posts", 1L);
            this.deleteResponse = new MyWrittenPostDeleteResponse("작성한 글이 삭제되었습니다.");
            this.targetNotFound = false;
            this.deleteForbidden = false;
            this.deleteNotFound = false;
            this.lastUserId = null;
            this.lastPostId = null;
            this.lastPage = -1;
            this.lastSize = -1;
            this.lastType = null;
        }

        Long lastUserId() {
            return lastUserId;
        }

        Long lastPostId() {
            return lastPostId;
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
            case "withdraw1" -> "20240021";
            case "withdraw2" -> "20240022";
            case "withdrawnUser" -> "20240023";
            case "postTargetInfo" -> "20240101";
            case "postTargetArchive" -> "20240102";
            case "postTargetPhoto" -> "20240103";
            case "postTargetNotice" -> "20240104";
            case "postTargetMissing" -> "20240105";
            case "postDeleteInfo" -> "20240111";
            case "postDeleteArchive" -> "20240112";
            case "postDeletePhoto" -> "20240113";
            case "postDeleteNotice" -> "20240114";
            case "postDeleteForbidden" -> "20240115";
            case "postDeleteMissing" -> "20240116";
            case "comments1" -> "20240121";
            case "comments2" -> "20240122";
            case "comments3" -> "20240123";
            case "commentsPending" -> "20240124";
            default -> "20249999";
        };
    }
}
