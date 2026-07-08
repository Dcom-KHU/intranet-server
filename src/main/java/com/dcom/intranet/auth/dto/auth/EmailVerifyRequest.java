package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이메일 인증코드 확인 요청")
public class EmailVerifyRequest {
    @NotBlank(message = "이메일을 입력하지 않았습니다..")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "인증코드를 발송받은 이메일", example = "dcom2024@example.com")
    private String email;

    @NotBlank(message = "인증 코드를 입력하지 않았습니다.")
    @Schema(description = "이메일로 발송된 6자리 인증 코드. 발급 후 5분간 유효", example = "482913")
    private String verificationCode;
}

