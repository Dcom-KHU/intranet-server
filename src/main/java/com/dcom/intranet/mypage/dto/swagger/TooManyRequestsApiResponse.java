package com.dcom.intranet.mypage.dto.swagger;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "재요청 제한 응답")
public class TooManyRequestsApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "429")
    public int status;

    @Schema(description = "응답 메시지", example = "이미 진행 중인 이메일 인증 요청이 있습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
