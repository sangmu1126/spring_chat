package com.inha.everytown.global.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class StringToGeoPointConverter implements Converter<String, GeoPoint> {

    @Override
    public GeoPoint convert(String source) {
        String[] parts = source.split(",");
        if (parts.length == 2) {
            return new GeoPoint(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
        }
        return null;
    }
}
