package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이메일 인증코드 발송 / 임시 비밀번호 발송 요청")
public class EmailSendRequest {

    @NotBlank(message = "이메일을 입력하지 않았습니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "인증코드 또는 임시 비밀번호를 받을 이메일", example = "dcom2024@example.com")
    private String email;
}
