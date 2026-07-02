package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 댓글 삭제 응답 데이터")
public record MyWrittenCommentDeleteResponse(
        @Schema(description = "처리 메시지", example = "작성한 댓글이 삭제되었습니다.")
        String message
) {
}
