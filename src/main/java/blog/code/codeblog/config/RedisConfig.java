package blog.code.codeblog.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // tighten polymorphic validator — do NOT allow Object.class globally
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                // only allow your application's base package(s) if you need polymorphism
                .allowIfSubType("blog.code.codeblog")
                // if you really need some JDK types:
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .build();

        // If you can avoid enabling default typing, comment out the next line.
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        log.info("[REDIS CONFIG] ObjectMapper for Redis configured with default typing and JavaTimeModule. Class: {}", mapper.getClass().getName());

        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        log.info("[REDIS CONFIG] RedisTemplate will use serializer: {} and ObjectMapper: {}", serializer.getClass().getName(), redisObjectMapper.getClass().getName());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper
    ) {
        // default serializer for caches (keeps your existing behavior)
        GenericJackson2JsonRedisSerializer genericSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        log.info("[REDIS CONFIG] RedisCacheManager will use serializer: {} and ObjectMapper: {}", genericSerializer.getClass().getName(), redisObjectMapper.getClass().getName());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(genericSerializer)
                );

        // create explicit per-cache config for 'users' which stores a concrete DTO type
        // use Jackson2JsonRedisSerializer<UserResponseDTO> so no '@class' is required
        Jackson2JsonRedisSerializer<blog.code.codeblog.dto.user.UserResponseDTO> usersSerializer =
                new Jackson2JsonRedisSerializer<>(blog.code.codeblog.dto.user.UserResponseDTO.class);
        usersSerializer.setObjectMapper(redisObjectMapper);

        RedisCacheConfiguration usersConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(usersSerializer));

        Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();
        initialCacheConfigurations.put("posts", defaultConfig);
        initialCacheConfigurations.put("users", usersConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(initialCacheConfigurations)
                .build();
    }

    // ... errorHandler() unchanged ...
}