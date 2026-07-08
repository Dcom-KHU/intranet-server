package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "토큰 재발급 / 로그아웃 요청")
public class RefreshRequest {
    @NotBlank(message = "Refresh Token은 필수입니다.")
    @Schema(description = "로그인 시 발급받은 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.aaaaa.bbbbb")
    private String refreshToken;
}
