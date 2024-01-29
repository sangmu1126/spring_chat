package com.inha.everytown.domain.member.service;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.member.repository.MemberRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import com.inha.everytown.global.jwt.JwtTokenProvider;
import com.inha.everytown.global.jwt.tokenDto.JwtToken;
import com.inha.everytown.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class CommonAuthService {

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public JwtToken reissue(String refreshToken) {
        String value = (String) redisService.getValue(refreshToken);
        // 유효성 검사
        if ("Deprecated".equals(value) || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        Long memberId = Long.valueOf(value);
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.AUTH_MEMBER_NOT_FOUND));
        JwtToken newJwtToken = jwtTokenProvider.createToken(member);

        redisService.setValue(refreshToken, "Deprecated", 7L, TimeUnit.DAYS);
        redisService.setValue(newJwtToken.getRefreshToken(), memberId.toString(), 7L, TimeUnit.DAYS);
        return newJwtToken;
    }

    public void deprecateJwtToken(String accessToken, String refreshToken) {
        redisService.setValue(accessToken, "Deprecated", 30L, TimeUnit.MINUTES);
        redisService.setValue(refreshToken, "Deprecated", 7L, TimeUnit.DAYS);
    }
}
