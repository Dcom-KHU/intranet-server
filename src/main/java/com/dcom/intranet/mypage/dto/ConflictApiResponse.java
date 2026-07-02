package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "충돌 응답")
public class ConflictApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "409")
    public int status;

    @Schema(description = "응답 메시지", example = "이미 사용 중인 이메일입니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
