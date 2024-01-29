package com.inha.everytown.domain.restaurant.service.relation;

import com.inha.everytown.domain.restaurant.dto.RestaurantBasicInfoDto;
import com.inha.everytown.domain.restaurant.entity.relation.Restaurant;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantMenuRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantRepository;
import com.inha.everytown.domain.etc.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public void updateImage(Long restaurantId, String image) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        restaurant.updateImage(image);
    }

    public boolean isExistImage(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        if(restaurant.getImage() == null || restaurant.getImage().isEmpty()) return false;
        return true;
    }

    public String getImage(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        return restaurant.getImage();
    }
}
