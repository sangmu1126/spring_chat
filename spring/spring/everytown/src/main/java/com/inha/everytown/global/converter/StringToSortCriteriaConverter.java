package com.inha.everytown.global.converter;

import com.inha.everytown.global.entity.Criteria;
import org.springframework.core.convert.converter.Converter;

public class StringToSortCriteriaConverter implements Converter<String, Criteria> {
    @Override
    public Criteria convert(String source) {
        return Criteria.valueOf(source.toUpperCase());
    }
}
