package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작성자 요약")
public record AuthorResponse(
        @Schema(description = "학번", example = "20230001")
        String studentNumber,

        @Schema(description = "이름", example = "하성준")
        String name
) {
}
