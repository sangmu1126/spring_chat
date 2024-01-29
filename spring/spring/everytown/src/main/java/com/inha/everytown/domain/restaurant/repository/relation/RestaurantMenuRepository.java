package com.inha.everytown.domain.restaurant.repository.relation;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantMenuRepository extends JpaRepository<RestaurantMenu, Long> {

    boolean existsByRestaurant_Id(Long id);

    List<RestaurantMenu> findByRestaurant_Id(Long restaurantId);
}
