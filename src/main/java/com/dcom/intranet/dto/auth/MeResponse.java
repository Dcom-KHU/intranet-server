package com.dcom.intranet.dto.auth;

import com.dcom.intranet.domain.User;
import com.dcom.intranet.domain.UserRole;
import com.dcom.intranet.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeResponse {
    private Long userId;
    private String loginId;
    private String name;
    private String studentId;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;

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
                .build();
    }
}
