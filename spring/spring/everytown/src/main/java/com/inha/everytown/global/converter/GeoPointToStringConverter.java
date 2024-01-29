package com.inha.everytown.global.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

@Component
public class GeoPointToStringConverter implements Converter<GeoPoint, String> {

    @Override
    public String convert(GeoPoint source) {
        return source.getLat() + "," + source.getLon();
    }
}
