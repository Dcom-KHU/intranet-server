package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권한 없음 응답")
public class ForbiddenApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "403")
    public int status;

    @Schema(description = "응답 메시지", example = "삭제 권한이 없습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
