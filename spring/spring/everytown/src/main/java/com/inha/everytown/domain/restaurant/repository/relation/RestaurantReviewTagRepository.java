package com.inha.everytown.domain.restaurant.repository.relation;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantReviewTagRepository extends JpaRepository<RestaurantReviewTag, Long> {

    List<RestaurantReviewTag> findByRestaurantReview_Id(Long restaurantReviewId);

    List<RestaurantReviewTag> findByRestaurant_Id(Long restaurantId);

    void deleteByRestaurantReview_Id(Long restaurantReviewId);
}
