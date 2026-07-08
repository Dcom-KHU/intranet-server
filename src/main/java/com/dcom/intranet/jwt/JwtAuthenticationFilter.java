package com.dcom.intranet.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private  final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/swagger-ui")
                || path.equals("/api/swagger-ui.html")
                || path.startsWith("/api/v3/api-docs")
                || isPublicAuthPath(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException{
        try {
            /// 헤더에서 토큰 꺼내기
            String token = resolveToken(request);

            /// 토큰이 유효하면 인증 정보 세팅
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String loginId = jwtTokenProvider.getLoginId(token);
                String role = jwtTokenProvider.getRole(token);

                /// 스프링 시큐리티에 인증된 사람이란 것을 알려주기
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginId, /// 누구인지
                                null, /// 비번 -> 이미 검증되었으니까 필요 없다.
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)) /// 권한
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception e){
            /// 토큰이 잘못되었거나 만료된경우 인증안된상태로 통과
            /// 어차피 나중에 시큐리티가 401처리를 함
            SecurityContextHolder.clearContext();
        }
        /// 다음필터로 넘기기
        filterChain.doFilter(request, response);

    }

    /// Authorization 헤더에서 Bearer토큰 추출
    private String resolveToken(HttpServletRequest request){
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")){
            return bearer.substring(7);
            /// 헤더에서 "Bearer "의 7개 잘라내고 그 뒤로부터 토큰을 읽는다.
        }
        return null;
    }

    private boolean isPublicAuthPath(String path) {
        return path.equals("/api/auth/signup")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/check-login-id")
                || path.startsWith("/api/auth/email/")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/logout")
                || path.startsWith("/api/auth/password/reset/");
    }

}
