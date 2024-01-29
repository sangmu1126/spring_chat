package com.inha.everytown.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.entity.Platform;
import com.inha.everytown.domain.member.entity.Role;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import com.inha.everytown.global.jwt.JwtTokenProvider;
import com.inha.everytown.global.jwt.PrincipalDetails;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import com.inha.everytown.global.oauth.tokenDto.GoogleToken;
import com.inha.everytown.global.oauth.tokenDto.OauthToken;
import com.inha.everytown.global.oauth.userInfo.GoogleUserInfo;
import com.inha.everytown.global.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
public class GoogleAuthService implements OauthService {

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String REDIRECT_URI;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public GoogleAuthService(@Value("${oauth.google.client-id}") String clientId,
                             @Value("${oauth.google.secret-key}") String secretKey,
                             @Value("${oauth.google.redirect_uri}") String redirectUri,
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
                .fromUriString("https://accounts.google.com" )
                .path("/o/oauth2/v2/auth" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code" )
                .queryParam("scope", "email profile" )
                .queryParam("access_type", "offline" )
                .build();

        try {
            response.sendRedirect(url.toUriString());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_REDIRECT_FAILED);
        }
    }

    @Override
    public OauthToken getAccessToken(String code) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com" )
                .path("/token" )
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
        GoogleToken googleToken = null;
        try {
            googleToken = objectMapper.readValue(accessTokenResponse.getBody(), GoogleToken.class);
            setExpiresTime(googleToken);
            log.info("GOOGLE LOGIN : [{}]", googleToken);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.OAUTH_LOGIN_GET_TOKEN_FAILED);
        }
        return googleToken;
    }

    @Override
    public GoogleUserInfo getUserInfo(OauthToken oauthToken) {
        GoogleToken googleToken = (GoogleToken) oauthToken;
        String accessToken = googleToken.getAccess_token();
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/" )
                .path("/userinfo/v2/me" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> userInfoResponse = rt.exchange(
                    url.toUriString(),
                    HttpMethod.GET,
                    userInfoRequest,
                    GoogleUserInfo.class
            );

            return userInfoResponse.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_ACCESS_TOKEN_EXPIRED);
        }
    }

    @Override
    public JwtToken saveMemberAndPublishToken(OauthToken oauthToken) {
        GoogleUserInfo userInfo = getUserInfo(oauthToken);
        Optional<Member> findResult = memberRepository.findByEmailAndPlatform(userInfo.getEmail(), Platform.GOOGLE);

        Member member;
        if (findResult.isPresent()) {
            member = findResult.get();
        } else {
            member = Member.builder()
                    .nickname(userInfo.getName())
                    .email(userInfo.getEmail())
                    .platform(Platform.GOOGLE)
                    .thumbnail(userInfo.getPicture())
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
    public void logout(PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        if(member.getPlatform() != Platform.GOOGLE) return;

        GoogleToken googleToken = getToken(member.getId());

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com" )
                .path("/revoke" )
                .queryParam("token", googleToken.getAccess_token())
                .build();

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> logoutResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                null,
                String.class
        );

        redisService.deleteValue(member.getId().toString());
    }

    @Override
    public GoogleToken refresh(Long memberId, OauthToken oauthToken) {
        GoogleToken oldGoogleToken = (GoogleToken) oauthToken;

        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com" )
                .path("/token" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", CLIENT_ID);
        params.add("refresh_token", oldGoogleToken.getRefresh_token());
        params.add("client_secret", CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> refreshRequest = new HttpEntity<>(params, headers);

        ResponseEntity<GoogleToken> refreshResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                refreshRequest,
                GoogleToken.class
        );
        GoogleToken newGoogleToken = refreshResponse.getBody();
        newGoogleToken.setRefresh_token(oldGoogleToken.getRefresh_token());
        setExpiresTime(newGoogleToken);

        redisService.setValue(memberId.toString(), newGoogleToken, 50L, TimeUnit.DAYS);
        return newGoogleToken;
    }

    @Override
    public void updateUserInfo(Long memberId) {
        GoogleToken googleToken = getToken(memberId);
        GoogleUserInfo userInfo = getUserInfo(googleToken);

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND)
        );
        member.update(
                userInfo.getName(),
                userInfo.getEmail(),
                userInfo.getPicture()
        );
    }

    private boolean isExpired(GoogleToken googleToken) {
        long now = new Date().getTime() / 1000;
        return googleToken.getAccessTokenExpiresAt() <= now;
    }

    private GoogleToken getToken(Long memberId) {
        GoogleToken googleToken = (GoogleToken) redisService.getValue(memberId.toString());
        if (isExpired(googleToken)) {
            googleToken = refresh(memberId, googleToken);
        }
        return googleToken;
    }

    // Date.getTime()은 밀리초, expires_in은 초단위
    private void setExpiresTime(GoogleToken googleToken) {
        long nowInSec = new Date().getTime() / 1000;
        // 통신에 시간이 소요 되었기 때문에 2초 정도 만료 시간에 여유 둔다.
        long expiresAt = nowInSec + googleToken.getExpires_in() - 2;
        googleToken.setAccessTokenExpiresAt(expiresAt);
    }
}
