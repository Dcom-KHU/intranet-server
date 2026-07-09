package com.dcom.intranet.mypage.dto.response;

import com.dcom.intranet.mypage.domain.MyPageRouteType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "내가 쓴 글 목록 항목")
public record MyWrittenPostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "족보 게시글 ID", example = "467")
        Long recordId,

        @Schema(description = "제목", example = "오픈소스SW개발방법및도구")
        String title,

        @Schema(description = "교수명", example = "최진영")
        String professor,

        @Schema(description = "게시글 라우팅 타입", example = "archives")
        String type,

        @Schema(description = "작성일시", example = "2026-05-25T10:30:00")
        LocalDateTime createdAt
) {
    public MyWrittenPostResponse(Long id, String title, String type, LocalDateTime createdAt) {
        this(id, null, title, null, type, createdAt);
    }

    public MyWrittenPostResponse {
        type = MyPageRouteType.normalize(type);
    }
}
