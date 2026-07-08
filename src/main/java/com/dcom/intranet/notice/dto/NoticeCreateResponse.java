package com.dcom.intranet.notice.dto;

import com.dcom.intranet.notice.domain.Notice;

import java.time.LocalDateTime;

public record NoticeCreateResponse(
        Long noticeId,
        String title,
        LocalDateTime createdAt
) {

    public static NoticeCreateResponse from(Notice notice) {
        return new NoticeCreateResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getCreatedAt()
        );
    }
}
