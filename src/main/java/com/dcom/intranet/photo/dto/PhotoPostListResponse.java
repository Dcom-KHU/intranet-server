package com.dcom.intranet.photo.dto;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public record PhotoPostListResponse(
        List<AlbumSummary> albumList,
        PageInfo pageInfo
) {

    public static PhotoPostListResponse from(Page<AlbumSummary> page) {
        return new PhotoPostListResponse(
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

    public record AlbumSummary(
            Long albumId,
            String coverImageUrl,
            String eventName,
            LocalDate activityDate
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
