package com.dcom.intranet.auth.dto.auth;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accsessToken;
    private String refreshToken;
    private Long userId;
    private UserRole role;
    private UserStatus status;
    private boolean requirePasswordChange;

    public static LoginResponse of(User user, String accsessToken, String refreshToken,
                                   boolean requirePasswordChange){
        return LoginResponse.builder()
                .accsessToken(accsessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .role(user.getRole())
                .status(user.getStatus())
                .requirePasswordChange(requirePasswordChange)
                .build();
    }
}
