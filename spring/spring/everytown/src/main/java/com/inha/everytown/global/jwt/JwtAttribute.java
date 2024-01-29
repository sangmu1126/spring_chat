package com.inha.everytown.global.jwt;

public interface JwtAttribute {
    String HEADER = "Authorization";
    String GRANT_TYPE = "Bearer";
    Long ACCESS_TOKEN_VALID_TIME = 1000 * 60 * 30L;
    Long REFRESH_TOKEN_VALID_TIME = 1000 * 60 * 60 * 24 * 7L;
}
