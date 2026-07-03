package com.dcom.intranet.mypage.dto.swagger;

import com.dcom.intranet.mypage.dto.response.PasswordChangeResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 성공 응답")
public class PasswordChangeApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    public String message;

    @Schema(description = "비밀번호 변경 결과")
    public PasswordChangeResponse data;
}
