package com.dcom.intranet.mypage.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.mypage.domain.EmailChangeVerification;
import com.dcom.intranet.mypage.domain.MyPageRouteType;
import com.dcom.intranet.mypage.dto.response.MemberWithdrawResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentTargetResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostTargetResponse;
import com.dcom.intranet.mypage.dto.request.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.request.PasswordChangeRequest;
import com.dcom.intranet.mypage.dto.response.MyProfileResponse;
import com.dcom.intranet.mypage.dto.response.MyProfileUpdateResponse;
import com.dcom.intranet.mypage.dto.response.PasswordChangeResponse;
import com.dcom.intranet.mypage.exception.MyPageApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class MyPageService {

    private static final String NOTICES = "notices";

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final MyWrittenPostReader myWrittenPostReader;
    private final MyWrittenCommentReader myWrittenCommentReader;

    public MyPageService(
            UserRepository userRepository,
            EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder,
            MyWrittenPostReader myWrittenPostReader,
            MyWrittenCommentReader myWrittenCommentReader
    ) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.myWrittenPostReader = myWrittenPostReader;
        this.myWrittenCommentReader = myWrittenCommentReader;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(String loginId) {
        User user = getApprovedUser(loginId);
        return MyProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MyWrittenPostListResponse getMyPosts(String loginId, int page, int size, String type) {
        User user = getApprovedUser(loginId);
        String normalizedType = MyPageRouteType.normalize(type);
        if (NOTICES.equals(normalizedType) && !user.isAdmin()) {
            throw new MyPageApiException(HttpStatus.FORBIDDEN, "공지사항은 관리자만 조회할 수 있습니다.");
        }
        return myWrittenPostReader.read(user.getId(), page, size, normalizedType);
    }

    @Transactional(readOnly = true)
    public MyWrittenCommentListResponse getMyComments(String loginId, int page, int size, String type) {
        User user = getApprovedUser(loginId);
        return myWrittenCommentReader.read(user.getId(), page, size, MyPageRouteType.normalize(type));
    }

    @Transactional(readOnly = true)
    public MyWrittenCommentTargetResponse getMyCommentTarget(String loginId, Long commentId, String type) {
        User user = getApprovedUser(loginId);
        return myWrittenCommentReader.readTarget(user.getId(), commentId, MyPageRouteType.normalize(type));
    }

    @Transactional
    public MyWrittenCommentDeleteResponse deleteMyComment(String loginId, Long commentId, String type) {
        User user = getApprovedUser(loginId);
        return myWrittenCommentReader.delete(user.getId(), commentId, MyPageRouteType.normalize(type));
    }

    @Transactional(readOnly = true)
    public MyWrittenPostTargetResponse getMyPostTarget(String loginId, Long postId, String type) {
        User user = getApprovedUser(loginId);
        return myWrittenPostReader.readTarget(user.getId(), postId, MyPageRouteType.normalize(type));
    }

    @Transactional
    public MyWrittenPostDeleteResponse deleteMyPost(String loginId, Long postId, String type) {
        User user = getApprovedUser(loginId);
        return myWrittenPostReader.delete(user.getId(), postId, MyPageRouteType.normalize(type));
    }

    @Transactional
    public MyProfileUpdateResponse updateMyProfile(String loginId, MyProfileUpdateRequest request) {
        User user = getApprovedUser(loginId);

        user.updateProfile(request.name(), request.phoneNumber());

        if (StringUtils.hasText(request.emailChangeToken())) {
            EmailChangeVerification verification =
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
        User user = getApprovedUser(loginId);

        if (!user.isTempPasswordValid()) {
            if (!StringUtils.hasText(request.currentPassword())) {
                throw new MyPageApiException(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new MyPageApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
            }
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return new PasswordChangeResponse("비밀번호가 변경되었습니다.");
    }

    @Transactional
    public MemberWithdrawResponse withdraw(String loginId) {
        User user = getApprovedUser(loginId);

        user.withdraw(LocalDateTime.now());
        return MemberWithdrawResponse.from(user);
    }

    private User getApprovedUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return user;
    }
}
