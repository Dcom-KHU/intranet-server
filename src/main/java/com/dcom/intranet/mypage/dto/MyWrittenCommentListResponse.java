package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 쓴 댓글 목록 조회 응답 데이터")
public record MyWrittenCommentListResponse(
        @Schema(description = "내가 쓴 댓글 목록")
        List<MyWrittenCommentResponse> commentList,

        @Schema(description = "페이지 정보")
        PageInfoResponse pageInfo
) {

    public static MyWrittenCommentListResponse empty(int page, int size) {
        return new MyWrittenCommentListResponse(
                List.of(),
                new PageInfoResponse(page, size, 0, 0)
        );
    }
}
