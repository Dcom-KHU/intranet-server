package com.dcom.intranet.jwt;

import com.dcom.intranet.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    @Test
    void authMeRequestShouldBeFiltered() {
        TestableJwtAuthenticationFilter filter =
                new TestableJwtAuthenticationFilter(mock(JwtTokenProvider.class), mock(UserRepository.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");

        assertThat(filter.shouldSkip(request)).isFalse();
    }

    @Test
    void publicAuthRequestsShouldNotBeFiltered() {
        TestableJwtAuthenticationFilter filter =
                new TestableJwtAuthenticationFilter(mock(JwtTokenProvider.class), mock(UserRepository.class));

        assertThat(filter.shouldSkip(new MockHttpServletRequest("POST", "/api/auth/login"))).isTrue();
        assertThat(filter.shouldSkip(new MockHttpServletRequest("POST", "/api/auth/signup"))).isTrue();
        assertThat(filter.shouldSkip(new MockHttpServletRequest("GET", "/api/auth/check-login-id"))).isTrue();
    }

    private static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {

        private TestableJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
            super(jwtTokenProvider, userRepository);
        }

        private boolean shouldSkip(MockHttpServletRequest request) {
            return shouldNotFilter(request);
        }
    }
}
