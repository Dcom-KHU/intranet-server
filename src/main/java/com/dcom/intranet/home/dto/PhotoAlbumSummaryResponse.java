package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "활동 사진 앨범 요약")
public record PhotoAlbumSummaryResponse(
        @Schema(description = "앨범 ID", example = "1")
        Long id,

        @Schema(description = "대표 이미지 URL 또는 프론트 목업 이미지 키", example = "khuBg1")
        String imageUrl,

        @Schema(description = "앨범 제목", example = "2026-1 D.COM 커리어세션")
        String title,

        @Schema(description = "활동일", example = "2026.05.16")
        String date,

        @Schema(description = "이미지 개수", example = "5")
        int imageCount
) {
}
