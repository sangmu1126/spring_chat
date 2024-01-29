package com.inha.everytown.global.config.converter;

import com.inha.everytown.global.converter.StringToPlatformConverter;
import com.inha.everytown.global.converter.StringToRestaurantTag;
import com.inha.everytown.global.converter.StringToSortCriteriaConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToPlatformConverter());
        registry.addConverter(new StringToSortCriteriaConverter());
        registry.addConverter(new StringToRestaurantTag());
    }
}
