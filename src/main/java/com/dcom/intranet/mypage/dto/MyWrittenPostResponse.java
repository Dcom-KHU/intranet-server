package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.mypage.MyPageRouteType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 쓴 글 목록 항목")
public record MyWrittenPostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "오픈소스SW개발방법및도구")
        String title,

        @Schema(description = "게시글 라우팅 타입", example = "archives")
        String type,

        @Schema(description = "작성일시", example = "2026-05-25T10:30:00")
        LocalDateTime createdAt
) {
    public MyWrittenPostResponse {
        type = MyPageRouteType.normalize(type);
    }
}
