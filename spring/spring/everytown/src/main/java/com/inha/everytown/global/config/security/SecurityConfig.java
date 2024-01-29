package com.inha.everytown.global.config.security;

import com.inha.everytown.global.jwt.JwtAccessDeniedHandler;
import com.inha.everytown.global.jwt.JwtAuthenticationEntryPoint;
import com.inha.everytown.global.jwt.JwtAuthenticationFilter;
import com.inha.everytown.global.jwt.JwtTokenProvider;
import com.inha.everytown.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf().disable()   //CSRF 기능을 꺼야 동작한다
                .cors().configurationSource(corsConfigurationSource())

                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisService), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //JWT방식에서는 session 관리 X

                .and()
                .formLogin().disable()  //Security의 formLogin은 session login 방식에서 자동처리를 위한 것
                .httpBasic().disable()  //알림창을 통해 request header에 id, pw 넣어 보내는 형식. 보안에 매우 취약

                .authorizeRequests()
                .antMatchers("/users/**").authenticated()
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll()
                .and().build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 모든 출처 허용
        config.addAllowedOriginPattern("*");
        // 모든 요청 헤더 허용
        config.addAllowedHeader("*");
        // 모든 HTTP 메서드 허용
        config.addAllowedMethod("*");
        // 요청 응답간 인증 정보 주고 받을 수 있게 허용
        config.setAllowCredentials(true);

        // 브라우저가 접근 가능한 헤더
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
