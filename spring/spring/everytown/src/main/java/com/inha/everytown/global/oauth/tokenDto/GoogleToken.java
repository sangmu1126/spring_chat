package com.inha.everytown.global.oauth.tokenDto;

import lombok.Data;

@Data
public class GoogleToken implements OauthToken {

    private String token_type;
    private String access_token;
    private Integer expires_in;
    private String refresh_token;
    private String scope;
    private String id_token;
    private long accessTokenExpiresAt;
}
