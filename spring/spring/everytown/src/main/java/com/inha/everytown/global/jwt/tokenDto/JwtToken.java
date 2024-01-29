package com.inha.everytown.global.jwt.tokenDto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtToken {

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresAt;

    @Builder
    public JwtToken(String grantType, String accessToken, String refreshToken, long accessTokenExpiresAt) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
}
