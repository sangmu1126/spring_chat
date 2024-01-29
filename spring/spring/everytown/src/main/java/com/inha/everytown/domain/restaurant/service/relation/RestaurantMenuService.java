package com.inha.everytown.domain.restaurant.service.relation;

import com.inha.everytown.domain.restaurant.dto.RestaurantMenuDto;
import com.inha.everytown.domain.restaurant.entity.relation.Restaurant;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantMenu;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantMenuRepository;
import com.inha.everytown.domain.restaurant.repository.relation.RestaurantRepository;
import com.inha.everytown.global.exception.CustomException;
import com.inha.everytown.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantMenuService {

    private final RestaurantMenuRepository restaurantMenuRepository;
    private final RestaurantRepository restaurantRepository;

    public boolean isMenuExist(Long restaurantId) {
        return restaurantMenuRepository.existsByRestaurant_Id(restaurantId);
    }

    public boolean isNeedCrawling(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );
        // 크롤링을 했나 안했나가 저장되므로 not 을 씌워준다
        return !restaurant.getCrawling();
    }

    public List<RestaurantMenuDto> getMenuDtoList(Long restaurantId) {
        return restaurantMenuRepository.findByRestaurant_Id(restaurantId).stream().map(RestaurantMenuDto::EntityToDto).collect(Collectors.toList());
    }

    public List<RestaurantMenuDto> saveMenuData(Long restaurantId, List<RestaurantMenuDto> menuData) {

        // 크롤링으로 받아온 데이터를 저장
        // elastic과의 동기화를 위해 Restaurant의 modified_at update 필요
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new CustomException(ErrorCode.RESTAURANT_NO_SUCH_RESTAURANT)
        );

        List<RestaurantMenu> saveMenuList = new ArrayList<>();
        for (RestaurantMenuDto menu : menuData) {
            RestaurantMenu restaurantMenu = RestaurantMenu.builder()
                    .name(menu.getName())
                    .price(menu.getPrice())
                    .restaurant(restaurant)
                    .build();
            saveMenuList.add(restaurantMenu);
        }
        List<RestaurantMenu> responseMenuList = restaurantMenuRepository.saveAll(saveMenuList);
        restaurant.updateCrawling();
        return responseMenuList.stream().map(RestaurantMenuDto::EntityToDto).collect(Collectors.toList());
    }
}
