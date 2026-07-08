package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "아이디 중복 확인 응답")
public class CheckLoginIdResponse {
    @Schema(description = "사용 가능 여부", example = "true")
    private boolean isAvailable;

    @Schema(description = "결과 메시지", example = "사용가능한 아이디입니다.")
    private String message;

    public static CheckLoginIdResponse of(boolean isAvailable){
        return CheckLoginIdResponse.builder()
                .isAvailable(isAvailable)
                .message(isAvailable ? "사용가능한 아이디입니다." : "이미 사용 중인 아이디입니다.")
                .build();
    }
}
