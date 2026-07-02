package com.dcom.intranet.auth.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckLoginIdResponse {
    private boolean isAvailable;
    private String message;

    public static CheckLoginIdResponse of(boolean isAvailable){
        return CheckLoginIdResponse.builder()
                .isAvailable(isAvailable)
                .message(isAvailable ? "사용가능한 아이디입니다." : "이미 사용 중인 아이디입니다.")
                .build();
    }
}
