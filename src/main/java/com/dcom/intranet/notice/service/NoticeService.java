package com.dcom.intranet.notice.service;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.notice.domain.Notice;
import com.dcom.intranet.notice.domain.NoticeFile;
import com.dcom.intranet.notice.dto.NoticeCreateRequest;
import com.dcom.intranet.notice.dto.NoticeCreateResponse;
import com.dcom.intranet.notice.dto.NoticeDeleteResponse;
import com.dcom.intranet.notice.dto.NoticeDetailResponse;
import com.dcom.intranet.notice.dto.NoticeListResponse;
import com.dcom.intranet.notice.dto.NoticeUpdateRequest;
import com.dcom.intranet.notice.dto.NoticeUpdateResponse;
import com.dcom.intranet.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NoticeFileStorageService noticeFileStorageService;

    @Transactional(readOnly = true)
    public NoticeListResponse getNoticeList(String title, Pageable pageable) {
        Page<Notice> notices = title == null || title.isBlank()
                ? noticeRepository.findAll(pageable)
                : noticeRepository.findByTitleContaining(title, pageable);

        Page<NoticeListResponse.NoticeSummary> page = notices
                .map(notice -> new NoticeListResponse.NoticeSummary(
                        notice.getNoticeId(),
                        notice.getTitle(),
                        notice.getAuthorId(),
                        notice.getCreatedAt()
                ));

        return NoticeListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        Notice notice = findNotice(noticeId);

        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public NoticeCreateResponse createNotice(
            NoticeCreateRequest request,
            List<MultipartFile> files,
            String loginId
    ) {
        User author = findUser(loginId);

        Notice notice = new Notice(
                request.title(),
                request.content(),
                author.getId(),
                LocalDateTime.now(),
                toNoticeFiles(files)
        );

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeCreateResponse.from(savedNotice);
    }

    @Transactional
    public NoticeUpdateResponse updateNotice(
            Long noticeId,
            NoticeUpdateRequest request,
            List<MultipartFile> files
    ) {
        Notice notice = findNotice(noticeId);

        notice.update(
                request.title(),
                request.content(),
                LocalDateTime.now()
        );

        deleteFiles(notice, request.deleteFileIds());
        notice.addFiles(toNoticeFiles(files));

        return NoticeUpdateResponse.from(notice);
    }

    @Transactional
    public NoticeDeleteResponse deleteNotice(Long noticeId) {
        Notice notice = findNotice(noticeId);

        List<NoticeFile> filesToDelete = new ArrayList<>(notice.getFiles());

        noticeRepository.delete(notice);
        filesToDelete.forEach(file -> noticeFileStorageService.delete(file.getFileUrl()));

        return new NoticeDeleteResponse("공지사항이 삭제되었습니다.");
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "공지사항을 찾을 수 없습니다."
                ));
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private List<NoticeFile> toNoticeFiles(List<MultipartFile> files) {
        List<NoticeFile> noticeFiles = new ArrayList<>();

        if (files != null) {
            files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(noticeFileStorageService::store)
                    .map(file -> new NoticeFile(
                            file.getOriginalFileName(),
                            file.getStoredFileName(),
                            file.getObjectKey(),
                            file.getFileUrl(),
                            file.getFileSize(),
                            file.getContentType()
                    ))
                    .forEach(noticeFiles::add);
        }

        return noticeFiles;
    }

    private void deleteFiles(Notice notice, List<Long> deleteFileIds) {
        if (deleteFileIds == null || deleteFileIds.isEmpty()) {
            return;
        }

        deleteFileIds.stream()
                .distinct()
                .map(fileId -> findFileInNotice(notice, fileId))
                .forEach(file -> {
                    noticeFileStorageService.delete(file.getFileUrl());
                    notice.removeFile(file);
                });
    }

    private NoticeFile findFileInNotice(Notice notice, Long fileId) {
        return notice.getFiles()
                .stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "첨부파일을 찾을 수 없습니다."
                ));
    }
}
