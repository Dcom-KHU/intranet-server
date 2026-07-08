package com.dcom.intranet.auth.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "토큰 재발급 응답")
public class RefreshResponse {

    @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1NiJ9.xxxxx.yyyyy")
    private String accessToken;

    @Schema(description = "새로 발급된 Refresh Token. 기존 Refresh Token은 폐기됨(Rotation)", example = "eyJhbGciOiJIUzI1NiJ9.aaaaa.bbbbb")
    private String refreshToken;

    @Schema(description = "Access Token 만료까지 남은 시간(초)", example = "1800")
    private long expiresIn;

    public static RefreshResponse of(String accessToken, String refreshToken, long expiresIn){
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
