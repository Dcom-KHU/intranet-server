package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Home API 에러 응답")
public record HomeErrorResponse(
        @Schema(description = "에러 메시지", example = "인증이 필요합니다.")
        String message
) {
}
