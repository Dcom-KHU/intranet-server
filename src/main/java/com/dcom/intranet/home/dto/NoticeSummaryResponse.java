package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 요약")
public record NoticeSummaryResponse(
        @Schema(description = "공지 ID", example = "1")
        Long id,

        @Schema(description = "공지 제목", example = "2026 D.COM 여름 프로젝트 팀 모집 안내")
        String title,

        @Schema(description = "작성자", example = "ADMIN")
        String author,

        @Schema(description = "작성일", example = "2026.06.20")
        String date,

        @Schema(description = "첨부파일 존재 여부", example = "true")
        boolean hasAttachment
) {
}
