package com.dcom.intranet.auth;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.jwt.JwtTokenProvider;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-for-auth-controller-test-1234567890",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password"
})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Auth me returns requirePasswordChange false for normal password user")
    void authMeReturnsRequirePasswordChangeFalseForNormalPasswordUser() throws Exception {
        User user = saveApprovedUser("authMeNormal");
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("authMeNormal"))
                .andExpect(jsonPath("$.requirePasswordChange").value(false));
    }

    @Test
    @DisplayName("Auth me returns requirePasswordChange true when temp password is still valid")
    void authMeReturnsRequirePasswordChangeTrueWhenTempPasswordIsStillValid() throws Exception {
        User user = saveApprovedUser("authMeTemp");
        user.setTempPassword(passwordEncoder.encode("temporary-password"), 30);
        userRepository.save(user);
        String token = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("authMeTemp"))
                .andExpect(jsonPath("$.requirePasswordChange").value(true));
    }

    private User saveApprovedUser(String loginId) {
        User user = new User(
                loginId,
                passwordEncoder.encode("current-password"),
                "홍길동",
                studentIdFor(loginId),
                loginId + "@dcom.org",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(user, "status", UserStatus.APPROVED);
        ReflectionTestUtils.setField(user, "role", UserRole.USER);

        return userRepository.save(user);
    }

    private String studentIdFor(String loginId) {
        return switch (loginId) {
            case "authMeNormal" -> "20249901";
            case "authMeTemp" -> "20249902";
            default -> throw new IllegalArgumentException("Unknown loginId: " + loginId);
        };
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @TestConfiguration
    static class MailSenderTestConfig {

        @Bean
        @Primary
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
