package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyProfileResponse;
import com.dcom.intranet.mypage.dto.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.MyProfileUpdateResponse;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    public MyPageService(UserRepository userRepository, EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return MyProfileResponse.from(user);
    }

    @Transactional
    public MyProfileUpdateResponse updateMyProfile(String loginId, MyProfileUpdateRequest request) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));

        user.updateProfile(request.name(), request.phoneNumber());

        if (StringUtils.hasText(request.emailChangeToken())) {
            EmailVerification verification =
                    emailVerificationService.consumeEmailChangeToken(loginId, request.emailChangeToken());
            if (userRepository.existsByEmail(verification.getEmail())) {
                throw new MyPageApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
            }
            user.changeEmail(verification.getEmail());
        }

        return MyProfileUpdateResponse.from(user);
    }
}
