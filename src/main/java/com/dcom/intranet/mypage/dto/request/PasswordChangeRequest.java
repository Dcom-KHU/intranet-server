package com.dcom.intranet.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호. 임시 비밀번호 변경 필요 상태가 아닌 경우 필수", example = "current-password")
        String currentPassword,

        @NotBlank
        @Schema(description = "새 비밀번호", example = "changed-password")
        String newPassword
) {
}
