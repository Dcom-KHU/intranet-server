package com.dcom.intranet.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "페이지 정보")
public record PageInfoResponse(
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "전체 요소 수", example = "1")
        long totalElements
) {
}
