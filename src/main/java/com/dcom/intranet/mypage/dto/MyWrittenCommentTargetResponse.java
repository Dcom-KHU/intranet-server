package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.mypage.MyPageRouteType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 상세 이동 응답 데이터")
public record MyWrittenCommentTargetResponse(
        @Schema(description = "상세 페이지 이동 대상 URL segment", example = "info-posts")
        String targetType,

        @Schema(description = "상세 페이지 이동 대상 ID", example = "12")
        Long targetId,

        @Schema(description = "댓글 ID", example = "101")
        Long commentId
) {
    public MyWrittenCommentTargetResponse {
        targetType = MyPageRouteType.normalize(targetType);
    }
}
