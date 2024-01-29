package com.inha.everytown.domain.member.service;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.entity.Platform;
import com.inha.everytown.global.jwt.PrincipalDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OauthServiceMapper {
    private static Map<Platform, OauthService> mapper = new HashMap<>();

    @Autowired
    public OauthServiceMapper(KakaoAuthService kakaoAuthService,
                              NaverAuthService naverAuthService,
                              GoogleAuthService googleAuthService) {

        mapper.put(Platform.KAKAO, kakaoAuthService);
        mapper.put(Platform.NAVER, naverAuthService);
        mapper.put(Platform.GOOGLE, googleAuthService);
    }

    public OauthService getService(Platform platform) {
        return mapper.get(platform);
    }

    public OauthService getService(PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        return mapper.get(member.getPlatform());
    }
}
