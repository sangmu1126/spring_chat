package com.inha.everytown.domain.restaurant.dto;

import com.inha.everytown.domain.restaurant.entity.document.RestaurantMenuDoc;
import com.inha.everytown.domain.restaurant.entity.relation.RestaurantMenu;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantMenuDto {

    private Long id;
    private String name;
    private Integer price;

    @Builder
    public RestaurantMenuDto(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static RestaurantMenuDto DocToDto(RestaurantMenuDoc restaurantMenuDoc) {
        return RestaurantMenuDto.builder()
                .id(restaurantMenuDoc.getId())
                .name(restaurantMenuDoc.getName())
                .price(restaurantMenuDoc.getPrice())
                .build();
    }

    public static RestaurantMenuDto EntityToDto(RestaurantMenu restaurantMenu) {
        return RestaurantMenuDto.builder()
                .id(restaurantMenu.getId())
                .name(restaurantMenu.getName())
                .price(restaurantMenu.getPrice())
                .build();
    }
}
