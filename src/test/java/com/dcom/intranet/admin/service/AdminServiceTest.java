package com.dcom.intranet.admin.service;

import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.auth.service.EmailService;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.notice.repository.NoticeRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final PhotoPostRepository photoPostRepository = mock(PhotoPostRepository.class);
    private final ArchiveRepository archiveRepository = mock(ArchiveRepository.class);
    private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
    private final EmailService emailService = mock(EmailService.class);

    private final AdminService adminService = new AdminService(
            userRepository,
            noticeRepository,
            photoPostRepository,
            archiveRepository,
            infoPostRepository,
            emailService
    );

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
}
