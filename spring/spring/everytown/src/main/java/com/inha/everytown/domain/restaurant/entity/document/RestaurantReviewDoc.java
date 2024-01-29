package com.inha.everytown.domain.restaurant.entity.document;

import lombok.Getter;

@Getter
public class RestaurantReviewDoc {

    private Long id;
    private Integer rating;
    private String content;
    private String nickname;
}
