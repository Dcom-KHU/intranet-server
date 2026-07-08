package com.dcom.intranet.notice.dto;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.notice.domain.Notice;

import java.time.LocalDateTime;
import java.util.List;

public record NoticeDetailResponse(
        Long noticeId,
        String title,
        String content,
        AuthorResponse author,
        LocalDateTime createdAt,
        List<FileInfo> files
) {

    public static NoticeDetailResponse from(Notice notice, AuthorResponse author) {
        return new NoticeDetailResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                author,
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
