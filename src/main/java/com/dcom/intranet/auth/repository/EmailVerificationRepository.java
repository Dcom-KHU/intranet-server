package com.dcom.intranet.auth.repository;

import com.dcom.intranet.auth.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    /// findtop : 한개만 가져와 / ByEmail : email이 일치하는 것중에 / OrderByCreatedAtDesc : 가장 최근 것.
    /// 이 이메일로 가장 최근에 보낸 인증코드를 가져와라.
}
