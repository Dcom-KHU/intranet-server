package com.dcom.intranet.auth.dto.auth;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 응답")
public class LoginResponse {
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9.xxxxx.yyyyy")
    private String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.aaaaa.bbbbb")
    private String refreshToken;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "권한", example = "USER")
    private UserRole role;

    @Schema(description = "가입 상태. APPROVED 상태만 로그인이 허용됨", example = "APPROVED")
    private UserStatus status;

    @Schema(description = "임시 비밀번호로 로그인했는지 여부. true인 경우 /api/auth/password로 새 비밀번호를 설정해야 함", example = "false")
    private boolean requirePasswordChange;

    public static LoginResponse of(User user, String accessToken, String refreshToken,
                                   boolean requirePasswordChange){
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .role(user.getRole())
                .status(user.getStatus())
                .requirePasswordChange(requirePasswordChange)
                .build();
    }
}
