package com.inha.everytown.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate redisTemplate;

    // <Key : Value> 를 TTL 둬서 저장
    public void setValue(String key, Object value, long timeout, TimeUnit timeUnit) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value, timeout, timeUnit);
    }

    public Object getValue(String key) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);    // If not exist, then return null
        return value;
    }

    public void deleteValue(String key) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.getAndDelete(key);
    }
}
