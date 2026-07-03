package com.dcom.intranet.mypage.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.domain.InfoComment;
import com.dcom.intranet.info.repository.InfoCommentRepository;
import com.dcom.intranet.info.service.InfoCommentService;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentTargetResponse;
import com.dcom.intranet.mypage.dto.response.PageInfoResponse;
import com.dcom.intranet.mypage.exception.MyPageApiException;
import com.dcom.intranet.photo.domain.PhotoComment;
import com.dcom.intranet.photo.repository.PhotoCommentRepository;
import com.dcom.intranet.photo.service.PhotoPostService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CompositeMyWrittenCommentReader implements MyWrittenCommentReader {

    private static final String INFO_POSTS = "info-posts";
    private static final String PHOTO_POSTS = "photo-posts";

    private final InfoCommentRepository infoCommentRepository;
    private final PhotoCommentRepository photoCommentRepository;
    private final UserRepository userRepository;
    private final InfoCommentService infoCommentService;
    private final PhotoPostService photoPostService;

    public CompositeMyWrittenCommentReader(
            InfoCommentRepository infoCommentRepository,
            PhotoCommentRepository photoCommentRepository,
            UserRepository userRepository,
            InfoCommentService infoCommentService,
            PhotoPostService photoPostService
    ) {
        this.infoCommentRepository = infoCommentRepository;
        this.photoCommentRepository = photoCommentRepository;
        this.userRepository = userRepository;
        this.infoCommentService = infoCommentService;
        this.photoPostService = photoPostService;
    }

    @Override
    @Transactional(readOnly = true)
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        List<MyWrittenCommentResponse> comments = switch (type == null ? "" : type) {
            case INFO_POSTS -> readInfoComments(userId);
            case PHOTO_POSTS -> readPhotoComments(userId);
            case "" -> readAllComments(userId);
            default -> List.of();
        };

        List<MyWrittenCommentResponse> pagedComments = page(comments, page, size);
        return new MyWrittenCommentListResponse(pagedComments, pageInfo(page, size, comments.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type) {
        return switch (requireType(type)) {
            case INFO_POSTS -> {
                InfoComment comment = findInfoComment(userId, commentId);
                yield new MyWrittenCommentTargetResponse(INFO_POSTS, comment.getPost().getId(), comment.getId());
            }
            case PHOTO_POSTS -> {
                PhotoComment comment = findPhotoComment(userId, commentId);
                yield new MyWrittenCommentTargetResponse(PHOTO_POSTS, comment.getPhotoPost().getAlbumId(), comment.getCommentId());
            }
            default -> throw notFound();
        };
    }

    @Override
    @Transactional
    public MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type) {
        User user = findUser(userId);

        switch (requireType(type)) {
            case INFO_POSTS -> {
                InfoComment comment = findInfoComment(userId, commentId);
                infoCommentService.deleteComment(comment.getPost().getId(), comment.getId(), user.getLoginId());
            }
            case PHOTO_POSTS -> {
                PhotoComment comment = findPhotoComment(userId, commentId);
                photoPostService.deleteComment(comment.getPhotoPost().getAlbumId(), comment.getCommentId(), user.getLoginId());
            }
            default -> throw notFound();
        }

        return new MyWrittenCommentDeleteResponse("작성한 댓글이 삭제되었습니다.");
    }

    private List<MyWrittenCommentResponse> readAllComments(Long userId) {
        return sortByCreatedAtDesc(concat(readInfoComments(userId), readPhotoComments(userId)));
    }

    private List<MyWrittenCommentResponse> readInfoComments(Long userId) {
        return infoCommentRepository.findByAuthorId(userId)
                .stream()
                .map(comment -> new MyWrittenCommentResponse(
                        comment.getId(),
                        INFO_POSTS,
                        comment.getPost().getId(),
                        comment.getPost().getTitle(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    private List<MyWrittenCommentResponse> readPhotoComments(Long userId) {
        return photoCommentRepository.findByAuthorId(userId)
                .stream()
                .map(comment -> new MyWrittenCommentResponse(
                        comment.getCommentId(),
                        PHOTO_POSTS,
                        comment.getPhotoPost().getAlbumId(),
                        comment.getPhotoPost().getEventName(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    private InfoComment findInfoComment(Long userId, Long commentId) {
        InfoComment comment = infoCommentRepository.findById(commentId)
                .orElseThrow(this::notFound);
        if (!comment.isAuthor(userId)) {
            throw notFound();
        }
        return comment;
    }

    private PhotoComment findPhotoComment(Long userId, Long commentId) {
        PhotoComment comment = photoCommentRepository.findById(commentId)
                .orElseThrow(this::notFound);
        if (!comment.isAuthor(userId)) {
            throw notFound();
        }
        return comment;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MyPageApiException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
    }

    private String requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new MyPageApiException(HttpStatus.BAD_REQUEST, "라우팅 타입이 필요합니다.");
        }
        return type;
    }

    private MyPageApiException notFound() {
        return new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
    }

    private List<MyWrittenCommentResponse> concat(
            List<MyWrittenCommentResponse> left,
            List<MyWrittenCommentResponse> right
    ) {
        return java.util.stream.Stream.concat(left.stream(), right.stream()).toList();
    }

    private List<MyWrittenCommentResponse> sortByCreatedAtDesc(List<MyWrittenCommentResponse> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(MyWrittenCommentResponse::createdAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();
    }

    private List<MyWrittenCommentResponse> page(List<MyWrittenCommentResponse> comments, int page, int size) {
        if (page < 0 || size <= 0) {
            return List.of();
        }
        int fromIndex = Math.min(page * size, comments.size());
        int toIndex = Math.min(fromIndex + size, comments.size());
        return comments.subList(fromIndex, toIndex);
    }

    private PageInfoResponse pageInfo(int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageInfoResponse(page, size, totalPages, totalElements);
    }
}
