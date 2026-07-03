package com.dcom.intranet.notice.dto;

import com.dcom.intranet.notice.domain.Notice;

import java.time.LocalDateTime;
import java.util.List;

public record NoticeDetailResponse(
        Long noticeId,
        String title,
        String content,
        Long authorId,
        LocalDateTime createdAt,
        List<FileInfo> files
) {

    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthorId(),
                notice.getCreatedAt(),
                notice.getFiles().stream()
                        .map(file -> new FileInfo(file.getId(), file.getOriginalFileName(), file.getFileUrl()))
                        .toList()
        );
    }

    public record FileInfo(
            Long fileId,
            String originalFileName,
            String fileUrl
    ) {
    }
}
