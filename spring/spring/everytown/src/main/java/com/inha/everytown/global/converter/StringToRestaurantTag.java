package com.inha.everytown.global.converter;

import com.inha.everytown.domain.restaurant.entity.relation.RestaurantTagEnum;
import org.springframework.core.convert.converter.Converter;

public class StringToRestaurantTag implements Converter<String, RestaurantTagEnum> {

    @Override
    public RestaurantTagEnum convert(String source) {
        return RestaurantTagEnum.valueOf(source.toUpperCase());
    }
}
