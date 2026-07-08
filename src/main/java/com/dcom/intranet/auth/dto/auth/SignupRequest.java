package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "로그인 아이디. 다른 회원과 중복될 수 없음", example = "dcom2024")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password1234!")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Schema(description = "이름", example = "하성준")
    private String name;

    @NotBlank(message = "학번은 필수입니다")
    @Schema(description = "학번. 다른 회원과 중복될 수 없음", example = "20241234")
    private String studentId;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Schema(description = "이메일. 도메인 제한 없음. /api/auth/email/verify로 인증을 완료한 이메일이어야 함", example = "dcom2024@example.com")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    /// NotBlank와 Email은 Validation 어노테이션으로
    /// NotBlank는 빈 문자열이나 null이면 400 Error
    /// Email은 이메일 형식이 아니면 400 Error을 띄워준다.
}

