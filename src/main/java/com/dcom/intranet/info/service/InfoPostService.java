package com.dcom.intranet.info.service;

import com.dcom.intranet.info.domain.InfoPost;
import com.dcom.intranet.info.domain.InfoPostFile;
import com.dcom.intranet.info.dto.request.InfoPostCreateRequest;
import com.dcom.intranet.info.dto.request.InfoPostUpdateRequest;
import com.dcom.intranet.info.dto.response.*;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfoPostService {

    private final InfoPostRepository infoPostRepository;
    private final UserRepository userRepository;
    private final InfoPostFileStorageService infoPostFileStorageService;

    public InfoPostPageResponse getPosts(
            int page,
            int size,
            String keyword,
            String sort
    ) {
        Pageable pageable = createPageable(page, size, sort);

        Page<InfoPost> posts;

        if (keyword != null && !keyword.isBlank()) {
            posts = infoPostRepository.findByTitleContainingOrContentContaining(
                    keyword,
                    keyword,
                    pageable
            );
        } else {
            posts = infoPostRepository.findAll(pageable);
        }

        Page<InfoPostListResponse> responsePage =
                posts.map(InfoPostListResponse::new);

        return new InfoPostPageResponse(responsePage);
    }

    @Transactional
    public InfoPostDetailResponse getPostDetail(Long postId) {
        InfoPost post = findPost(postId);

        post.increaseViews();

        return new InfoPostDetailResponse(post);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortOption = switch (sort == null ? "latest" : sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "views" -> Sort.by(Sort.Direction.DESC, "views");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        return PageRequest.of(page, size, sortOption);
    }

    @Transactional
    public InfoPostCreateResponse createPost(
            InfoPostCreateRequest request,
            List<MultipartFile> files,
            Long userId
    ) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        InfoPost post = new InfoPost(
                author,
                request.getTitle(),
                request.getContent()
        );

        List<MultipartFile> safeFiles = files == null
                ? List.of()
                : files;

        for (MultipartFile multipartFile : safeFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }

            InfoPostFileStorageService.StoredFile storedFile =
                    infoPostFileStorageService.store(multipartFile);

            InfoPostFile infoPostFile = new InfoPostFile(
                    storedFile.getOriginalFileName(),
                    storedFile.getStoredFileName(),
                    storedFile.getObjectKey(),
                    storedFile.getFileUrl(),
                    storedFile.getFileSize(),
                    storedFile.getContentType()
            );

            post.addFile(infoPostFile);
        }

        InfoPost savedPost = infoPostRepository.saveAndFlush(post);

        return new InfoPostCreateResponse(savedPost);
    }

    @Transactional
    public InfoPostUpdateResponse updatePost(
            Long postId,
            InfoPostUpdateRequest request,
            List<MultipartFile> files,
            Long userId
    ) {
        InfoPost post = findPost(postId);

        validateAuthor(post, userId);

        List<Long> deleteFileIds = request.getDeleteFileIds() == null
                ? List.of()
                : request.getDeleteFileIds()
                .stream()
                .distinct()
                .toList();

        List<MultipartFile> safeFiles = files == null
                ? List.of()
                : files;

        // 1. 제목, 본문 수정
        post.update(
                request.getTitle(),
                request.getContent()
        );

        // 2. 기존 파일 선택 삭제
        for (Long deleteFileId : deleteFileIds) {
            InfoPostFile targetFile = findFileInPost(post, deleteFileId);

            infoPostFileStorageService.delete(targetFile.getFileUrl());
            post.removeFile(targetFile);
        }

        // 3. 새 파일 추가
        for (MultipartFile multipartFile : safeFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }

            InfoPostFileStorageService.StoredFile storedFile =
                    infoPostFileStorageService.store(multipartFile);

            InfoPostFile infoPostFile = new InfoPostFile(
                    storedFile.getOriginalFileName(),
                    storedFile.getStoredFileName(),
                    storedFile.getObjectKey(),
                    storedFile.getFileUrl(),
                    storedFile.getFileSize(),
                    storedFile.getContentType()
            );

            post.addFile(infoPostFile);
        }

        infoPostRepository.flush();

        return new InfoPostUpdateResponse(post);
    }

    private InfoPost findPost(Long postId) {
        return infoPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "정보 공유 게시글을 찾을 수 없습니다."
                ));
    }

    private InfoPostFile findFileInPost(InfoPost post, Long fileId) {
        return post.getFiles()
                .stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "첨부파일을 찾을 수 없습니다."
                ));
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        InfoPost post = findPost(postId);

        validateAuthorOrAdmin(post, userId);

        List<InfoPostFile> filesToDelete = new ArrayList<>(post.getFiles());

        for (InfoPostFile file : filesToDelete) {
            infoPostFileStorageService.delete(file.getFileUrl());
        }

        infoPostRepository.delete(post);
    }

    // 작성자 검증
    private void validateAuthor(InfoPost post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        if (!post.isAuthor(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작성자만 수정할 수 있습니다."
            );
        }
    }

    // 작성자 또는 관리자 확인
    private void validateAuthorOrAdmin(InfoPost post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        boolean isAuthor = post.isAuthor(user.getId());
        boolean isAdmin = user.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작성자 또는 관리자만 삭제할 수 있습니다."
            );
        }
    }
}