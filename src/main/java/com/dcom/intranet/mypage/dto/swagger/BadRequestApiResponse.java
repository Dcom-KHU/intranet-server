package com.dcom.intranet.mypage.dto.swagger;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요청값 오류 응답")
public class BadRequestApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "400")
    public int status;

    @Schema(description = "응답 메시지", example = "요청값이 올바르지 않습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
