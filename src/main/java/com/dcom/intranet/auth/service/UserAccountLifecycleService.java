package com.dcom.intranet.auth.service;

import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.EmailVerificationRepository;
import com.dcom.intranet.auth.repository.RefreshTokenRepository;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.repository.InfoCommentRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.mypage.repository.EmailChangeVerificationRepository;
import com.dcom.intranet.notice.repository.NoticeRepository;
import com.dcom.intranet.photo.repository.PhotoCommentRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountLifecycleService {

    private final InfoPostRepository infoPostRepository;
    private final InfoCommentRepository infoCommentRepository;
    private final ArchiveRecordRepository archiveRecordRepository;
    private final NoticeRepository noticeRepository;
    private final PhotoPostRepository photoPostRepository;
    private final PhotoCommentRepository photoCommentRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailChangeVerificationRepository emailChangeVerificationRepository;

    public boolean hasRetainedActivity(User user) {
        Long userId = user.getId();
        return infoPostRepository.existsByAuthorId(userId)
                || infoCommentRepository.existsByAuthorId(userId)
                || archiveRecordRepository.existsByAuthorId(userId)
                || noticeRepository.existsByAuthorId(userId)
                || photoPostRepository.existsByAuthorId(userId)
                || photoCommentRepository.existsByAuthorId(userId)
                || userRepository.existsByApprovedByAdminId(userId);
    }

    public void cleanupAccountAttachments(User user) {
        refreshTokenRepository.deleteByLoginId(user.getLoginId());
        emailVerificationRepository.deleteByLoginIdOrEmail(user.getLoginId(), user.getEmail());
        emailChangeVerificationRepository.deleteByLoginId(user.getLoginId());
    }

    public void cleanupSessions(User user) {
        refreshTokenRepository.deleteByLoginId(user.getLoginId());
    }
}
