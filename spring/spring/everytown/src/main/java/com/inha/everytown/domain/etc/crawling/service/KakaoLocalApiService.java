package com.inha.everytown.domain.etc.crawling.service;

import com.inha.everytown.domain.etc.crawling.dto.response.KakaoLocalApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class KakaoLocalApiService {

    private final String REST_API_KEY;

    public KakaoLocalApiService(@Value("${api.kakao.key}") String key) {
        this.REST_API_KEY = key;
    }

    public Long getKakaoId(String name, double lat, double lon) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com")
                .path("/v2/local/search/keyword.json")
                .queryParam("query", name)
                .queryParam("x", lon)
                .queryParam("y", lat)
                .queryParam("radius", "200")
                .queryParam("size", "5")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + REST_API_KEY);

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(headers);

        ResponseEntity<KakaoLocalApiResponseDto> res = rt.exchange(
                url.toUriString(),
                HttpMethod.GET,
                req,
                KakaoLocalApiResponseDto.class
        );

        KakaoLocalApiResponseDto result = res.getBody();
        if(result == null || result.getDocuments() == null || result.getDocuments().isEmpty()) {
            log.info("Kakao Local API > NO ITEM");
            return 0L;
        }
        return result.getDocuments().get(0).getId();
    }
}