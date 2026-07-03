package com.dcom.intranet.photo.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.photo.domain.PhotoComment;
import com.dcom.intranet.photo.domain.PhotoPost;
import com.dcom.intranet.photo.dto.PhotoCommentCreateResponse;
import com.dcom.intranet.photo.dto.PhotoCommentDeleteResponse;
import com.dcom.intranet.photo.dto.PhotoCommentUpdateResponse;
import com.dcom.intranet.photo.dto.PhotoPostCreateRequest;
import com.dcom.intranet.photo.dto.PhotoPostCreateResponse;
import com.dcom.intranet.photo.dto.PhotoPostDeleteResponse;
import com.dcom.intranet.photo.dto.PhotoPostDetailResponse;
import com.dcom.intranet.photo.dto.PhotoPostListResponse;
import com.dcom.intranet.photo.dto.PhotoPostUpdateRequest;
import com.dcom.intranet.photo.repository.PhotoCommentRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoPostService {

    private final PhotoPostRepository photoPostRepository;
    private final PhotoCommentRepository photoCommentRepository;
    private final UserRepository userRepository;
    private final PhotoPostFileStorageService photoPostFileStorageService;

    @Transactional(readOnly = true)
    public PhotoPostListResponse getPhotoPostList(Pageable pageable) {
        Page<PhotoPostListResponse.AlbumSummary> page = photoPostRepository.findAll(pageable)
                .map(photoPost -> new PhotoPostListResponse.AlbumSummary(
                        photoPost.getAlbumId(),
                        photoPost.getCoverImageUrl(),
                        photoPost.getEventName(),
                        photoPost.getActivityDate()
                ));

        return PhotoPostListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PhotoPostDetailResponse getPhotoPostDetail(Long albumId) {
        PhotoPost photoPost = photoPostRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩을 찾을 수 없습니다."
                ));

        return PhotoPostDetailResponse.from(photoPost);
    }

    @Transactional
    public PhotoPostCreateResponse createPhotoPost(
            PhotoPostCreateRequest request,
            List<MultipartFile> files
    ) {
        List<String> imageUrls = storeImages(files);

        PhotoPost photoPost = new PhotoPost(
                request.eventName(),
                request.activityDate(),
                request.description(),
                imageUrls
        );

        PhotoPost savedPhotoPost = photoPostRepository.save(photoPost);
        return PhotoPostCreateResponse.from(savedPhotoPost);
    }

    @Transactional
    public PhotoPostCreateResponse updatePhotoPost(
            Long albumId,
            PhotoPostUpdateRequest request,
            List<MultipartFile> files
    ) {
        PhotoPost photoPost = photoPostRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩을 찾을 수 없습니다."
                ));

        photoPost.update(
                request.eventName(),
                request.activityDate(),
                request.description()
        );

        if (hasFiles(files)) {
            List<String> oldImageUrls = new ArrayList<>(photoPost.getImageUrls());
            List<String> newImageUrls = storeImages(files);

            photoPost.replaceImages(newImageUrls);
            oldImageUrls.forEach(photoPostFileStorageService::delete);
        }

        return PhotoPostCreateResponse.from(photoPost);
    }

    @Transactional
    public PhotoPostDeleteResponse deletePhotoPost(Long albumId) {
        PhotoPost photoPost = photoPostRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩을 찾을 수 없습니다."
                ));

        List<String> imagePaths = new ArrayList<>(photoPost.getImageUrls());
        photoPostRepository.delete(photoPost);
        imagePaths.forEach(path -> {
            try {
                photoPostFileStorageService.delete(path);
            } catch (Exception e) {
                // 파일 삭제 실패해도 DB는 이미 삭제됨
                System.err.println("파일 삭제 실패: " + path);
            }
        });

        return new PhotoPostDeleteResponse("사진첩이 삭제되었습니다.");
    }

    @Transactional
    public PhotoCommentCreateResponse createComment(Long albumId, String content, String loginId) {
        PhotoPost photoPost = photoPostRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩을 찾을 수 없습니다."
                ));

        User author = findUser(loginId);

        PhotoComment comment = new PhotoComment(photoPost, author, content);
        PhotoComment savedComment = photoCommentRepository.save(comment);

        return PhotoCommentCreateResponse.from(savedComment);
    }

    @Transactional
    public PhotoCommentUpdateResponse updateComment(
            Long albumId,
            Long commentId,
            String content,
            String loginId
    ) {
        PhotoComment comment = photoCommentRepository.findByCommentIdAndPhotoPostAlbumId(commentId, albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩 댓글을 찾을 수 없습니다."
                ));

        validateAuthor(comment, loginId);

        comment.update(content);
        photoCommentRepository.flush();

        return PhotoCommentUpdateResponse.from(comment);
    }

    @Transactional
    public PhotoCommentDeleteResponse deleteComment(
            Long albumId,
            Long commentId,
            String loginId
    ) {
        PhotoComment comment = photoCommentRepository.findByCommentIdAndPhotoPostAlbumId(commentId, albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사진첩 댓글을 찾을 수 없습니다."
                ));

        validateAuthorOrAdmin(comment, loginId);

        photoCommentRepository.delete(comment);

        return new PhotoCommentDeleteResponse("댓글이 삭제되었습니다.");
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private void validateAuthor(PhotoComment comment, String loginId) {
        User user = findUser(loginId);

        if (!comment.isAuthor(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "댓글 작성자만 수정할 수 있습니다."
            );
        }
    }

    private void validateAuthorOrAdmin(PhotoComment comment, String loginId) {
        User user = findUser(loginId);

        boolean isAuthor = comment.isAuthor(user.getId());
        boolean isAdmin = user.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "댓글 작성자 또는 관리자만 삭제할 수 있습니다."
            );
        }
    }

    private List<String> storeImages(List<MultipartFile> files) {
        if (!hasFiles(files)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사진은 최소 1개 이상 필요합니다."
            );
        }

        List<String> imageUrls = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(photoPostFileStorageService::store)
                .map(PhotoPostFileStorageService.StoredFile::getFileUrl)
                .toList();

        return new ArrayList<>(imageUrls);
    }

    private boolean hasFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }

        return files.stream()
                .anyMatch(file -> file != null && !file.isEmpty());
    }
}
