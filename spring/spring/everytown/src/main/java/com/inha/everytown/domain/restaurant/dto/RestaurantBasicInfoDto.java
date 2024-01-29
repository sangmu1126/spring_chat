package com.inha.everytown.domain.restaurant.dto;

import com.inha.everytown.domain.restaurant.entity.document.RestaurantDoc;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantBasicInfoDto {

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


    @Builder
    public RestaurantBasicInfoDto(Long id, String name, String categoryLargeName, String categoryMiddleName, String categorySmallName, List<String> tag, String dong, String address, double latitude, double longitude, Double rating, Integer distance, Integer reviewCnt, String image) {
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
        this.image = image;
    }

    public static RestaurantBasicInfoDto DocToDto(RestaurantDoc restaurantDoc) {
        return RestaurantBasicInfoDto.builder()
                .id(restaurantDoc.getId())
                .name(restaurantDoc.getName())
                .categoryLargeName(restaurantDoc.getCategoryLargeName())
                .categoryMiddleName(restaurantDoc.getCategoryMiddleName())
                .categorySmallName(restaurantDoc.getCategorySmallName())
                // 임의로 넣은 " default"를 제거하고 공백 문자를 기준으로 리스트 만듬 -> " "를 넣은 이유는 태그가 없을 때 빈 배열 만들기 위함
                .tag(Arrays.asList(restaurantDoc.getTag().replace(" default", " ").split(" ")))
                .dong(restaurantDoc.getDong())
                .address(restaurantDoc.getAddress())
                .latitude(restaurantDoc.getLocation().getLat())
                .longitude(restaurantDoc.getLocation().getLon())
                .rating(restaurantDoc.getRating())
                .distance(restaurantDoc.getDistance())
                .reviewCnt(restaurantDoc.getReviewCnt())
                .image(restaurantDoc.getImage())
                .build();
    }
}
