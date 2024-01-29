package com.inha.everytown.global.oauth.userInfo;

import lombok.Getter;

@Getter
public class KakaoUserInfo implements OAuthUserInfo {

    private Long id;
    private String connected_at;
    private KakaoAccount kakao_account;

    @Getter
    public class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        public class Profile {
            private String nickname;
            private String thumbnail_image_url;
        }
    }
}

