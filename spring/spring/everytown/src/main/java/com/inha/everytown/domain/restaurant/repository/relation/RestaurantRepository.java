package com.inha.everytown.domain.restaurant.repository.relation;

import com.inha.everytown.domain.restaurant.entity.relation.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

}
