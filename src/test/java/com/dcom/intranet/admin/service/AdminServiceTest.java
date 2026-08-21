package com.dcom.intranet.admin.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final PhotoPostRepository photoPostRepository = mock(PhotoPostRepository.class);
    private final ArchiveRepository archiveRepository = mock(ArchiveRepository.class);
    private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final InfoCommentRepository infoCommentRepository = mock(InfoCommentRepository.class);
    private final ArchiveRecordRepository archiveRecordRepository = mock(ArchiveRecordRepository.class);
    private final PhotoCommentRepository photoCommentRepository = mock(PhotoCommentRepository.class);
    private final EmailVerificationRepository emailVerificationRepository = mock(EmailVerificationRepository.class);
    private final EmailChangeVerificationRepository emailChangeVerificationRepository = mock(EmailChangeVerificationRepository.class);

    private final AdminService adminService = new AdminService(
            userRepository,
            refreshTokenRepository,
            noticeRepository,
            photoPostRepository,
            archiveRepository,
            infoPostRepository,
            emailService,
            infoCommentRepository,
            archiveRecordRepository,
            photoCommentRepository,
            emailVerificationRepository,
            emailChangeVerificationRepository
    );

    @Test
    @DisplayName("Dashboard total user count includes only approved users")
    void dashboardTotalUserCountIncludesOnlyApprovedUsers() {
        when(userRepository.countByStatus(UserStatus.PENDING)).thenReturn(3L);
        when(userRepository.countByStatus(UserStatus.APPROVED)).thenReturn(12L);

        var response = adminService.getDashboard();

        assertThat(response.pendingUserCount()).isEqualTo(3L);
        assertThat(response.totalUserCount()).isEqualTo(12L);
        verify(userRepository).countByStatus(UserStatus.PENDING);
        verify(userRepository).countByStatus(UserStatus.APPROVED);
        verify(userRepository, never()).count();
    }

    @Test
    @DisplayName("Name ascending sort adds id ascending tie-breaker")
    void nameAscendingSortAddsIdAscendingTieBreaker() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        when(userRepository.findByStatus(eq(UserStatus.APPROVED), any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1)));

        adminService.getUserList(null, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByStatus(eq(UserStatus.APPROVED), captor.capture());
        assertThat(captor.getValue().getSort().stream().toList()).containsExactly(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
        );
    }

    @Test
    @DisplayName("Keyword search applies the same stable name sort")
    void keywordSearchAppliesTheSameStableNameSort() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        when(userRepository.findByStatusAndKeyword(
                eq(UserStatus.APPROVED), eq("김"), any(Pageable.class)
        )).thenAnswer(invocation -> Page.empty(invocation.getArgument(2)));

        adminService.getUserList("김", requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByStatusAndKeyword(
                eq(UserStatus.APPROVED), eq("김"), captor.capture()
        );
        assertThat(captor.getValue().getSort().stream().toList()).containsExactly(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
        );
    }

    @Test
    @DisplayName("Non-name sort is passed to repository unchanged")
    void nonNameSortIsPassedToRepositoryUnchanged() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastLoginAt"));
        when(userRepository.findByStatus(eq(UserStatus.APPROVED), any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1)));

        adminService.getUserList(null, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByStatus(eq(UserStatus.APPROVED), captor.capture());
        assertThat(captor.getValue()).isSameAs(requested);
    }

    @Test
    @DisplayName("가입 거절 시 족보 작성 이력이 있으면 회원을 탈퇴 상태로 유지한다")
    void rejectUserWithdrawsUserWithArchiveRecord() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.PENDING, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(archiveRecordRepository.existsByAuthorId(2L)).thenReturn(true);

        var response = adminService.rejectUser(2L, "admin");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.rejectedByAdminId()).isEqualTo(1L);
        assertThat(target.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(refreshTokenRepository).deleteByLoginId("target");
        verify(userRepository, never()).delete(target);
    }

    @Test
    @DisplayName("가입 거절 시 활동 이력이 없으면 회원과 인증 부속 데이터를 물리 삭제한다")
    void rejectUserHardDeletesUserWithoutRetainedActivity() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.PENDING, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.rejectUser(2L, "admin");

        verify(refreshTokenRepository).deleteByLoginId("target");
        verify(emailVerificationRepository).deleteByLoginIdOrEmail("target", "target@dcom.org");
        verify(emailChangeVerificationRepository).deleteByLoginId("target");
        verify(userRepository).delete(target);
    }

    @Test
    @DisplayName("Admin user processing hard deletes user without retained activity")
    void adminUserProcessingHardDeletesUserWithoutRetainedActivity() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.APPROVED, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        var response = adminService.withdrawOrDeleteUser(2L, "admin");

        assertThat(response.result()).isEqualTo("HARD_DELETED");
        verify(refreshTokenRepository).deleteByLoginId("target");
        verify(emailVerificationRepository).deleteByLoginIdOrEmail("target", "target@dcom.org");
        verify(emailChangeVerificationRepository).deleteByLoginId("target");
        verify(userRepository).delete(target);
        verify(userRepository).flush();
    }

    @Test
    @DisplayName("Admin user processing withdraws user with retained activity")
    void adminUserProcessingWithdrawsUserWithRetainedActivity() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.APPROVED, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(infoPostRepository.existsByAuthorId(2L)).thenReturn(true);

        var response = adminService.withdrawOrDeleteUser(2L, "admin");

        assertThat(response.result()).isEqualTo("WITHDRAWN");
        assertThat(target.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(target.getWithdrawnAt()).isNotNull();
        verify(refreshTokenRepository).deleteByLoginId("target");
        verify(userRepository, never()).delete(target);
        verify(userRepository, never()).flush();
        verify(emailVerificationRepository, never()).deleteByLoginIdOrEmail(any(), any());
        verify(emailChangeVerificationRepository, never()).deleteByLoginId(any());
    }

    @Test
    @DisplayName("Admin user processing treats photo post authorship as retained activity")
    void adminUserProcessingTreatsPhotoPostAuthorshipAsRetainedActivity() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.APPROVED, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(photoPostRepository.existsByAuthorId(2L)).thenReturn(true);

        var response = adminService.withdrawOrDeleteUser(2L, "admin");

        assertThat(response.result()).isEqualTo("WITHDRAWN");
        assertThat(target.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(refreshTokenRepository).deleteByLoginId("target");
        verify(userRepository, never()).delete(target);
    }

    @Test
    @DisplayName("Admin user processing rejects self processing")
    void adminUserProcessingRejectsSelfProcessing() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.withdrawOrDeleteUser(1L, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("자기 자신");
    }

    @Test
    @DisplayName("Admin user processing separates pending users from withdraw API")
    void adminUserProcessingSeparatesPendingUsersFromWithdrawApi() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "target", UserStatus.PENDING, UserRole.USER);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminService.withdrawOrDeleteUser(2L, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("가입 거절 API");
    }

    @Test
    @DisplayName("Admin user processing rejects last approved admin")
    void adminUserProcessingRejectsLastApprovedAdmin() {
        User admin = user(1L, "admin", UserStatus.APPROVED, UserRole.ADMIN);
        User target = user(2L, "targetAdmin", UserStatus.APPROVED, UserRole.ADMIN);
        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.APPROVED)).thenReturn(1L);

        assertThatThrownBy(() -> adminService.withdrawOrDeleteUser(2L, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("마지막 관리자");
    }

    private User user(Long id, String loginId, UserStatus status, UserRole role) {
        User user = new User(
                loginId,
                "encoded-password",
                "테스트회원" + id,
                "2099%04d".formatted(id),
                loginId + "@dcom.org",
                "010-0000-%04d".formatted(id)
        );
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "status", status);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }
}
