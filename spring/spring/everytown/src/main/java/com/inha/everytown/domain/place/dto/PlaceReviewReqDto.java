package com.inha.everytown.domain.place.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PlaceReviewReqDto {

    private Integer rating;
    private String content;
    private List<String> tag;

    @Builder
    public PlaceReviewReqDto(Integer rating, String content, List<String> tag) {
        this.rating = rating;
        this.content = content;
        this.tag = tag;
    }
}
