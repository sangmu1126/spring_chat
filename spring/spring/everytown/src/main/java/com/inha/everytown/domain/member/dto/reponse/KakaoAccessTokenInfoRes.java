package com.inha.everytown.domain.member.dto.reponse;

import lombok.Getter;

@Getter
public class KakaoAccessTokenInfoRes {
    private Long id;
    private Integer expires_in;
    private Integer app_id;
}
