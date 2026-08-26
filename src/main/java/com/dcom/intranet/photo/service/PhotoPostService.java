package com.dcom.intranet.photo.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.photo.domain.PhotoComment;
import com.dcom.intranet.photo.domain.PhotoPost;
import com.dcom.intranet.photo.domain.PhotoPostImage;
import com.dcom.intranet.photo.dto.PhotoCommentCreateResponse;
import com.dcom.intranet.photo.dto.PhotoCommentDeleteResponse;
import com.dcom.intranet.photo.dto.PhotoCommentListResponse;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PhotoPostService {

    private static final int MAX_IMAGE_COUNT_PER_ALBUM = 10;
    private static final long MAX_PHOTO_FILE_SIZE = 10L * 1024 * 1024;
    private static final String SVG_CONTENT_TYPE = "image/svg+xml";
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp",
            ".heic",
            ".heif"
    );

    private final PhotoPostRepository photoPostRepository;
    private final PhotoCommentRepository photoCommentRepository;
    private final UserRepository userRepository;
    private final PhotoPostFileStorageService photoPostFileStorageService;

    @Transactional(readOnly = true)
    public PhotoPostListResponse getPhotoPostList(String keyword, Pageable pageable) {
        Page<PhotoPost> photoPosts = keyword == null || keyword.isBlank()
                ? photoPostRepository.findAll(pageable)
                : photoPostRepository.searchByEventNameIgnoringSpaces(normalizeKeyword(keyword), pageable);

        Page<PhotoPostListResponse.AlbumSummary> page = photoPosts
                .map(photoPost -> new PhotoPostListResponse.AlbumSummary(
                        photoPost.getAlbumId(),
                        photoPost.getImages().isEmpty()
                                ? null
                                : "/api/photo-posts/%d/images/%d".formatted(
                                        photoPost.getAlbumId(),
                                        photoPost.getImages().get(0).getId()
                                ),
                        photoPost.getEventName(),
                        photoPost.getActivityDate(),
                        photoPost.getImages().size()
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

    @Transactional(readOnly = true)
    public DownloadFile downloadImage(Long albumId, Long imageId) {
        PhotoPost photoPost = photoPostRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사진첩을 찾을 수 없습니다."));
        PhotoPostImage image = photoPost.getImages().stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."));

        return new DownloadFile(
                photoPostFileStorageService.loadAsResource(image.getFileUrl()),
                image.getOriginalFileName(),
                image.getContentType()
        );
    }

    @Transactional(readOnly = true)
    public PhotoCommentListResponse getCommentList(Long albumId) {
        if (!photoPostRepository.existsById(albumId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "사진첩을 찾을 수 없습니다."
            );
        }

        List<PhotoComment> comments = photoCommentRepository.findByPhotoPostAlbumIdOrderByCreatedAtAsc(albumId);
        return PhotoCommentListResponse.from(comments);
    }

    @Transactional
    public PhotoPostCreateResponse createPhotoPost(
            PhotoPostCreateRequest request,
            List<MultipartFile> files,
            String loginId
    ) {
        User author = findUser(loginId);
        List<MultipartFile> uploadedFiles = uploadedFiles(files);
        validateImageCount(0, uploadedFiles.size());
        List<PhotoPostImage> images = storeImages(uploadedFiles);

        PhotoPost photoPost = new PhotoPost(
                author,
                request.eventName(),
                request.activityDate(),
                request.description(),
                request.place(),
                images
        );

        try {
            PhotoPost savedPhotoPost = photoPostRepository.save(photoPost);
            photoPostRepository.flush();
            return PhotoPostCreateResponse.from(savedPhotoPost);
        } catch (RuntimeException e) {
            images.stream()
                    .map(PhotoPostImage::getFileUrl)
                    .forEach(this::deleteStoredPhotoQuietly);
            throw e;
        }
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
                request.description(),
                request.place()
        );

        if (hasFiles(files)) {
            List<MultipartFile> uploadedFiles = uploadedFiles(files);
            validateImageCount(photoPost.getImages().size(), uploadedFiles.size());
            List<PhotoPostImage> newImages = storeImages(uploadedFiles);

            try {
                photoPost.addImageFiles(newImages);
                photoPostRepository.flush();
            } catch (RuntimeException e) {
                newImages.stream()
                        .map(PhotoPostImage::getFileUrl)
                        .forEach(this::deleteStoredPhotoQuietly);
                throw e;
            }
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

    private String normalizeKeyword(String keyword) {
        return keyword.replaceAll("\\s+", "");
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

    private List<PhotoPostImage> storeImages(List<MultipartFile> files) {
        List<MultipartFile> uploadedFiles = uploadedFiles(files);

        if (uploadedFiles.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사진은 최소 1개 이상 필요합니다."
            );
        }

        validateImageFiles(uploadedFiles);

        List<PhotoPostImage> images = uploadedFiles.stream()
                .map(photoPostFileStorageService::store)
                .map(file -> new PhotoPostImage(
                        file.getOriginalFileName(),
                        file.getStoredFileName(),
                        file.getObjectKey(),
                        file.getFileUrl(),
                        file.getFileSize(),
                        file.getContentType()
                ))
                .toList();

        return new ArrayList<>(images);
    }

    private List<MultipartFile> uploadedFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private void validateImageCount(int existingImageCount, int newImageCount) {
        if (existingImageCount + newImageCount > MAX_IMAGE_COUNT_PER_ALBUM) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사진은 앨범당 최대 %d개까지 업로드할 수 있습니다.".formatted(MAX_IMAGE_COUNT_PER_ALBUM)
            );
        }
    }

    private void validateImageFiles(List<MultipartFile> files) {
        files.forEach(this::validateImageFile);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.getSize() > MAX_PHOTO_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "사진 파일은 1개당 최대 %dMB까지 업로드할 수 있습니다.".formatted(
                            MAX_PHOTO_FILE_SIZE / 1024 / 1024
                    )
            );
        }

        if (!isAllowedImageFile(file)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사진 파일만 업로드할 수 있습니다."
            );
        }
    }

    private boolean isAllowedImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
            if (normalizedContentType.startsWith("image/")) {
                return !SVG_CONTENT_TYPE.equals(normalizedContentType);
            }

            if (!MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(normalizedContentType)) {
                return false;
            }
        }

        return hasAllowedImageExtension(file);
    }

    private boolean hasAllowedImageExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            return false;
        }

        String normalizedFileName = originalFileName.toLowerCase(Locale.ROOT);
        return ALLOWED_IMAGE_EXTENSIONS.stream()
                .anyMatch(normalizedFileName::endsWith);
    }

    private void deleteStoredPhotoQuietly(String fileUrl) {
        try {
            photoPostFileStorageService.delete(fileUrl);
        } catch (Exception ignored) {
            // DB 반영 실패 후 보상 삭제가 실패해도 원래 예외를 유지한다.
        }
    }

    private boolean hasFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }

        return files.stream()
                .anyMatch(file -> file != null && !file.isEmpty());
    }

    public record DownloadFile(Resource resource, String fileName, String contentType) {
    }
}
