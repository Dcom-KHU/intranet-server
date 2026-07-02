package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 성공 응답")
public class MemberWithdrawApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    public String message;

    @Schema(description = "회원 탈퇴 결과")
    public MemberWithdrawResponse data;
}
