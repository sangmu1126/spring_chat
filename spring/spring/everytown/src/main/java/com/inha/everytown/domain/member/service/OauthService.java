package com.inha.everytown.domain.member.service;

import com.inha.everytown.global.jwt.PrincipalDetails;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import com.inha.everytown.global.oauth.tokenDto.OauthToken;
import com.inha.everytown.global.oauth.userInfo.OAuthUserInfo;

import javax.servlet.http.HttpServletResponse;

public interface OauthService {

    void redirectToLoginUrl(HttpServletResponse response);

    OauthToken getAccessToken(String code);

    OAuthUserInfo getUserInfo(OauthToken oauthToken);

    JwtToken saveMemberAndPublishToken(OauthToken oauthToken);

    void logout(PrincipalDetails principalDetails);

    OauthToken refresh(Long memberId, OauthToken oauthToken);

    void updateUserInfo(Long memberId);
}
