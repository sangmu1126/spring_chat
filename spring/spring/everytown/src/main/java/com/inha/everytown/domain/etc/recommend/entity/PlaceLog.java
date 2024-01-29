package com.inha.everytown.domain.etc.recommend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PlaceLog {
    private Map<String, ItemLog> itemLog = new HashMap<>();
}
