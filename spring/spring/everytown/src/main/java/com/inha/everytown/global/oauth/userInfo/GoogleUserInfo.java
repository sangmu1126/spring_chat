package com.inha.everytown.global.oauth.userInfo;

import lombok.Getter;

@Getter
public class GoogleUserInfo implements OAuthUserInfo {

    private String email;
    private String name;
    private String picture;
}
