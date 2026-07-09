package com.dcom.intranet.auth.dto.auth;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 정보 조회 응답")
public class MeResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "로그인 아이디", example = "dcom2024")
    private String loginId;

    @Schema(description = "이름", example = "하성준")
    private String name;

    @Schema(description = "학번", example = "20241234")
    private String studentId;

    @Schema(description = "이메일", example = "dcom2024@example.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "권한", example = "USER")
    private UserRole role;

    @Schema(description = "가입 상태", example = "APPROVED")
    private UserStatus status;

    @Schema(description = "임시 비밀번호 변경 필요 여부", example = "false")
    private boolean requirePasswordChange;

    public static MeResponse from(User user){
        return MeResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .requirePasswordChange(user.isTempPasswordValid())
                .build();
    }
}
