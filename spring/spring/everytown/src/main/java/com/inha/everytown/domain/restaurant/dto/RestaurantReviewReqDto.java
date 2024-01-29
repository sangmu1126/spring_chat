package com.inha.everytown.domain.restaurant.dto;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantTagEnum;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class RestaurantReviewReqDto {

    private Integer rating;
    private String content;
    private List<String> tag;

    @Builder
    public RestaurantReviewReqDto(Integer rating, String content, List<String> tag) {
        this.rating = rating;
        this.content = content;
        this.tag = tag;
    }
}
