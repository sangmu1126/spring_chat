package com.inha.everytown.global.converter;

import com.inha.everytown.domain.member.entity.Platform;
import org.springframework.core.convert.converter.Converter;

public class StringToPlatformConverter implements Converter<String, Platform> {

    @Override
    public Platform convert(String source) {
        return Platform.valueOf(source.toUpperCase());
    }
}
