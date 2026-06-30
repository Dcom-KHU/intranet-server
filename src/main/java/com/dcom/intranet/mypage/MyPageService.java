package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyProfileResponse;
import com.dcom.intranet.mypage.dto.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.MyProfileUpdateResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostTargetResponse;
import com.dcom.intranet.mypage.dto.PasswordChangeRequest;
import com.dcom.intranet.mypage.dto.PasswordChangeResponse;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final MyWrittenPostReader myWrittenPostReader;

    public MyPageService(
            UserRepository userRepository,
            EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder,
            MyWrittenPostReader myWrittenPostReader
    ) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.myWrittenPostReader = myWrittenPostReader;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return MyProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MyWrittenPostListResponse getMyPosts(String loginId, int page, int size, String type) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return myWrittenPostReader.read(user.getId(), page, size, type);
    }

    @Transactional(readOnly = true)
    public MyWrittenPostTargetResponse getMyPostTarget(String loginId, Long postId, String type) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return myWrittenPostReader.readTarget(user.getId(), postId, type);
    }

    @Transactional
    public MyWrittenPostDeleteResponse deleteMyPost(String loginId, Long postId, String type) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        return myWrittenPostReader.delete(user.getId(), postId, type);
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

    @Transactional
    public PasswordChangeResponse changePassword(String loginId, PasswordChangeRequest request) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new MyPageApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return new PasswordChangeResponse("비밀번호가 변경되었습니다.");
    }
}
