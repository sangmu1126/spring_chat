package com.inha.everytown.global.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key는 String으로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value에 대한 역직렬화를 설정 하는 과정
        // 객체 <-> Json (역)직렬화 하는 serializer
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // PropertyAccessor.ALL : 객체의 모든 필드를 대상으로 함
        // JsonAutoDetect.Visibility.ANY : 모든 종류의 접근자(public, private, protected 등)에 대해 수행을 하도록 함
        // cf. 원래는 public이나 getter, setter가 있는 필드에 대해서 수행함
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // activateDefaultTyping : Json에 실제 타입 정보를 포함시킴
        // LaissezFaireSubTypeValidator.instance : 서브타입 검사기. 기본적으로 안전한 서브타입만 역직렬화를 수행
        // ObjectMapper.DefaultTyping.NON_FINAL : non-final 클래스에 대해서만 유형 정보를 포함. 기본적으로 final 클래스는 유형 정보 없어도 직렬화 가능
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        return redisTemplate;
    }
}
