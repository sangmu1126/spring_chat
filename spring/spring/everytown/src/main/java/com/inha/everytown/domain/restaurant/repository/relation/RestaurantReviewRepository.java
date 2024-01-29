package com.inha.everytown.domain.restaurant.repository.relation;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

    List<RestaurantReview> findByRestaurant_Id(Long restaurantId);

    List<RestaurantReview> findByMember_Id(Long memberId);

    boolean existsByRestaurant_Id(Long restaurantId);
}
