package com.dcom.intranet.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
