package com.inha.everytown.domain.restaurant.entity.relation;

import com.inha.everytown.domain.member.entity.Member;
import com.inha.everytown.domain.restaurant.dto.RestaurantReviewReqDto;
import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantReview extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "nickname")
    private String nickname;

    @Builder
    public RestaurantReview(Integer rating, String content, Restaurant restaurant, Member member, String nickname) {
        this.rating = rating;
        this.content = content;
        this.restaurant = restaurant;
        this.member = member;
        this.nickname = nickname;
    }

    public void updateReview(RestaurantReviewReqDto reviewReqDto) {
        this.rating = reviewReqDto.getRating();
        this.content = reviewReqDto.getContent();
    }
}
