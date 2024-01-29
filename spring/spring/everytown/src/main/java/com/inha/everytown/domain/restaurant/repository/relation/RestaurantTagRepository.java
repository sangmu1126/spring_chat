package com.inha.everytown.domain.restaurant.repository.relation;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTagRepository extends JpaRepository<RestaurantTag, Long> {

    void deleteByRestaurant_Id(Long restaurantId);
}
