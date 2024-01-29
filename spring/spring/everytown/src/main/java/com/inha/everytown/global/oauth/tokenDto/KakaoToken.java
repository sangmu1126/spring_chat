package com.inha.everytown.global.oauth.tokenDto;

import lombok.Data;

@Data
public class KakaoToken implements OauthToken {

    private String token_type;
    private String access_token;
    private Integer expires_in;
    private String refresh_token;
    private Integer refresh_token_expires_in;
    private String scope;

    private long accessTokenExpiresAt;
}
