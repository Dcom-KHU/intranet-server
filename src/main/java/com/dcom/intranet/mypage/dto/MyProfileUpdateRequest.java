package com.dcom.intranet.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원정보 수정 요청")
public record MyProfileUpdateRequest(
        @NotBlank
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotBlank
        @Schema(description = "전화번호", example = "010-9999-8888")
        String phoneNumber,

        @Schema(description = "이메일 변경 인증 확인 API에서 발급받은 토큰", example = "generated-email-change-token")
        String emailChangeToken
) {
}
