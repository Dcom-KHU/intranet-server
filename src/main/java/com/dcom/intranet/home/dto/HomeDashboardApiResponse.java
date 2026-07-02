package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메인 대시보드 API 성공 응답")
public record HomeDashboardApiResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "HTTP 상태 코드", example = "200")
        int status,

        @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
        String message,

        @Schema(description = "메인 대시보드 데이터")
        HomeDashboardResponse data
) {

    public static HomeDashboardApiResponse success(HomeDashboardResponse data) {
        return new HomeDashboardApiResponse(true, 200, "요청에 성공했습니다.", data);
    }
}
