package com.dcom.intranet.notice.dto;

import com.dcom.intranet.global.dto.AuthorResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public record NoticeListResponse(
        List<NoticeSummary> noticeList,
        PageInfo pageInfo
) {

    public static NoticeListResponse from(Page<NoticeSummary> page) {
        return new NoticeListResponse(
                page.getContent(),
                new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast()
                )
        );
    }

    public record NoticeSummary(
            Long noticeId,
            String title,
            AuthorResponse author,
            LocalDateTime createdAt
    ) {
    }

    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}
