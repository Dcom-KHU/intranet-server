package com.dcom.intranet.mypage.service;

import com.dcom.intranet.archive.domain.Archive;
import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.service.ArchiveService;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.info.service.InfoPostService;
import com.dcom.intranet.mypage.exception.MyPageApiException;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.notice.domain.Notice;
import com.dcom.intranet.notice.repository.NoticeRepository;
import com.dcom.intranet.notice.service.NoticeService;
import com.dcom.intranet.photo.domain.PhotoPost;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import com.dcom.intranet.photo.service.PhotoPostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompositeMyWrittenPostReaderTest {

    private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
    private final ArchiveRecordRepository archiveRecordRepository = mock(ArchiveRecordRepository.class);
    private final PhotoPostRepository photoPostRepository = mock(PhotoPostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final InfoPostService infoPostService = mock(InfoPostService.class);
    private final ArchiveService archiveService = mock(ArchiveService.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final PhotoPostService photoPostService = mock(PhotoPostService.class);
    private final NoticeService noticeService = mock(NoticeService.class);

    private final CompositeMyWrittenPostReader reader = new CompositeMyWrittenPostReader(
            infoPostRepository,
            archiveRecordRepository,
            photoPostRepository,
            userRepository,
            infoPostService,
            archiveService,
            noticeRepository,
            photoPostService,
            noticeService
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
        assertThat(response.posts().get(0).title()).isEqualTo("관리자 공지");
        assertThat(response.posts().get(0).type()).isEqualTo("notices");
        assertThat(response.posts().get(0).createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Read photo posts returns photo albums written by user")
    void readPhotoPostsReturnsPhotoAlbumsWrittenByUser() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 14, 20);
        User author = new User("former-admin", "password", "name", "20260001", "former@example.com", "010-0000-0000");
        ReflectionTestUtils.setField(author, "id", 1L);
        PhotoPost photoPost = new PhotoPost(author, "MT", LocalDate.of(2026, 7, 10), "description", List.of());
        ReflectionTestUtils.setField(photoPost, "albumId", 77L);
        ReflectionTestUtils.setField(photoPost, "createdAt", createdAt);
        when(photoPostRepository.findByAuthorId(1L))
                .thenReturn(List.of(photoPost));

        MyWrittenPostListResponse response = reader.read(1L, 0, 10, "photo-posts");

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).id()).isEqualTo(77L);
        assertThat(response.posts().get(0).title()).isEqualTo("MT");
        assertThat(response.posts().get(0).type()).isEqualTo("photo-posts");
        assertThat(response.posts().get(0).createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Delete photo post returns 403 when writer is no longer admin")
    void deletePhotoPostReturns403WhenWriterIsNoLongerAdmin() {
        User user = user(1L, UserRole.USER);
        PhotoPost photoPost = photoPost(user, 77L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(photoPostRepository.findById(77L)).thenReturn(Optional.of(photoPost));

        assertThatThrownBy(() -> reader.delete(1L, 77L, "photo-posts"))
                .isInstanceOfSatisfying(MyPageApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(photoPostService, never()).deletePhotoPost(77L);
    }

    @Test
    @DisplayName("Delete photo post succeeds when writer is admin")
    void deletePhotoPostSucceedsWhenWriterIsAdmin() {
        User admin = user(1L, UserRole.ADMIN);
        PhotoPost photoPost = photoPost(admin, 77L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(photoPostRepository.findById(77L)).thenReturn(Optional.of(photoPost));

        reader.delete(1L, 77L, "photo-posts");

        verify(photoPostService).deletePhotoPost(77L);
    }

    @Test
    @DisplayName("Delete notice returns 403 when writer is no longer admin")
    void deleteNoticeReturns403WhenWriterIsNoLongerAdmin() {
        User user = user(1L, UserRole.USER);
        Notice notice = new Notice("Former admin notice", "content", 1L, LocalDateTime.of(2026, 7, 12, 9, 0));
        ReflectionTestUtils.setField(notice, "noticeId", 51L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noticeRepository.findById(51L)).thenReturn(Optional.of(notice));

        assertThatThrownBy(() -> reader.delete(1L, 51L, "notices"))
                .isInstanceOfSatisfying(MyPageApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(noticeService, never()).deleteNotice(51L);
    }

    @Test
    @DisplayName("Delete notice succeeds when writer is admin")
    void deleteNoticeSucceedsWhenWriterIsAdmin() {
        User admin = user(1L, UserRole.ADMIN);
        Notice notice = new Notice("Admin notice", "content", 1L, LocalDateTime.of(2026, 7, 12, 9, 0));
        ReflectionTestUtils.setField(notice, "noticeId", 51L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(noticeRepository.findById(51L)).thenReturn(Optional.of(notice));

        reader.delete(1L, 51L, "notices");

        verify(noticeService).deleteNotice(51L);
    }

    @Test
    @DisplayName("Read archives returns record id, subject, and professor")
    void readArchivesReturnsRecordIdSubjectAndProfessor() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 10, 1, 31, 23);
        Archive archive = new Archive("객체지향프로그래밍", "최진우");
        ReflectionTestUtils.setField(archive, "id", 159L);
        ArchiveRecord record = new ArchiveRecord(null, null, null, null, "내용");
        ReflectionTestUtils.setField(record, "id", 467L);
        ReflectionTestUtils.setField(record, "archive", archive);
        ReflectionTestUtils.setField(record, "createdAt", createdAt);
        when(archiveRecordRepository.findByAuthorId(1L))
                .thenReturn(List.of(record));

        MyWrittenPostListResponse response = reader.read(1L, 0, 10, "archives");

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.posts()).hasSize(1);
        assertThat(response.posts().get(0).id()).isEqualTo(467L);
        assertThat(response.posts().get(0).recordId()).isEqualTo(467L);
        assertThat(response.posts().get(0).title()).isEqualTo("객체지향프로그래밍");
        assertThat(response.posts().get(0).professor()).isEqualTo("최진우");
        assertThat(response.posts().get(0).type()).isEqualTo("archives");
        assertThat(response.posts().get(0).createdAt()).isEqualTo(createdAt);
    }

    private User user(Long id, UserRole role) {
        User user = new User("user" + id, "password", "name", "2026000" + id, "user" + id + "@example.com", "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", id);
        user.changeRole(role);
        return user;
    }

    private PhotoPost photoPost(User author, Long albumId) {
        PhotoPost photoPost = new PhotoPost(author, "MT", LocalDate.of(2026, 7, 10), "description", List.of());
        ReflectionTestUtils.setField(photoPost, "albumId", albumId);
        return photoPost;
    }
}
