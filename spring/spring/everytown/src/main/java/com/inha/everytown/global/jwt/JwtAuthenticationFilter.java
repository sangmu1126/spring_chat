package com.inha.everytown.global.jwt;

import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import com.inha.everytown.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            // Is token valid?
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Is it on Black List?
                if ("Deprecated".equals(redisService.getValue(token))) {
                    request.setAttribute("exception", ErrorCode.AUTH_DEPRECATED_TOKEN);
                    filterChain.doFilter(request, response);
                    return;
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else request.setAttribute("exception", ErrorCode.JWT_ABSENCE_TOKEN);
        } catch (CustomException e) {
            request.setAttribute("exception", e.getErrorCode());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(JwtAttribute.HEADER);
        if (StringUtils.hasText(token) && token.startsWith(JwtAttribute.GRANT_TYPE)) {
            return token.split(" ")[1].trim();
        }
        return null;
    }
}
