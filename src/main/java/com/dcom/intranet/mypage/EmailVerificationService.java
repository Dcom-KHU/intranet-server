package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.EmailVerificationSendResponse;
import com.dcom.intranet.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private static final int EXPIRES_IN_SECONDS = 300;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
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

        EmailVerification verification = EmailVerification.create(
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

    private String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
