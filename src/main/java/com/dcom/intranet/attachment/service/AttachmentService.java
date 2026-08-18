package com.dcom.intranet.attachment.service;

import com.dcom.intranet.info.domain.InfoPostFile;
import com.dcom.intranet.info.repository.InfoPostFileRepository;
import com.dcom.intranet.info.service.InfoPostFileStorageService;
import com.dcom.intranet.notice.domain.NoticeFile;
import com.dcom.intranet.notice.repository.NoticeFileRepository;
import com.dcom.intranet.notice.service.NoticeFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final InfoPostFileRepository infoPostFileRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final InfoPostFileStorageService infoPostFileStorageService;
    private final NoticeFileStorageService noticeFileStorageService;

    public DownloadFile download(String type, Long fileId) {
        return switch (type) {
            case "info-posts" -> downloadInfoFile(fileId);
            case "notice" -> downloadNoticeFile(fileId);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일 유형을 찾을 수 없습니다.");
        };
    }

    private DownloadFile downloadInfoFile(Long fileId) {
        InfoPostFile file = infoPostFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
        return new DownloadFile(
                infoPostFileStorageService.loadAsResource(file.getFileUrl()),
                file.getOriginalFileName(),
                file.getContentType()
        );
    }

    private DownloadFile downloadNoticeFile(Long fileId) {
        NoticeFile file = noticeFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
        return new DownloadFile(
                noticeFileStorageService.loadAsResource(file.getFileUrl()),
                file.getOriginalFileName(),
                file.getContentType()
        );
    }

    public record DownloadFile(Resource resource, String fileName, String contentType) {
    }
}
