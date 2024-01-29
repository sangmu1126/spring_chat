package com.inha.everytown.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    JWT_ABSENCE_TOKEN(HttpStatus.BAD_REQUEST, "JWT_001", "토큰이 없습니다"),
    JWT_VALID_FAILED(HttpStatus.BAD_REQUEST, "JWT_002", "토큰 유효성 검증 실패"),

    AUTH_INVALID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_001", "토큰이 유효하지 않습니다."),
    AUTH_DEPRECATED_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_002", "파기된 토큰입니다"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_003", "유효하지 않은 refresh token 입니다"),
    AUTH_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_004", "사용자를 찾을 수 없습니다."),

    OAUTH_LOGIN_REDIRECT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_001", "로그인 리다이렉트가 실패했습니다."),
    OAUTH_LOGIN_GET_TOKEN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_002", "엑세스 토큰을 읽어오는데 실패했습니다."),
    OAUTH_READING_USER_INFO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_003", "유저 정보를 읽어오는데 실패했습니다."),
    OAUTH_LOGIN_ACCESS_TOKEN_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_004", "엑세스 토큰을 읽어오는데 실패했습니다."),
    OAUTH_REFRESH_TOKEN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_005", "토큰 정보를 갱신하는데 실패했습니다."),
    OAUTH_SERVER_ERROR_KAKAO(HttpStatus.BAD_REQUEST, "OAUTH_997", "카카오 서버 장애"),

    RESTAURANT_NO_SUCH_RESTAURANT(HttpStatus.BAD_REQUEST, "REST_001", "해당 음식점이 존재하지 않습니다."),

    PLACE_NO_SUCH_PLACE(HttpStatus.BAD_REQUEST, "PLACE_001", "해당 플레이스가 존재하지 않습니다."),

    REVIEW_NO_SUCH_REVIEW(HttpStatus.BAD_REQUEST, "REVIEW_001", "해당 리뷰가 존재하지 않습니다."),

    CRAWLING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CRAW_001", "크롤링 실패"),

    UNAUTHORIZED_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER_001", "허가받지 않은 사용자입니다."),

    CHATROOM_NO_SUCH_CHATROOM(HttpStatus.BAD_REQUEST, "CHAT_001", "해당 채팅방이 존재하지 않습니다.")
    ;

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
