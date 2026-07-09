package com.dcom.intranet.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 쓴 글 목록 조회 응답 데이터")
public record MyWrittenPostListResponse(
        @Schema(description = "전체 요소 수", example = "1")
        long total,

        @Schema(description = "내가 쓴 글 목록")
        List<MyWrittenPostResponse> posts
) {
    public MyWrittenPostListResponse(List<MyWrittenPostResponse> postList, PageInfoResponse pageInfo) {
        this(pageInfo.totalElements(), postList);
    }

    public static MyWrittenPostListResponse empty(int page, int size) {
        return new MyWrittenPostListResponse(0, List.of());
    }
}
