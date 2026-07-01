package com.dcom.intranet.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "새 비밀번호를 입력하세요.")
    private String newPassword;
}
