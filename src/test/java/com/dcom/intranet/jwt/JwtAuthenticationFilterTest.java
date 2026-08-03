package com.dcom.intranet.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    @Test
    void authMeRequestShouldBeFiltered() {
        TestableJwtAuthenticationFilter filter =
                new TestableJwtAuthenticationFilter(mock(JwtTokenProvider.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");

        assertThat(filter.shouldSkip(request)).isFalse();
    }

    @Test
    void publicAuthRequestsShouldNotBeFiltered() {
        TestableJwtAuthenticationFilter filter =
                new TestableJwtAuthenticationFilter(mock(JwtTokenProvider.class));

        assertThat(filter.shouldSkip(new MockHttpServletRequest("POST", "/api/auth/login"))).isTrue();
        assertThat(filter.shouldSkip(new MockHttpServletRequest("POST", "/api/auth/signup"))).isTrue();
        assertThat(filter.shouldSkip(new MockHttpServletRequest("GET", "/api/auth/check-login-id"))).isTrue();
    }

    private static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {

        private TestableJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
            super(jwtTokenProvider);
        }

        private boolean shouldSkip(MockHttpServletRequest request) {
            return shouldNotFilter(request);
        }
    }
}
