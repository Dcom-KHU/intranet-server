package com.dcom.intranet.mypage.service;

import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.mypage.domain.EmailChangeVerification;
import com.dcom.intranet.mypage.dto.response.EmailVerificationSendResponse;
import com.dcom.intranet.mypage.dto.response.EmailVerificationVerifyResponse;
import com.dcom.intranet.mypage.exception.MyPageApiException;
import com.dcom.intranet.mypage.repository.EmailChangeVerificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final int EXPIRES_IN_SECONDS = 300;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailChangeVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    public EmailVerificationService(
            EmailChangeVerificationRepository emailVerificationRepository,
            UserRepository userRepository
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EmailVerificationSendResponse sendEmailChangeVerification(String loginId, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new MyPageApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        emailVerificationRepository.findTopByLoginIdAndEmailOrderByCreatedAtDesc(loginId, newEmail)
                .filter(verification -> verification.isActive(now))
                .ifPresent(verification -> {
                    throw new MyPageApiException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "이미 진행 중인 이메일 인증 요청이 있습니다."
                    );
                });

        EmailChangeVerification verification = EmailChangeVerification.create(
                loginId,
                newEmail,
                generateVerificationCode(),
                now.plusSeconds(EXPIRES_IN_SECONDS)
        );
        emailVerificationRepository.save(verification);

        return new EmailVerificationSendResponse(
                "이메일 변경 인증 코드가 생성되었습니다.",
                EXPIRES_IN_SECONDS
        );
    }

    @Transactional
    public EmailVerificationVerifyResponse verifyEmailChangeCode(
            String loginId,
            String newEmail,
            String verificationCode
    ) {
        EmailChangeVerification verification = emailVerificationRepository
                .findTopByLoginIdAndEmailOrderByCreatedAtDesc(loginId, newEmail)
                .orElseThrow(() -> new MyPageApiException(
                        HttpStatus.BAD_REQUEST,
                        "이메일 인증 요청을 찾을 수 없습니다."
                ));

        LocalDateTime now = LocalDateTime.now();
        if (verification.isExpired(now)) {
            throw new MyPageApiException(HttpStatus.GONE, "이메일 인증이 만료되었습니다.");
        }

        if (!verification.matchesCode(verificationCode)) {
            throw new MyPageApiException(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다.");
        }

        String emailChangeToken = UUID.randomUUID().toString();
        verification.verify(emailChangeToken);

        return new EmailVerificationVerifyResponse(
                emailChangeToken,
                "이메일 변경 인증이 완료되었습니다.",
                verification.getEmail()
        );
    }

    @Transactional
    public EmailChangeVerification consumeEmailChangeToken(String loginId, String emailChangeToken) {
        EmailChangeVerification verification = emailVerificationRepository.findByEmailChangeToken(emailChangeToken)
                .filter(candidate -> candidate.belongsTo(loginId))
                .filter(EmailChangeVerification::canChangeEmail)
                .orElseThrow(() -> new MyPageApiException(
                        HttpStatus.BAD_REQUEST,
                        "이메일 변경 토큰이 올바르지 않습니다."
                ));

        if (verification.isExpired(LocalDateTime.now())) {
            throw new MyPageApiException(HttpStatus.GONE, "이메일 인증이 만료되었습니다.");
        }

        verification.markUsed();
        return verification;
    }

    private String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
