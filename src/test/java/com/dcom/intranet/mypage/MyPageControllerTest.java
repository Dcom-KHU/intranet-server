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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
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

    private String studentIdFor(String loginId) {
        return switch (loginId) {
            case "member1" -> "20240001";
            case "member2" -> "20240002";
            case "member3" -> "20240003";
            case "member4" -> "20240004";
            case "admin1" -> "20240005";
            default -> "20249999";
        };
    }
}
