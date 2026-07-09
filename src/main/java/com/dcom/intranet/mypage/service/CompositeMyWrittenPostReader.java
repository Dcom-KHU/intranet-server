package com.dcom.intranet.mypage.service;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.service.ArchiveService;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.domain.InfoPost;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.info.service.InfoPostService;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostTargetResponse;
import com.dcom.intranet.mypage.dto.response.PageInfoResponse;
import com.dcom.intranet.mypage.exception.MyPageApiException;
import com.dcom.intranet.notice.repository.NoticeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CompositeMyWrittenPostReader implements MyWrittenPostReader {

    private static final String INFO_POSTS = "info-posts";
    private static final String ARCHIVES = "archives";
    private static final String NOTICES = "notices";

    private final InfoPostRepository infoPostRepository;
    private final ArchiveRecordRepository archiveRecordRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final InfoPostService infoPostService;
    private final ArchiveService archiveService;

    public CompositeMyWrittenPostReader(
            InfoPostRepository infoPostRepository,
            ArchiveRecordRepository archiveRecordRepository,
            UserRepository userRepository,
            InfoPostService infoPostService,
            ArchiveService archiveService,
            NoticeRepository noticeRepository
    ) {
        this.infoPostRepository = infoPostRepository;
        this.archiveRecordRepository = archiveRecordRepository;
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.infoPostService = infoPostService;
        this.archiveService = archiveService;
    }

    @Override
    @Transactional(readOnly = true)
    public MyWrittenPostListResponse read(Long userId, int page, int size, String type) {
        List<MyWrittenPostResponse> posts = switch (type == null ? "" : type) {
            case INFO_POSTS -> readInfoPosts(userId);
            case ARCHIVES -> readArchiveRecords(userId);
            case NOTICES -> readNotices(userId);
            case "" -> readAllPosts(userId);
            default -> List.of();
        };

        List<MyWrittenPostResponse> pagedPosts = page(posts, page, size);
        return new MyWrittenPostListResponse(pagedPosts, pageInfo(page, size, posts.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public MyWrittenPostTargetResponse readTarget(Long userId, Long postId, String type) {
        return switch (requireType(type)) {
            case INFO_POSTS -> {
                InfoPost post = findInfoPost(userId, postId);
                yield new MyWrittenPostTargetResponse(INFO_POSTS, post.getId());
            }
            case ARCHIVES -> {
                ArchiveRecord record = findArchiveRecord(userId, postId);
                yield new MyWrittenPostTargetResponse(ARCHIVES, record.getArchive().getId());
            }
            default -> throw notFound();
        };
    }

    @Override
    @Transactional
    public MyWrittenPostDeleteResponse delete(Long userId, Long postId, String type) {
        User user = findUser(userId);

        switch (requireType(type)) {
            case INFO_POSTS -> {
                findInfoPost(userId, postId);
                infoPostService.deletePost(postId, user.getLoginId());
            }
            case ARCHIVES -> {
                ArchiveRecord record = findArchiveRecord(userId, postId);
                archiveService.deleteRecord(record.getArchive().getId(), record.getId(), user.getLoginId());
            }
            default -> throw notFound();
        }

        return new MyWrittenPostDeleteResponse("작성한 글이 삭제되었습니다.");
    }

    private List<MyWrittenPostResponse> readAllPosts(Long userId) {
        return sortByCreatedAtDesc(concat(concat(readInfoPosts(userId), readArchiveRecords(userId)), readNotices(userId)));
    }

    private List<MyWrittenPostResponse> readInfoPosts(Long userId) {
        return infoPostRepository.findByAuthorId(userId)
                .stream()
                .map(post -> new MyWrittenPostResponse(
                        post.getId(),
                        post.getTitle(),
                        INFO_POSTS,
                        post.getCreatedAt()
                ))
                .toList();
    }

    private List<MyWrittenPostResponse> readArchiveRecords(Long userId) {
        return archiveRecordRepository.findByAuthorId(userId)
                .stream()
                .map(record -> new MyWrittenPostResponse(
                        record.getId(),
                        archiveTitle(record),
                        ARCHIVES,
                        record.getCreatedAt()
                ))
                .toList();
    }

    private List<MyWrittenPostResponse> readNotices(Long userId) {
        return noticeRepository.findByAuthorId(userId)
                .stream()
                .map(notice -> new MyWrittenPostResponse(
                        notice.getNoticeId(),
                        notice.getTitle(),
                        NOTICES,
                        notice.getCreatedAt()
                ))
                .toList();
    }

    private InfoPost findInfoPost(Long userId, Long postId) {
        InfoPost post = infoPostRepository.findById(postId)
                .orElseThrow(this::notFound);
        if (!post.isAuthor(userId)) {
            throw notFound();
        }
        return post;
    }

    private ArchiveRecord findArchiveRecord(Long userId, Long recordId) {
        ArchiveRecord record = archiveRecordRepository.findById(recordId)
                .orElseThrow(this::notFound);
        if (!record.getAuthor().getId().equals(userId)) {
            throw notFound();
        }
        return record;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MyPageApiException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
    }

    private String archiveTitle(ArchiveRecord record) {
        return record.getArchive().getSubjectName() + " / " + record.getArchive().getProfessorName();
    }

    private String requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new MyPageApiException(HttpStatus.BAD_REQUEST, "라우팅 타입이 필요합니다.");
        }
        return type;
    }

    private MyPageApiException notFound() {
        return new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.");
    }

    private List<MyWrittenPostResponse> concat(
            List<MyWrittenPostResponse> left,
            List<MyWrittenPostResponse> right
    ) {
        return java.util.stream.Stream.concat(left.stream(), right.stream()).toList();
    }

    private List<MyWrittenPostResponse> sortByCreatedAtDesc(List<MyWrittenPostResponse> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(MyWrittenPostResponse::createdAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();
    }

    private List<MyWrittenPostResponse> page(List<MyWrittenPostResponse> posts, int page, int size) {
        if (page < 0 || size <= 0) {
            return List.of();
        }
        int fromIndex = Math.min(page * size, posts.size());
        int toIndex = Math.min(fromIndex + size, posts.size());
        return posts.subList(fromIndex, toIndex);
    }

    private PageInfoResponse pageInfo(int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageInfoResponse(page, size, totalPages, totalElements);
    }
}
