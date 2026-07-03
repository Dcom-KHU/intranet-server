package com.dcom.intranet.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 변경 인증 확인 요청")
public record EmailVerificationVerifyRequest(
        @NotBlank
        @Email
        @Schema(description = "새 이메일", example = "newhong@khu.ac.kr")
        String newEmail,

        @NotBlank
        @Schema(description = "인증 코드", example = "123456")
        String verificationCode
) {
}
