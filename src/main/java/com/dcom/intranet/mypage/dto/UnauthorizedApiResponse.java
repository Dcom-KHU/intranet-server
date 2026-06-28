package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 실패 응답")
public class UnauthorizedApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "401")
    public int status;

    @Schema(description = "응답 메시지", example = "인증이 필요합니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
