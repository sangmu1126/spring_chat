package com.inha.everytown.global.jwt;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final MemberRepository memberRepository;
    private final String SECRET_KEY;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.token.secret-key}") String secret,
                            MemberRepository memberRepository) {

        this.SECRET_KEY = Base64.getEncoder().encodeToString(secret.getBytes());
        this.memberRepository = memberRepository;
    }

    public JwtToken createToken(Member member) {
        long now = new Date().getTime();
        Date accessTokenExpireTime = new Date(now + JwtAttribute.ACCESS_TOKEN_VALID_TIME);
        Date refreshTokenExpireTime = new Date(now + JwtAttribute.REFRESH_TOKEN_VALID_TIME);

        String accessToken = Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("email", member.getEmail())
                .claim("platform", member.getPlatform())
                .setExpiration(accessTokenExpireTime)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpireTime)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        return JwtToken.builder()
                .grantType(JwtAttribute.GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpireTime.getTime() / 1000)
                .build();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(accessToken).getBody();

        Long memberId = Long.valueOf(claims.getSubject());
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND));
        PrincipalDetails principal = new PrincipalDetails(member);

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND);
        }
    }
}
