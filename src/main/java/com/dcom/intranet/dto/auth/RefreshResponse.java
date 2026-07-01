package com.dcom.intranet.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public static RefreshResponse of(String accessToken, String refreshToken, long expiresIn){
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
