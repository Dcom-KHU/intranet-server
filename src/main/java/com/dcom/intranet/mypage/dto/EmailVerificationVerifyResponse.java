package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 변경 인증 확인 응답 데이터")
public record EmailVerificationVerifyResponse(
        @Schema(description = "회원정보 수정 API에서 사용할 이메일 변경 토큰", example = "generated-email-change-token")
        String emailChangeToken,

        @Schema(description = "처리 메시지", example = "이메일 변경 인증이 완료되었습니다.")
        String message,

        @Schema(description = "인증 완료된 이메일", example = "newhong@khu.ac.kr")
        String verifiedEmail
) {
}
