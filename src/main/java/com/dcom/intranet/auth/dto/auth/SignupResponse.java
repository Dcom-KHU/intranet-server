package com.dcom.intranet.auth.dto.auth;


import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "회원가입 응답")
public class SignupResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "로그인 아이디", example = "dcom2024")
    private String loginId;

    @Schema(description = "학번", example = "20241234")
    private String studentId;

    @Schema(description = "이메일", example = "dcom2024@example.com")
    private String email;

    @Schema(description = "가입 상태. 가입 직후에는 항상 관리자 승인 대기(PENDING) 상태", example = "PENDING")
    private UserStatus status;

    public static SignupResponse from(User user){
        return SignupResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }


}
