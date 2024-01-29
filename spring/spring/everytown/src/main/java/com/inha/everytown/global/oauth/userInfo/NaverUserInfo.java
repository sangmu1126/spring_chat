package com.inha.everytown.global.oauth.userInfo;

import lombok.Getter;

@Getter
public class NaverUserInfo implements OAuthUserInfo {

    private Response response;

    @Getter
    public class Response {
        private String nickname;
        private String email;
        private String profile_image;
    }
}
