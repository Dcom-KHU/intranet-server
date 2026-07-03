package com.dcom.intranet.mypage.dto.swagger;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "만료 응답")
public class GoneApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "410")
    public int status;

    @Schema(description = "응답 메시지", example = "이메일 인증이 만료되었습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
