package com.dcom.intranet.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 응답 데이터")
public record PasswordChangeResponse(
        @Schema(description = "처리 메시지", example = "비밀번호가 변경되었습니다.")
        String message
) {
}
