package com.dcom.intranet.dto.auth;


import com.dcom.intranet.domain.User;
import com.dcom.intranet.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SignupResponse {
    private Long userId;
    private String loginId;
    private String studentId;
    private String email;
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
