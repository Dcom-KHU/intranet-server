package com.dcom.intranet.auth.repository;

import com.dcom.intranet.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /// 재 발급시 토큰 찾기
    Optional<RefreshToken> findByToken(String token);

    /// 로그아웃시에 토큰 삭제
    void deleteByToken(String token);

    /// 해당 사용자의 모든 토큰 삭제(전체 로그아웃)
    void deleteByLoginId(String loginId);

}
