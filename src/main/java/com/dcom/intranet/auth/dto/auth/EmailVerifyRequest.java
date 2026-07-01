package com.dcom.intranet.auth.dto.auth;

import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerifyRequest {
    @NotBlank(message = "이메일을 입력하지 않았습니다..")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증 코드를 입력하지 않았습니다.")
    private String verificationCode;
}

