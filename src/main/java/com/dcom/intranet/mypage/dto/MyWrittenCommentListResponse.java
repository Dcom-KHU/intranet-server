package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.IntStream;

@Schema(description = "내가 쓴 댓글 목록 조회 응답 데이터")
public record MyWrittenCommentListResponse(
        @Schema(description = "전체 요소 수", example = "1")
        long total,

        @Schema(description = "내가 쓴 댓글 목록")
        List<MyWrittenCommentResponse> comments
) {
    public MyWrittenCommentListResponse(List<MyWrittenCommentResponse> commentList, PageInfoResponse pageInfo) {
        this(pageInfo.totalElements(), numberedComments(commentList, pageInfo));
    }

    public static MyWrittenCommentListResponse empty(int page, int size) {
        return new MyWrittenCommentListResponse(0, List.of());
    }

    private static List<MyWrittenCommentResponse> numberedComments(
            List<MyWrittenCommentResponse> comments,
            PageInfoResponse pageInfo
    ) {
        int startNumber = pageInfo.page() * pageInfo.size() + 1;
        return IntStream.range(0, comments.size())
                .mapToObj(index -> comments.get(index).withNumber(startNumber + index))
                .toList();
    }
}
