package com.dcom.intranet.mypage.service;

import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.service.ArchiveService;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.info.service.InfoPostService;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.notice.domain.Notice;
import com.dcom.intranet.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompositeMyWrittenPostReaderTest {

    private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
    private final ArchiveRecordRepository archiveRecordRepository = mock(ArchiveRecordRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final InfoPostService infoPostService = mock(InfoPostService.class);
    private final ArchiveService archiveService = mock(ArchiveService.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);

    private final CompositeMyWrittenPostReader reader = new CompositeMyWrittenPostReader(
            infoPostRepository,
            archiveRecordRepository,
            userRepository,
            infoPostService,
            archiveService,
            noticeRepository
    );

    @Test
    @DisplayName("Read notices returns notice posts written by user")
    void readNoticesReturnsNoticePostsWrittenByUser() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 9, 9, 0);
        Notice notice = new Notice("관리자 공지", "내용", 1L, createdAt);
        ReflectionTestUtils.setField(notice, "noticeId", 51L);
        when(noticeRepository.findByAuthorId(1L))
                .thenReturn(List.of(notice));

        MyWrittenPostListResponse response = reader.read(1L, 0, 10, "notices");

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).id()).isEqualTo(51L);
        assertThat(response.posts().get(0).number()).isEqualTo(1);
        assertThat(response.posts().get(0).title()).isEqualTo("관리자 공지");
        assertThat(response.posts().get(0).type()).isEqualTo("notices");
        assertThat(response.posts().get(0).createdAt()).isEqualTo(createdAt);
    }
}
