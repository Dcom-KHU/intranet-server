package com.dcom.intranet.auth.repository;

import com.dcom.intranet.auth.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerification> findTopByLoginIdAndEmailOrderByCreatedAtDesc(String loginId, String email);

    Optional<EmailVerification> findByEmailChangeToken(String emailChangeToken);
}