package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "족보 요약")
public record ArchiveSummaryResponse(
        @Schema(description = "족보 ID", example = "1")
        Long id,

        @Schema(description = "과목명", example = "오픈소스SW개발방법및도구")
        String subject,

        @Schema(description = "교수명", example = "이성원")
        String professor,

        @Schema(description = "작성자")
        AuthorResponse author,

        @Schema(description = "등록일", example = "2026.05.25")
        String date
) {
}
