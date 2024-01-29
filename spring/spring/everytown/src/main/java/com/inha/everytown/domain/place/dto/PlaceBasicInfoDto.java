package com.inha.everytown.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inha.everytown.domain.place.entity.document.PlaceDoc;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceBasicInfoDto {

    private Long id;

    private String name;

    private String categoryLargeName;

    private String categoryMiddleName;

    private String categorySmallName;

    private List<String> tag;

    private String dong;

    private String address;

    private double latitude;

    private double longitude;

    private Double rating;

    private Integer distance;

    private Integer reviewCnt;

    private String image;

    @JsonIgnore
    private String categoryMiddleCode;    // Jackson 직렬화 제외 -> 추천 시스템 위한 로그 저장에 사용

    @Builder
    public PlaceBasicInfoDto(Long id, String name, String categoryLargeName, String categoryMiddleName, String categorySmallName, List<String> tag, String dong, String address, double latitude, double longitude, Double rating, Integer distance, Integer reviewCnt, String categoryMiddleCode, String image) {
        this.id = id;
        this.name = name;
        this.categoryLargeName = categoryLargeName;
        this.categoryMiddleName = categoryMiddleName;
        this.categorySmallName = categorySmallName;
        this.tag = tag;
        this.dong = dong;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.distance = distance;
        this.reviewCnt = reviewCnt;
        this.categoryMiddleCode = categoryMiddleCode;
        this.image = image;
    }

    public static PlaceBasicInfoDto DocToDto(PlaceDoc placeDoc) {
        return PlaceBasicInfoDto.builder()
                .id(placeDoc.getId())
                .name(placeDoc.getName())
                .categoryLargeName(placeDoc.getCategoryLargeName())
                .categoryMiddleName(placeDoc.getCategoryMiddleName())
                .categorySmallName(placeDoc.getCategorySmallName())
                .tag(Arrays.asList(placeDoc.getTag().replace(" default", " ").split(" ")))
                .dong(placeDoc.getDong())
                .address(placeDoc.getAddress())
                .latitude(placeDoc.getLocation().getLat())
                .longitude(placeDoc.getLocation().getLon())
                .rating(placeDoc.getRating())
                .distance(placeDoc.getDistance())
                .reviewCnt(placeDoc.getReviewCnt())
                .categoryMiddleCode(placeDoc.getCategoryMiddleCode())
                .image(placeDoc.getImage())
                .build();
    }
}
