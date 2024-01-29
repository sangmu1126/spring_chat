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
import com.inha.everytown.global.oauth.tokenDto.NaverToken;
import com.inha.everytown.global.oauth.tokenDto.OauthToken;
import com.inha.everytown.global.oauth.userInfo.NaverUserInfo;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class NaverAuthService implements OauthService {

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String REDIRECT_URI;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public NaverAuthService(@Value("${oauth.naver.client-id}") String clientId,
                            @Value("${oauth.naver.secret-key}") String secretKey,
                            @Value("${oauth.naver.redirect_uri}") String redirectUri,
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
                .fromUriString("https://nid.naver.com" )
                .path("/oauth2.0/authorize" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("response_type", "code" )
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "1234", StandardCharsets.UTF_8)
                .build();

        try {
            response.sendRedirect(url.toUriString());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_REDIRECT_FAILED);
        }
    }

    @Override
    public NaverToken getAccessToken(String code) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://nid.naver.com" )
                .path("/oauth2.0/token" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("grant_type", "authorization_code" )
                .queryParam("state", "1234" )
                .queryParam("code", code)
                .build();

        ResponseEntity<String> accessTokenResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                null,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        NaverToken naverToken = null;
        try {
            naverToken = objectMapper.readValue(accessTokenResponse.getBody(), NaverToken.class);
            setExpiresAt(naverToken);
            log.info("NAVER LOGIN : [{}]", naverToken);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.OAUTH_LOGIN_GET_TOKEN_FAILED);
        }
        return naverToken;
    }

    @Override
    public NaverUserInfo getUserInfo(OauthToken oauthToken) {
        NaverToken naverToken = (NaverToken) oauthToken;
        String accessToken = naverToken.getAccess_token();
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com" )
                .path("/v1/nid/me" )
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<NaverUserInfo> userInfoResponse = rt.exchange(
                    url.toUriString(),
                    HttpMethod.POST,
                    userInfoRequest,
                    NaverUserInfo.class
            );

            return userInfoResponse.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_ACCESS_TOKEN_EXPIRED);
        }
    }

    @Override
    public JwtToken saveMemberAndPublishToken(OauthToken oauthToken) {
        NaverUserInfo userInfo = getUserInfo(oauthToken);
        Optional<Member> findResult = memberRepository.findByEmailAndPlatform(userInfo.getResponse().getEmail(), Platform.NAVER);

        Member member;
        if (findResult.isPresent()) {
            member = findResult.get();
        } else {
            member = Member.builder()
                    .nickname(userInfo.getResponse().getNickname())
                    .email(userInfo.getResponse().getEmail())
                    .platform(Platform.NAVER)
                    .thumbnail(userInfo.getResponse().getProfile_image())
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
        NaverToken naverToken = getToken(memberId);
        NaverUserInfo userInfo = getUserInfo(naverToken);

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND)
        );
        member.update(
                userInfo.getResponse().getNickname(),
                userInfo.getResponse().getEmail(),
                userInfo.getResponse().getProfile_image()
        );
    }

    @Override
    public void logout(PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        if(member.getPlatform() != Platform.NAVER) return;

        NaverToken naverToken = getToken(member.getId());

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://nid.naver.com" )
                .path("/oauth2.0/token" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("grant_type", "delete" )
                .queryParam("access_token", naverToken.getAccess_token())
                .queryParam("service_provider", "NAVER" )
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
    public NaverToken refresh(Long memberId, OauthToken oauthToken) {
        NaverToken naverToken = (NaverToken) oauthToken;

        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://nid.naver.com" )
                .path("/oauth2.0/token" )
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("grant_type", "refresh_token" )
                .queryParam("refresh_token", naverToken.getRefresh_token())
                .build();

        ResponseEntity<NaverToken> refreshResponse = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                null,
                NaverToken.class
        );
        NaverToken newNaverToken = refreshResponse.getBody();
        newNaverToken.setRefresh_token(naverToken.getRefresh_token());
        setExpiresAt(newNaverToken);

        redisService.setValue(memberId.toString(), newNaverToken, 50L, TimeUnit.DAYS);
        return newNaverToken;
    }

    private boolean isExpired(NaverToken naverToken) {
        long nowInSec = new Date().getTime() / 1000;
        return naverToken.getAccessTokenExpiresAt() <= nowInSec;
    }

    private NaverToken getToken(Long memberId) {
        NaverToken naverToken = (NaverToken) redisService.getValue(memberId.toString());
        if (isExpired(naverToken)) {
            naverToken = refresh(memberId, naverToken);
        }
        return naverToken;
    }

    // Date.getTime()은 밀리초, expires_in은 초단위
    private void setExpiresAt(NaverToken naverToken) {
        long now = new Date().getTime() / 1000;
        // 통신에 시간이 소요 되었기 때문에 2초 정도 만료 시간메 여유 둔다.
        long expiresAt = now + naverToken.getExpires_in() - 2;
        naverToken.setAccessTokenExpiresAt(expiresAt);
    }
}
