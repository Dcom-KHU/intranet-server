package com.dcom.intranet.photo.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.photo.domain.PhotoPost;
import com.dcom.intranet.photo.domain.PhotoPostImage;
import com.dcom.intranet.photo.dto.PhotoPostCreateRequest;
import com.dcom.intranet.photo.dto.PhotoPostCreateResponse;
import com.dcom.intranet.photo.dto.PhotoPostUpdateRequest;
import com.dcom.intranet.photo.repository.PhotoCommentRepository;
import com.dcom.intranet.photo.repository.PhotoPostImageRepository;
import com.dcom.intranet.photo.repository.PhotoPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoPostServiceTest {

    @Mock
    private PhotoPostRepository photoPostRepository;

    @Mock
    private PhotoPostImageRepository photoPostImageRepository;

    @Mock
    private PhotoCommentRepository photoCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotoPostFileStorageService photoPostFileStorageService;

    @Test
    @DisplayName("사진첩 수정 시 새 사진은 기존 사진 뒤에 추가하고 기존 파일은 삭제하지 않는다")
    void updatePhotoPostAppendsNewImagesWithoutDeletingExistingImages() {
        PhotoPostService photoPostService = photoPostService();

        PhotoPostImage existingImage = new PhotoPostImage(
                "old.jpg",
                "old.jpg",
                "2026/08/old.jpg",
                "/uploads/photo/2026/08/old.jpg",
                10L,
                "image/jpeg"
        );
        ReflectionTestUtils.setField(existingImage, "id", 1L);

        PhotoPost photoPost = new PhotoPost(
                null,
                "기존 행사",
                LocalDate.of(2026, 8, 1),
                "기존 설명",
                "기존 장소",
                List.of(existingImage)
        );
        ReflectionTestUtils.setField(photoPost, "albumId", 14L);

        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        when(photoPostRepository.findById(14L)).thenReturn(Optional.of(photoPost));
        when(photoPostFileStorageService.store(newFile)).thenReturn(new PhotoPostFileStorageService.StoredFile(
                "new.jpg",
                "new-stored.jpg",
                "2026/08/new-stored.jpg",
                "/uploads/photo/2026/08/new-stored.jpg",
                20L,
                "image/jpeg"
        ));
        doAnswer(invocation -> {
            if (photoPost.getImages().size() > 1) {
                ReflectionTestUtils.setField(photoPost.getImages().get(1), "id", 2L);
            }
            return null;
        }).when(photoPostRepository).flush();

        PhotoPostCreateResponse response = photoPostService.updatePhotoPost(
                14L,
                new PhotoPostUpdateRequest(
                        "수정 행사",
                        LocalDate.of(2026, 8, 2),
                        "수정 설명",
                        "수정 장소",
                        List.of()
                ),
                List.of(newFile)
        );

        assertThat(response.eventName()).isEqualTo("수정 행사");
        assertThat(response.activityDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(response.place()).isEqualTo("수정 장소");
        assertThat(response.coverImageUrl()).isEqualTo("/api/photo-posts/14/images/1");
        assertThat(response.imageUrls()).containsExactly(
                "/api/photo-posts/14/images/1",
                "/api/photo-posts/14/images/2"
        );

        verify(photoPostRepository, times(2)).flush();
        verify(photoPostFileStorageService, never()).delete("/uploads/photo/2026/08/old.jpg");
    }

    @Test
    @DisplayName("사진첩 등록 중 DB 반영에 실패하면 먼저 저장한 사진 파일을 정리한다")
    void createPhotoPostDeletesStoredImagesWhenDbFlushFails() {
        PhotoPostService photoPostService = photoPostService();
        User author = user();
        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(author));
        when(photoPostFileStorageService.store(newFile)).thenReturn(new PhotoPostFileStorageService.StoredFile(
                "new.jpg",
                "new-stored.jpg",
                "2026/08/new-stored.jpg",
                "/uploads/photo/2026/08/new-stored.jpg",
                20L,
                "image/jpeg"
        ));
        when(photoPostRepository.save(any(PhotoPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("db failure")).when(photoPostRepository).flush();

        assertThatThrownBy(() -> photoPostService.createPhotoPost(
                new PhotoPostCreateRequest(
                        "신입생 환영회",
                        LocalDate.of(2026, 8, 2),
                        "설명",
                        "장소"
                ),
                List.of(newFile),
                "admin"
        )).isInstanceOf(RuntimeException.class)
                .hasMessage("db failure");

        verify(photoPostFileStorageService).delete("/uploads/photo/2026/08/new-stored.jpg");
    }

    @Test
    @DisplayName("사진첩 등록 시 이미지가 아닌 파일은 저장하지 않고 거절한다")
    void createPhotoPostRejectsNonImageFileBeforeStorage() {
        PhotoPostService photoPostService = photoPostService();
        User author = user();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "files",
                "document.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> photoPostService.createPhotoPost(
                new PhotoPostCreateRequest(
                        "신입생 환영회",
                        LocalDate.of(2026, 8, 2),
                        "설명",
                        "장소"
                ),
                List.of(pdfFile),
                "admin"
        )).isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(photoPostFileStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    @DisplayName("사진첩 수정 시 앨범 최대 사진 개수를 초과하면 파일 저장 전에 거절한다")
    void updatePhotoPostRejectsImagesOverAlbumLimitBeforeStorage() {
        PhotoPostService photoPostService = photoPostService();
        PhotoPost photoPost = new PhotoPost(
                null,
                "기존 행사",
                LocalDate.of(2026, 8, 1),
                "기존 설명",
                "기존 장소",
                images(10)
        );
        ReflectionTestUtils.setField(photoPost, "albumId", 14L);
        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        when(photoPostRepository.findById(14L)).thenReturn(Optional.of(photoPost));

        assertThatThrownBy(() -> photoPostService.updatePhotoPost(
                14L,
                new PhotoPostUpdateRequest(
                        "수정 행사",
                        LocalDate.of(2026, 8, 2),
                        "수정 설명",
                        "수정 장소",
                        List.of()
                ),
                List.of(newFile)
        )).isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(photoPostFileStorageService, never()).store(any(MultipartFile.class));
    }

    @Test
    @DisplayName("사진첩 수정 시 deleteFileIds에 포함된 기존 사진을 삭제하고 남은 사진 순서를 재정렬한다")
    void updatePhotoPostDeletesExistingImagesByDeleteFileIds() {
        PhotoPostService photoPostService = photoPostService();
        PhotoPostImage firstImage = image(1L, "first.jpg");
        PhotoPostImage secondImage = image(2L, "second.jpg");
        PhotoPostImage thirdImage = image(3L, "third.jpg");
        PhotoPost photoPost = new PhotoPost(
                null,
                "기존 행사",
                LocalDate.of(2026, 8, 1),
                "기존 설명",
                "기존 장소",
                List.of(firstImage, secondImage, thirdImage)
        );
        ReflectionTestUtils.setField(photoPost, "albumId", 14L);

        PhotoPost reloadedPhotoPost = new PhotoPost(
                null,
                "수정 행사",
                LocalDate.of(2026, 8, 2),
                "수정 설명",
                "수정 장소",
                List.of(firstImage, thirdImage)
        );
        ReflectionTestUtils.setField(reloadedPhotoPost, "albumId", 14L);

        when(photoPostRepository.findById(14L))
                .thenReturn(Optional.of(photoPost))
                .thenReturn(Optional.of(reloadedPhotoPost));
        when(photoPostImageRepository.deleteByAlbumIdAndImageIds(14L, List.of(2L)))
                .thenReturn(1);

        PhotoPostCreateResponse response = photoPostService.updatePhotoPost(
                14L,
                new PhotoPostUpdateRequest(
                        "수정 행사",
                        LocalDate.of(2026, 8, 2),
                        "수정 설명",
                        "수정 장소",
                        List.of(2L)
                ),
                List.of()
        );

        assertThat(response.imageUrls()).containsExactly(
                "/api/photo-posts/14/images/1",
                "/api/photo-posts/14/images/3"
        );

        verify(photoPostImageRepository).deleteByAlbumIdAndImageIds(14L, List.of(2L));
        verify(photoPostImageRepository).shiftUploadOrderForReorder(14L);
        verify(photoPostImageRepository).reorderUploadOrder(14L);
        verify(photoPostFileStorageService).delete("/uploads/photo/2026/08/second.jpg");
    }

    @Test
    @DisplayName("사진첩 수정 시 모든 기존 사진을 삭제하고 새 사진이 없으면 거절한다")
    void updatePhotoPostRejectsDeletingEveryImageWithoutNewImages() {
        PhotoPostService photoPostService = photoPostService();
        PhotoPostImage existingImage = image(1L, "old.jpg");
        PhotoPost photoPost = new PhotoPost(
                null,
                "기존 행사",
                LocalDate.of(2026, 8, 1),
                "기존 설명",
                "기존 장소",
                List.of(existingImage)
        );
        ReflectionTestUtils.setField(photoPost, "albumId", 14L);

        when(photoPostRepository.findById(14L)).thenReturn(Optional.of(photoPost));

        assertThatThrownBy(() -> photoPostService.updatePhotoPost(
                14L,
                new PhotoPostUpdateRequest(
                        "수정 행사",
                        LocalDate.of(2026, 8, 2),
                        "수정 설명",
                        "수정 장소",
                        List.of(1L)
                ),
                List.of()
        )).isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(photoPostImageRepository, never()).deleteByAlbumIdAndImageIds(any(), any());
        verify(photoPostFileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("사진첩 등록 시 파일당 10MB를 초과하면 파일 저장 전에 거절한다")
    void createPhotoPostRejectsImageOverSizeLimitBeforeStorage() {
        PhotoPostService photoPostService = photoPostService();
        User author = user();
        MultipartFile largeFile = mock(MultipartFile.class);

        when(userRepository.findByLoginId("admin")).thenReturn(Optional.of(author));
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(10L * 1024 * 1024 + 1);

        assertThatThrownBy(() -> photoPostService.createPhotoPost(
                new PhotoPostCreateRequest(
                        "신입생 환영회",
                        LocalDate.of(2026, 8, 2),
                        "설명",
                        "장소"
                ),
                List.of(largeFile),
                "admin"
        )).isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);

        verify(photoPostFileStorageService, never()).store(any(MultipartFile.class));
    }

    private PhotoPostService photoPostService() {
        return new PhotoPostService(
                photoPostRepository,
                photoPostImageRepository,
                photoCommentRepository,
                userRepository,
                photoPostFileStorageService
        );
    }

    private User user() {
        return new User(
                "admin",
                "password",
                "관리자",
                "20201234",
                "admin@example.com",
                "01012345678"
        );
    }

    private List<PhotoPostImage> images(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new PhotoPostImage(
                        "image-%d.jpg".formatted(index),
                        "image-%d.jpg".formatted(index),
                        "2026/08/image-%d.jpg".formatted(index),
                        "/uploads/photo/2026/08/image-%d.jpg".formatted(index),
                        10L,
                        "image/jpeg"
                ))
                .toList();
    }

    private PhotoPostImage image(Long id, String fileName) {
        PhotoPostImage image = new PhotoPostImage(
                fileName,
                fileName,
                "2026/08/%s".formatted(fileName),
                "/uploads/photo/2026/08/%s".formatted(fileName),
                10L,
                "image/jpeg"
        );
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
}
