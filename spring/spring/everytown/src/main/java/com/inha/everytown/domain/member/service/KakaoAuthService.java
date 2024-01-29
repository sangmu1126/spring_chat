package com.inha.everytown.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.everytown.global.oauth.tokenDto.KakaoToken;
import com.inha.everytown.global.oauth.tokenDto.OauthToken;
import com.inha.everytown.global.oauth.userInfo.KakaoUserInfo;
import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.entity.Platform;
import com.inha.everytown.domain.member.entity.Role;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import com.inha.everytown.global.jwt.JwtTokenProvider;
import com.inha.everytown.global.jwt.PrincipalDetails;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import com.inha.everytown.global.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class KakaoAuthService implements OauthService {

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String REDIRECT_URI;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;


    @Autowired
    public KakaoAuthService(@Value("${oauth.kakao.client-id}") String clientId,
                            @Value("${oauth.kakao.secret-key}") String secretKey,
                            @Value("${oauth.kakao.redirect_uri}") String redirectUri,
                            MemberRepository memberRepository,
                            RedisService redisService,
                            JwtTokenProvider jwtTokenProvider) {

        this.CLIENT_ID = clientId;
        this.CLIENT_SECRET = secretKey;
        this.REDIRECT_URI = redirectUri;
        this.memberRepository = memberRepository;
        this.redisService = redisService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void redirectToLoginUrl(HttpServletResponse response) {

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com" )
                .path("/oauth/authorize" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code" )
                .build();

        try {
            response.sendRedirect(url.toUriString());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_REDIRECT_FAILED);
        }
    }

    @Override
    public KakaoToken getAccessToken(String code) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com" )
                .path("/oauth/token" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", CLIENT_ID);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("code", code);
        params.add("client_secret", CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> accessTokenRequest
                = new HttpEntity<>(params, headers);

        ResponseEntity<String> accessTokenResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                accessTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoToken = null;
        try {
            kakaoToken = objectMapper.readValue(accessTokenResponse.getBody(), KakaoToken.class);
            setExpiresTime(kakaoToken);
            log.info("KAKAO LOGIN : [{}]", kakaoToken);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.OAUTH_LOGIN_GET_TOKEN_FAILED);
        }
        return kakaoToken;
    }

    @Override
    public KakaoUserInfo getUserInfo(OauthToken oauthToken) {
        KakaoToken kakaoToken = (KakaoToken) oauthToken;
        String accessToken = kakaoToken.getAccess_token();
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com" )
                .path("/v2/user/me" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfo> userInfoResponse = rt.exchange(
                    url.toUriString(),
                    HttpMethod.POST,
                    userInfoRequest,
                    KakaoUserInfo.class
            );

            return userInfoResponse.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_ACCESS_TOKEN_EXPIRED);
        }
    }

    @Override
    public JwtToken saveMemberAndPublishToken(OauthToken oauthToken) {
        KakaoUserInfo userInfo = getUserInfo(oauthToken);
        Optional<Member> findResult = memberRepository.findByEmailAndPlatform(userInfo.getKakao_account().getEmail(), Platform.KAKAO);

        Member member;
        if (findResult.isPresent()) {
            member = findResult.get();
        } else {
            member = Member.builder()
                    .nickname(userInfo.getKakao_account().getProfile().getNickname())
                    .email(userInfo.getKakao_account().getEmail())
                    .platform(Platform.KAKAO)
                    .thumbnail(userInfo.getKakao_account().getProfile().getThumbnail_image_url())
                    .role(Role.USER)
                    .build();
            memberRepository.save(member);
        }

        redisService.setValue(member.getId().toString(), oauthToken, 50L, TimeUnit.DAYS);   // refresh token의 validation time이 대충 59정도

        JwtToken jwtToken = jwtTokenProvider.createToken(member);
        redisService.setValue(jwtToken.getRefreshToken(), member.getId().toString(), 7L, TimeUnit.DAYS);
        return jwtToken;
    }

    @Override
    public void updateUserInfo(Long memberId) {
        KakaoToken kakaoToken = getToken(memberId);
        KakaoUserInfo userInfo = getUserInfo(kakaoToken);

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND)
        );
        member.update(
                userInfo.getKakao_account().getProfile().getNickname(),
                userInfo.getKakao_account().getEmail(),
                userInfo.getKakao_account().getProfile().getThumbnail_image_url()
        );
    }

    @Override
    public void logout(PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        if(member.getPlatform() != Platform.KAKAO) return;

        KakaoToken kakaoToken = getToken(member.getId());

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com" )
                .path("/v1/user/logout" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> logoutRequest = new HttpEntity<>(headers);

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> logoutResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                logoutRequest,
                String.class
        );

        redisService.deleteValue(member.getId().toString());
    }

    @Override
    public KakaoToken refresh(Long memberId, OauthToken oauthToken) {
        KakaoToken kakaoToken = (KakaoToken) oauthToken;

        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com" )
                .path("/oauth/token" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", CLIENT_ID);
        params.add("refresh_token", kakaoToken.getRefresh_token());
        params.add("client_secret", CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> refreshRequest = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoToken> refreshResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                refreshRequest,
                KakaoToken.class
        );
        KakaoToken newKakaoToken = refreshResponse.getBody();
        newKakaoToken.setScope(kakaoToken.getScope());
        setExpiresTime(newKakaoToken);

        // 카카오에서 refresh token의 유효기간이 충분하면 갱신 안해줌
        if (newKakaoToken.getRefresh_token() == null) {
            newKakaoToken.setRefresh_token(kakaoToken.getRefresh_token());
            newKakaoToken.setRefresh_token_expires_in(kakaoToken.getRefresh_token_expires_in());
        }
        redisService.setValue(memberId.toString(), newKakaoToken, 50L, TimeUnit.DAYS);

        return newKakaoToken;
    }

    private boolean isExpired(KakaoToken kakaoToken) {
        long nowInSec = new Date().getTime() / 1000;
        return kakaoToken.getAccessTokenExpiresAt() <= nowInSec;
    }

    private KakaoToken getToken(Long memberId) {
        KakaoToken kakaoToken = (KakaoToken) redisService.getValue(memberId.toString());
        if (isExpired(kakaoToken)) {
            kakaoToken = refresh(memberId, kakaoToken);
        }
        return kakaoToken;
    }

    // Date.getTime()은 밀리초, expires_in은 초단위
    private void setExpiresTime(KakaoToken kakaoToken) {
        long nowInSec = new Date().getTime() / 1000;
        // 통신에 시간이 소요 되었기 때문에 2초 정도 만료 시간에 여유 둔다.
        long expiresAt = nowInSec + kakaoToken.getExpires_in() - 2;
        kakaoToken.setAccessTokenExpiresAt(expiresAt);
    }
}
