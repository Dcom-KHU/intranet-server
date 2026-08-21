package com.dcom.intranet.admin.service;

import com.dcom.intranet.admin.dto.request.AdminTransferAdminRequest;
import com.dcom.intranet.admin.dto.response.AdminDashboardResponse;
import com.dcom.intranet.admin.dto.response.AdminPendingUserListResponse;
import com.dcom.intranet.admin.dto.response.AdminTransferAdminResponse;
import com.dcom.intranet.admin.dto.response.AdminUserApproveResponse;
import com.dcom.intranet.admin.dto.response.AdminUserDetailResponse;
import com.dcom.intranet.admin.dto.response.AdminUserListResponse;
import com.dcom.intranet.admin.dto.response.AdminUserRejectResponse;
import com.dcom.intranet.admin.dto.response.AdminUserWithdrawResponse;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.auth.repository.EmailVerificationRepository;
import com.dcom.intranet.auth.repository.RefreshTokenRepository;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.auth.service.EmailService;
import com.dcom.intranet.global.exception.BadRequestException;
import com.dcom.intranet.info.repository.InfoCommentRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.mypage.repository.EmailChangeVerificationRepository;
import com.dcom.intranet.notice.repository.NoticeRepository;
import com.dcom.intranet.photo.repository.PhotoCommentRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NoticeRepository noticeRepository;
    private final PhotoPostRepository photoPostRepository;
    private final ArchiveRepository archiveRepository;
    private final InfoPostRepository infoPostRepository;
    private final EmailService emailService;
    private final InfoCommentRepository infoCommentRepository;
    private final ArchiveRecordRepository archiveRecordRepository;
    private final PhotoCommentRepository photoCommentRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailChangeVerificationRepository emailChangeVerificationRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long pendingUserCount = userRepository.countByStatus(UserStatus.PENDING);
        long totalUserCount = userRepository.countByStatus(UserStatus.APPROVED);

        List<AdminDashboardResponse.SignupRequestSummary> recentSignupRequests =
                userRepository.findTop5ByStatusOrderByCreatedAtDesc(UserStatus.PENDING).stream()
                        .map(user -> new AdminDashboardResponse.SignupRequestSummary(
                                user.getId(),
                                user.getName(),
                                user.getStudentId(),
                                user.getEmail(),
                                user.getCreatedAt()
                        ))
                        .toList();

        List<AdminDashboardResponse.RecentActiveMember> recentActiveMembers =
                userRepository.findTop3ByStatusOrderByLastLoginAtDesc(UserStatus.APPROVED).stream()
                        .map(user -> new AdminDashboardResponse.RecentActiveMember(
                                user.getId(),
                                user.getName(),
                                user.getStudentId(),
                                user.getLastLoginAt()
                        ))
                        .toList();

        return new AdminDashboardResponse(
                pendingUserCount,
                totalUserCount,
                recentSignupRequests,
                recentActiveMembers,
                getPostCounts()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse getUserList(String keyword, Pageable pageable) {
        Pageable stablePageable = stabilizeNameSort(pageable);
        Page<User> users = (keyword == null || keyword.isBlank())
                ? userRepository.findByStatus(UserStatus.APPROVED, stablePageable)
                : userRepository.findByStatusAndKeyword(
                        UserStatus.APPROVED,
                        keyword,
                        stablePageable
                );

        Page<AdminUserListResponse.UserSummary> page = users.map(user -> new AdminUserListResponse.UserSummary(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getStudentId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getLastLoginAt()
        ));

        return AdminUserListResponse.from(page);
    }

    private Pageable stabilizeNameSort(Pageable pageable) {
        Sort sort = pageable.getSort();
        Sort.Order nameOrder = sort.getOrderFor("name");

        if (nameOrder == null || !nameOrder.isAscending() || sort.getOrderFor("id") != null) {
            return pageable;
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "id"))
        );
    }

    @Transactional(readOnly = true)
    public AdminPendingUserListResponse getPendingUsers(Pageable pageable) {
        Page<AdminPendingUserListResponse.PendingUserSummary> page =
                userRepository.findByStatus(UserStatus.PENDING, pageable)
                        .map(user -> new AdminPendingUserListResponse.PendingUserSummary(
                                user.getId(),
                                user.getLoginId(),
                                user.getName(),
                                user.getStudentId(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getCreatedAt()
                        ));

        return AdminPendingUserListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        return AdminUserDetailResponse.from(findUserById(userId));
    }

    @Transactional
    public AdminUserApproveResponse approveUser(Long userId, String adminLoginId) {
        User admin = findUser(adminLoginId);
        User user = findUserById(userId);

        user.approve(admin.getId(), LocalDateTime.now());

        registerApprovalMailAfterCommit(user.getEmail(), user.getName());

        return new AdminUserApproveResponse(
                user.getId(),
                user.getStatus(),
                user.getApprovedAt(),
                user.getApprovedByAdminId()
        );
    }

    @Transactional
    public AdminUserRejectResponse rejectUser(Long userId, String adminLoginId) {
        User admin = findUser(adminLoginId);
        User user = findUserById(userId);

        AdminUserRejectResponse response = new AdminUserRejectResponse(
                user.getId(),
                "REJECTED",
                admin.getId(),
                LocalDateTime.now()
        );

        if (hasRetainedActivity(user)) {
            /// 기존 작성 기록의 작성자 참조를 보존하기 위해 탈퇴 상태로 유지
            refreshTokenRepository.deleteByLoginId(user.getLoginId());
            user.withdraw(LocalDateTime.now());
            return response;
        }

        /// 활동 이력이 없는 회원만 인증 부속 데이터를 정리한 뒤 물리 삭제
        cleanupAccountAttachments(user);
        userRepository.delete(user);

        return response;
    }

    @Transactional
    public AdminUserWithdrawResponse withdrawOrDeleteUser(Long userId, String adminLoginId) {
        User admin = findUser(adminLoginId);
        User target = findUserById(userId);

        validateAdminUserProcessing(admin, target);

        if (hasRetainedActivity(target)) {
            refreshTokenRepository.deleteByLoginId(target.getLoginId());
            target.withdraw(LocalDateTime.now());
            return AdminUserWithdrawResponse.withdrawn(target.getId());
        }

        cleanupAccountAttachments(target);
        Long deletedUserId = target.getId();
        userRepository.delete(target);
        userRepository.flush();

        return AdminUserWithdrawResponse.hardDeleted(deletedUserId);
    }

    @Transactional
    public AdminTransferAdminResponse transferAdmin(Long pathUserId, String authenticatedLoginId, AdminTransferAdminRequest request) {
        User currentAdmin = findUserById(pathUserId);

        if (!currentAdmin.getLoginId().equals(authenticatedLoginId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인 계정에 대해서만 권한을 이양할 수 있습니다."
            );
        }

        if (request.confirm() == null || !request.confirm()) {
            throw new BadRequestException("confirm=true로 요청해야 관리자 권한을 이양할 수 있습니다.");
        }

        if (request.targetUserId().equals(currentAdmin.getId())) {
            throw new BadRequestException("자기 자신에게는 관리자 권한을 이양할 수 없습니다.");
        }

        User target = findUserById(request.targetUserId());

        if (target.getStatus() != UserStatus.APPROVED) {
            throw new BadRequestException("승인된 회원에게만 관리자 권한을 이양할 수 있습니다.");
        }

        if (target.isAdmin()) {
            throw new BadRequestException("이미 관리자 권한을 가진 회원입니다.");
        }

        currentAdmin.changeRole(UserRole.USER);
        target.changeRole(UserRole.ADMIN);

        return new AdminTransferAdminResponse(
                currentAdmin.getId(),
                target.getId(),
                currentAdmin.getRole(),
                target.getRole(),
                LocalDateTime.now()
        );
    }

    private void validateAdminUserProcessing(User admin, User target) {
        if (admin.getId().equals(target.getId())) {
            throw new BadRequestException("관리자는 자기 자신을 탈퇴/삭제 처리할 수 없습니다.");
        }

        if (target.getStatus() == UserStatus.WITHDRAWN) {
            throw new BadRequestException("이미 탈퇴 처리된 회원입니다.");
        }

        if (target.getStatus() == UserStatus.PENDING) {
            throw new BadRequestException("가입 대기 회원은 가입 거절 API를 사용해주세요.");
        }

        if (target.isAdmin() && target.getStatus() == UserStatus.APPROVED
                && userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.APPROVED) <= 1) {
            throw new BadRequestException("마지막 관리자는 탈퇴/삭제 처리할 수 없습니다.");
        }
    }

    private boolean hasRetainedActivity(User user) {
        Long userId = user.getId();
        return infoPostRepository.existsByAuthorId(userId)
                || infoCommentRepository.existsByAuthorId(userId)
                || archiveRecordRepository.existsByAuthorId(userId)
                || noticeRepository.existsByAuthorId(userId)
                || photoPostRepository.existsByAuthorId(userId)
                || photoCommentRepository.existsByAuthorId(userId)
                || userRepository.existsByApprovedByAdminId(userId);
    }

    private void cleanupAccountAttachments(User user) {
        refreshTokenRepository.deleteByLoginId(user.getLoginId());
        emailVerificationRepository.deleteByLoginIdOrEmail(user.getLoginId(), user.getEmail());
        emailChangeVerificationRepository.deleteByLoginId(user.getLoginId());
    }

    private void registerApprovalMailAfterCommit(String email, String name) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailService.sendApprovalEmail(email, name);
                } catch (Exception e) {
                    /// 메일 발송 실패해도 승인 처리 자체는 이미 커밋됨
                    System.err.println("승인 안내 메일 발송 실패: " + email);
                }
            }
        });
    }

    private AdminDashboardResponse.PostCounts getPostCounts() {
        return new AdminDashboardResponse.PostCounts(
                noticeRepository.count(),
                archiveRepository.count(),
                infoPostRepository.count(),
                photoPostRepository.count()
        );
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));
    }
}
