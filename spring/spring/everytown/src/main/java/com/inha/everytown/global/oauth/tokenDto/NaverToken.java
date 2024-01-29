package com.inha.everytown.global.oauth.tokenDto;

import lombok.Data;

@Data
public class NaverToken implements OauthToken {

    private String token_type;
    private String access_token;
    private Integer expires_in;
    private String refresh_token;

    private String error;
    private String error_description;

    private long accessTokenExpiresAt;
}
