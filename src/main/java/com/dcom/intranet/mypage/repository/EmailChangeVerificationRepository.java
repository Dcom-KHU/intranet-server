package com.dcom.intranet.mypage.repository;

import com.dcom.intranet.mypage.domain.EmailChangeVerification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailChangeVerificationRepository extends JpaRepository<EmailChangeVerification, Long> {

    Optional<EmailChangeVerification> findTopByLoginIdAndEmailOrderByCreatedAtDesc(String loginId, String email);

    Optional<EmailChangeVerification> findByEmailChangeToken(String emailChangeToken);
}
