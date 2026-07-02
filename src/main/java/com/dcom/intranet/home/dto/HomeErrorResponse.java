package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈 API 오류 응답")
public record HomeErrorResponse(
        @Schema(description = "요청 성공 여부", example = "false")
        boolean success,

        @Schema(description = "HTTP 상태 코드", example = "401")
        int status,

        @Schema(description = "오류 메시지", example = "인증이 필요합니다.")
        String message,

        @Schema(description = "오류 응답 데이터", nullable = true)
        Object data
) {
}
