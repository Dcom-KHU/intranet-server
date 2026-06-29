package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 쓴 글 목록 조회 응답 데이터")
public record MyWrittenPostListResponse(
        @Schema(description = "내가 쓴 글 목록")
        List<MyWrittenPostResponse> postList,

        @Schema(description = "페이지 정보")
        PageInfoResponse pageInfo
) {

    public static MyWrittenPostListResponse empty(int page, int size) {
        return new MyWrittenPostListResponse(
                List.of(),
                new PageInfoResponse(page, size, 0, 0)
        );
    }
}
