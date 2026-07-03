package com.dcom.intranet.mypage.dto.swagger;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리소스 없음 응답")
public class NotFoundApiResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "404")
    public int status;

    @Schema(description = "응답 메시지", example = "작성한 글을 찾을 수 없습니다.")
    public String message;

    @Schema(description = "응답 데이터", nullable = true)
    public Object data;
}
