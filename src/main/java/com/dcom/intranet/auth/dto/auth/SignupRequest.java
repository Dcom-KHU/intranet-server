package com.dcom.intranet.auth.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "학번은 필수입니다")
    private String studentId;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;

    /// NotBlank와 Email은 Validation 어노테이션으로
    /// NotBlank는 빈 문자열이나 null이면 400 Error
    /// Email은 이메일 형식이 아니면 400 Error을 띄워준다.
}

