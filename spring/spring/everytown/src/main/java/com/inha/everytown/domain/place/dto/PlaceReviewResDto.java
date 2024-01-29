package com.inha.everytown.domain.place.dto;


import com.inha.everytown.domain.place.entity.relation.PlaceReview;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReviewResDto {

    private Long id;
    private Integer rating;
    private String content;
    private String nickname;
    private List<String> tag;
    private LocalDateTime createdAt;

    @Builder
    public PlaceReviewResDto(Long id, Integer rating, String content, String nickname, List<String> tag, LocalDateTime createdAt) {
        this.id = id;
        this.rating = rating;
        this.content = content;
        this.nickname = nickname;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public static PlaceReviewResDto EntityToDto(PlaceReview placeReview) {
        return PlaceReviewResDto.builder()
                .id(placeReview.getId())
                .rating(placeReview.getRating())
                .content(placeReview.getContent())
                .nickname(placeReview.getNickname())
                .createdAt(placeReview.getCreatedAt())
                .build();
    }
}
