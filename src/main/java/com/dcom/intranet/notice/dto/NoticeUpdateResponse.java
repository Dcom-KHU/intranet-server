package com.dcom.intranet.notice.dto;

import com.dcom.intranet.notice.domain.Notice;

import java.time.LocalDateTime;

public record NoticeUpdateResponse(
        Long noticeId,
        String title,
        LocalDateTime updatedAt
) {

    public static NoticeUpdateResponse from(Notice notice) {
        return new NoticeUpdateResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getUpdatedAt()
        );
    }
}
