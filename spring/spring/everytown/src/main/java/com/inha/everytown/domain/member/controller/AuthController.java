package com.inha.everytown.domain.member.controller;

import com.inha.everytown.domain.member.dto.request.RefreshTokenReq;
import com.inha.everytown.domain.member.entity.Platform;
import com.inha.everytown.domain.member.service.CommonAuthService;
import com.inha.everytown.domain.member.service.OauthService;
import com.inha.everytown.domain.member.service.OauthServiceMapper;
import com.inha.everytown.global.jwt.JwtAttribute;
import com.inha.everytown.global.jwt.PrincipalDetails;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import com.inha.everytown.global.oauth.tokenDto.OauthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OauthServiceMapper oauthServiceMapper;
    private final CommonAuthService commonAuthService;

    @PostMapping("/reissue")
    public ResponseEntity<JwtToken> reissue(@RequestBody RefreshTokenReq refreshTokenReq) {
        JwtToken jwtToken = commonAuthService.reissue(refreshTokenReq.getRefreshToken());
        return ResponseEntity.ok(jwtToken);
    }

    @GetMapping("/login/{platform}")
    public ResponseEntity<Void> getLoginUI(@PathVariable Platform platform,
                                             HttpServletResponse response) {
        OauthService oauthService = oauthServiceMapper.getService(platform);
        oauthService.redirectToLoginUrl(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/redirected/{platform}")
    public ResponseEntity<JwtToken> OAuthLogin(@PathVariable Platform platform,
                                               @RequestParam String code) {

        OauthService oauthService = oauthServiceMapper.getService(platform);
        OauthToken oauthToken = oauthService.getAccessToken(code);
        JwtToken jwtToken = oauthService.saveMemberAndPublishToken(oauthToken);
        return ResponseEntity.ok(jwtToken);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/logout")
    public ResponseEntity<Void> OAuthLogout(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                            HttpServletRequest request,
                                            @RequestBody RefreshTokenReq refreshTokenReq) {

        String accessToken = request.getHeader(JwtAttribute.HEADER).split(" ")[1].trim();
        String refreshToken = refreshTokenReq.getRefreshToken();

        OauthService oauthService = oauthServiceMapper.getService(principalDetails);
        oauthService.logout(principalDetails);
        commonAuthService.deprecateJwtToken(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/userinfo")
    public ResponseEntity<Void> updateUserInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long memberId = principalDetails.getMember().getId();

        OauthService oauthService = oauthServiceMapper.getService(principalDetails);
        oauthService.updateUserInfo(memberId);
        return ResponseEntity.ok().build();
    }
}
