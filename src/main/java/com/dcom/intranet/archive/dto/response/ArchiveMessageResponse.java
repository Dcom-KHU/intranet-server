package com.dcom.intranet.archive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "공통 메시지 응답")
public class ArchiveMessageResponse {

    @Schema(description = "응답 메시지", example = "족보가 삭제되었습니다.")
    private final String message;

    public ArchiveMessageResponse(String message) {
        this.message = message;
    }
}