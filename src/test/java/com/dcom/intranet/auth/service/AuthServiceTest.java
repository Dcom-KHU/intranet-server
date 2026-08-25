package com.dcom.intranet.auth.service;

import com.dcom.intranet.auth.domain.RefreshToken;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.auth.dto.auth.RefreshRequest;
import com.dcom.intranet.auth.dto.auth.SignupRequest;
import com.dcom.intranet.auth.repository.RefreshTokenRepository;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.global.exception.UnauthorizedException;
import com.dcom.intranet.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final EmailService emailService = mock(EmailService.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserAccountLifecycleService userAccountLifecycleService = mock(UserAccountLifecycleService.class);

    private final AuthService authService = new AuthService(
            userRepository,
            passwordEncoder,
            jwtTokenProvider,
            emailService,
            refreshTokenRepository,
            userAccountLifecycleService
    );

    @Test
    @DisplayName("Check login id treats WITHDRAWN user login id as available")
    void checkLoginIdTreatsWithdrawnUserLoginIdAsAvailable() {
        when(userRepository.existsByLoginIdAndStatusNot("rejoin", UserStatus.WITHDRAWN))
                .thenReturn(false);

        var response = authService.checkLoginId("rejoin");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Signup reactivates WITHDRAWN user with same login id")
    void signupReactivatesWithdrawnUserWithSameLoginId() {
        SignupRequest request = signupRequest(
                "rejoin",
                "new-password",
                "재가입회원",
                "20249998",
                "rejoin-new@dcom.org",
                "010-9999-9998"
        );
        User withdrawnUser = user("rejoin", UserStatus.WITHDRAWN, UserRole.ADMIN);
        ReflectionTestUtils.setField(withdrawnUser, "id", 10L);
        when(userRepository.findByLoginId("rejoin")).thenReturn(Optional.of(withdrawnUser));
        when(userRepository.existsByLoginIdAndStatusNot("rejoin", UserStatus.WITHDRAWN)).thenReturn(false);
        when(userRepository.existsByStudentIdAndIdNot("20249998", 10L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("rejoin-new@dcom.org", 10L)).thenReturn(false);
        when(emailService.isEmailVerified("rejoin-new@dcom.org")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userAccountLifecycleService.hasRetainedActivity(withdrawnUser)).thenReturn(true);
        when(userRepository.save(withdrawnUser)).thenReturn(withdrawnUser);

        var response = authService.signup(request);

        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getLoginId()).isEqualTo("rejoin");
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(withdrawnUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(withdrawnUser.getStudentId()).isEqualTo("20249998");
        assertThat(withdrawnUser.getEmail()).isEqualTo("rejoin-new@dcom.org");
        assertThat(withdrawnUser.getWithdrawnAt()).isNull();
        verify(userRepository).save(withdrawnUser);
    }

    @Test
    @DisplayName("Signup hard deletes WITHDRAWN user without retained activity before creating new user")
    void signupHardDeletesWithdrawnUserWithoutRetainedActivityBeforeCreatingNewUser() {
        SignupRequest request = signupRequest(
                "rejoin-empty",
                "new-password",
                "재가입회원",
                "20249997",
                "rejoin-empty@dcom.org",
                "010-9999-9997"
        );
        User withdrawnUser = user("rejoin-empty", UserStatus.WITHDRAWN, UserRole.USER);
        ReflectionTestUtils.setField(withdrawnUser, "id", 11L);
        when(userRepository.findByLoginId("rejoin-empty")).thenReturn(Optional.of(withdrawnUser));
        when(userRepository.existsByLoginIdAndStatusNot("rejoin-empty", UserStatus.WITHDRAWN)).thenReturn(false);
        when(userRepository.existsByStudentIdAndIdNot("20249997", 11L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("rejoin-empty@dcom.org", 11L)).thenReturn(false);
        when(emailService.isEmailVerified("rejoin-empty@dcom.org")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userAccountLifecycleService.hasRetainedActivity(withdrawnUser)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", 12L);
            return savedUser;
        });

        var response = authService.signup(request);

        assertThat(response.getUserId()).isEqualTo(12L);
        assertThat(response.getLoginId()).isEqualTo("rejoin-empty");
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING);
        verify(userAccountLifecycleService).cleanupAccountAttachments(withdrawnUser);
        verify(userRepository).delete(withdrawnUser);
        verify(userRepository).flush();
    }

    @Test
    @DisplayName("Refresh rejects invalid JWT and deletes stored token")
    void refreshRejectsInvalidJwtAndDeletesStoredToken() {
        RefreshRequest request = refreshRequest("refresh-token");
        RefreshToken savedToken = new RefreshToken("refresh-token", "member", 1209600000L);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(savedToken));
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository).delete(savedToken);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh rejects token subject mismatch and deletes stored token")
    void refreshRejectsTokenSubjectMismatchAndDeletesStoredToken() {
        RefreshRequest request = refreshRequest("refresh-token");
        RefreshToken savedToken = new RefreshToken("refresh-token", "member", 1209600000L);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(savedToken));
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getLoginId("refresh-token")).thenReturn("other-member");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository).delete(savedToken);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh rejects non approved user and deletes all refresh tokens")
    void refreshRejectsNonApprovedUserAndDeletesAllRefreshTokens() {
        RefreshRequest request = refreshRequest("refresh-token");
        RefreshToken savedToken = new RefreshToken("refresh-token", "member", 1209600000L);
        User withdrawnUser = user("member", UserStatus.WITHDRAWN, UserRole.USER);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(savedToken));
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getLoginId("refresh-token")).thenReturn("member");
        when(userRepository.findByLoginId("member")).thenReturn(Optional.of(withdrawnUser));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository).deleteByLoginId("member");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh issues new tokens with current database role")
    void refreshIssuesNewTokensWithCurrentDatabaseRole() {
        RefreshRequest request = refreshRequest("refresh-token");
        RefreshToken savedToken = new RefreshToken("refresh-token", "member", 1209600000L);
        User adminUser = user("member", UserStatus.APPROVED, UserRole.ADMIN);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(savedToken));
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getLoginId("refresh-token")).thenReturn("member");
        when(userRepository.findByLoginId("member")).thenReturn(Optional.of(adminUser));
        when(jwtTokenProvider.createAccessToken("member", "ADMIN")).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken("member", "ADMIN")).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(1209600000L);
        when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(1800000L);

        var response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(1800);
        verify(refreshTokenRepository).delete(savedToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    private RefreshRequest refreshRequest(String refreshToken) {
        RefreshRequest request = new RefreshRequest();
        ReflectionTestUtils.setField(request, "refreshToken", refreshToken);
        return request;
    }

    private SignupRequest signupRequest(String loginId, String password, String name,
                                        String studentId, String email, String phoneNumber) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "loginId", loginId);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "phoneNumber", phoneNumber);
        return request;
    }

    private User user(String loginId, UserStatus status, UserRole role) {
        User user = new User(
                loginId,
                "encoded-password",
                "테스트회원",
                "20990001",
                loginId + "@dcom.org",
                "010-0000-0001"
        );
        ReflectionTestUtils.setField(user, "status", status);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }
}
