package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정보 공유 게시글 요약")
public record InfoPostSummaryResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 제목", example = "시간 복잡도 Big-O 핵심 정리 (면접 필수)")
        String title,

        @Schema(description = "작성자")
        AuthorResponse author,

        @Schema(description = "작성일", example = "2026.06.20.")
        String date,

        @Schema(description = "첨부파일 존재 여부", example = "true")
        boolean hasAttachment
) {
}
