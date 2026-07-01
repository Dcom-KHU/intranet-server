package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.mypage.MyPageRouteType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 쓴 댓글 목록 항목")
public record MyWrittenCommentResponse(
        @Schema(description = "댓글 ID", example = "101")
        Long commentId,

        @Schema(description = "댓글 대상 라우팅 타입", example = "info-posts")
        String type,

        @Schema(description = "댓글 대상 ID", example = "12")
        Long targetId,

        @Schema(description = "댓글 대상 제목", example = "React 참고 자료 모음")
        String targetTitle,

        @Schema(description = "댓글 내용", example = "좋은 자료 감사합니다.")
        String content,

        @Schema(description = "작성일시", example = "2026-06-01T13:00:00")
        LocalDateTime createdAt
) {
    public MyWrittenCommentResponse {
        type = MyPageRouteType.normalize(type);
    }
}
