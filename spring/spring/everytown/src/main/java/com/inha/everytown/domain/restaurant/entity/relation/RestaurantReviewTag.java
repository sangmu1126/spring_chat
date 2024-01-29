package com.inha.everytown.domain.restaurant.entity.relation;

import com.inha.everytown.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "restaurant_review_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantReviewTag extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "bigint")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_review_id")
    private RestaurantReview restaurantReview;

    @Builder
    public RestaurantReviewTag(String tag, Restaurant restaurant, RestaurantReview restaurantReview) {
        this.tag = tag;
        this.restaurant = restaurant;
        this.restaurantReview = restaurantReview;
    }
}
