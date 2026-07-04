package com.dcom.intranet.home.service;

import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.home.dto.ArchiveSummaryResponse;
import com.dcom.intranet.home.dto.AuthorResponse;
import com.dcom.intranet.home.dto.HomeDashboardResponse;
import com.dcom.intranet.home.dto.InfoPostSummaryResponse;
import com.dcom.intranet.home.dto.NoticeSummaryResponse;
import com.dcom.intranet.home.dto.PhotoAlbumSummaryResponse;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.notice.repository.NoticeRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int RECENT_SIZE = 5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final NoticeRepository noticeRepository;
    private final ArchiveRecordRepository archiveRecordRepository;
    private final InfoPostRepository infoPostRepository;
    private final PhotoPostRepository photoPostRepository;
    private final UserRepository userRepository;

    public HomeDashboardResponse getHomeDashboard() {
        return new HomeDashboardResponse(
                recentNotices(),
                recentArchives(),
                recentInfoPosts(),
                recentPhotoAlbums()
        );
    }

    private List<NoticeSummaryResponse> recentNotices() {
        Pageable pageable = PageRequest.of(0, RECENT_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        return noticeRepository.findAll(pageable).getContent().stream()
                .map(notice -> new NoticeSummaryResponse(
                        notice.getNoticeId(),
                        notice.getTitle(),
                        resolveAuthorName(notice.getAuthorId()),
                        notice.getCreatedAt().format(DATE_FORMATTER),
                        !notice.getFiles().isEmpty()
                ))
                .toList();
    }

    private List<ArchiveSummaryResponse> recentArchives() {
        Pageable pageable = PageRequest.of(0, RECENT_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        return archiveRecordRepository.findAll(pageable).getContent().stream()
                .map(record -> new ArchiveSummaryResponse(
                        record.getId(),
                        record.getArchive().getSubjectName(),
                        record.getArchive().getProfessorName(),
                        new AuthorResponse(record.getAuthor().getStudentId(), record.getAuthor().getName()),
                        record.getCreatedAt().format(DATE_FORMATTER)
                ))
                .toList();
    }

    private List<InfoPostSummaryResponse> recentInfoPosts() {
        Pageable pageable = PageRequest.of(0, RECENT_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        return infoPostRepository.findAll(pageable).getContent().stream()
                .map(post -> new InfoPostSummaryResponse(
                        post.getId(),
                        post.getTitle(),
                        new AuthorResponse(post.getAuthor().getStudentId(), post.getAuthor().getName()),
                        post.getCreatedAt().format(DATE_FORMATTER),
                        !post.getFiles().isEmpty()
                ))
                .toList();
    }

    private List<PhotoAlbumSummaryResponse> recentPhotoAlbums() {
        Pageable pageable = PageRequest.of(0, RECENT_SIZE, Sort.by(Sort.Direction.DESC, "activityDate"));

        return photoPostRepository.findAll(pageable).getContent().stream()
                .map(photoPost -> new PhotoAlbumSummaryResponse(
                        photoPost.getAlbumId(),
                        photoPost.getCoverImageUrl(),
                        photoPost.getEventName(),
                        photoPost.getActivityDate().format(DATE_FORMATTER),
                        photoPost.getImages().size()
                ))
                .toList();
    }

    private String resolveAuthorName(Long authorId) {
        if (authorId == null) {
            return "알 수 없음";
        }

        return userRepository.findById(authorId)
                .map(User::getName)
                .orElse("알 수 없음");
    }
}
