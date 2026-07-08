package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 재설정 요청")
public class PasswordResetRequest {
    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Schema(description = "새로 설정할 비밀번호", example = "newPassword1234!")
    private String newPassword;
}
