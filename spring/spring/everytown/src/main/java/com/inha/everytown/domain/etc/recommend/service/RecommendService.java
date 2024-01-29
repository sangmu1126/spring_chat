package com.inha.everytown.domain.etc.recommend.service;

import com.inha.everytown.domain.etc.recommend.repository.MemberPreferLogRepository;
import com.inha.everytown.domain.place.dto.PlaceBasicInfoDto;
import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import com.inha.everytown.domain.place.repository.document.PlaceDocRepository;
import com.inha.everytown.domain.restaurant.dto.RestaurantBasicInfoDto;
import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import com.inha.everytown.domain.restaurant.repository.document.RestaurantDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final MemberPreferLogRepository memberPreferLogRepository;
    private final RestaurantDocRepository restaurantDocRepository;
    private final PlaceDocRepository placeDocRepository;

    public List<RestaurantBasicInfoDto> getRestaurantRecommend(Long memberId, double lat, double lon) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("http://localhost:5000")
                .path("/restaurant")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");


        memberPreferLogRepository.saveClickLog(memberId, 9657L, null);
        memberPreferLogRepository.saveClickLog(memberId, 14576L, null);
        memberPreferLogRepository.saveClickLog(memberId, 17604L, null);
        List<String> itemLog = memberPreferLogRepository.getItemList(memberId, null).stream().map(itemId -> itemId.toString()).collect(Collectors.toList());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll("id_list", itemLog);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Long[]> response = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                request,
                Long[].class
        );

        List<RestaurantBasicInfoDto> recommendList = new ArrayList<>();
        for (Long id : response.getBody()) {
            RestaurantDoc restaurantDoc = restaurantDocRepository.findById(id).get();
            restaurantDoc.setDistance(lat, lon);
            recommendList.add(RestaurantBasicInfoDto.DocToDto(restaurantDoc));
        }
        return recommendList;
    }

    public List<PlaceBasicInfoDto> getPlaceRecommend(Long memberId, double lat, double lon, String middle_cate) {
        RestTemplate rt = new RestTemplate();

        UriComponents url = UriComponentsBuilder
                .fromUriString("http://localhost:5000")
                .path("/place")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("cate", middle_cate)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        List<String> itemLog = memberPreferLogRepository.getItemList(memberId, middle_cate).stream().map(itemId -> itemId.toString()).collect(Collectors.toList());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll("id_list", itemLog);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Long[]> response = rt.exchange(
                url.toUriString(),
                HttpMethod.POST,
                request,
                Long[].class
        );

        List<PlaceBasicInfoDto> recommendList = new ArrayList<>();
        for (Long id : response.getBody()) {
            PlaceDoc placeDoc = placeDocRepository.findById(id).get();
            placeDoc.setDistance(lat, lon);
            recommendList.add(PlaceBasicInfoDto.DocToDto(placeDoc));
        }
        return recommendList;
    }
}
