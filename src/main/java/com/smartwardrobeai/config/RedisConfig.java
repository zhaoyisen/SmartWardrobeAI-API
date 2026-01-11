package com.smartwardrobeai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 创建配置了 Java 8 时间类型支持的 ObjectMapper
     * 不启用默认类型信息，避免添加包装层（如 ["java.util.ArrayList", [实际数据]]）
     * 
     * @return 配置好的 ObjectMapper
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 LocalDateTime 等 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁用将日期写入为时间戳，使用 ISO-8601 字符串格式（更适合 Redis 可读性）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 不启用默认类型信息，使用简洁的 JSON 格式
        // 反序列化时需要在服务层显式指定类型
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用 Jackson2JsonRedisSerializer 而不是 GenericJackson2JsonRedisSerializer
        // Jackson2JsonRedisSerializer 不会添加类型信息包装层
        // 使用 Object.class 作为默认类型，实际类型在反序列化时通过服务层指定
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = 
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        // Key 使用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        // Value 使用 JSON 序列化 (支持 LocalDateTime 等 Java 8 时间类型，不添加类型包装)
        template.setValueSerializer(jsonRedisSerializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}