package com.dcom.intranet.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 변경 인증 메일 발송 요청")
public record EmailVerificationSendRequest(
        @NotBlank
        @Email
        @Schema(description = "새 이메일", example = "newhong@khu.ac.kr")
        String newEmail
) {
}
