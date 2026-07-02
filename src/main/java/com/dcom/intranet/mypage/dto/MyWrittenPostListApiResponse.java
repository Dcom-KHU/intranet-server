package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 글 목록 조회 성공 응답")
public class MyWrittenPostListApiResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 메시지", example = "요청에 성공했습니다.")
    public String message;

    @Schema(description = "내가 쓴 글 목록 조회 결과")
    public MyWrittenPostListResponse data;
}
