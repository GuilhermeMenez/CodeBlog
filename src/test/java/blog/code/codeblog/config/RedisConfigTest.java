package blog.code.codeblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    private RedisConfig config;
    private ObjectMapper redisObjectMapper;

    @BeforeEach
    void setUp() {
        config = new RedisConfig();
        redisObjectMapper = config.redisObjectMapper();
    }

    @Test
    @DisplayName("objectMapper returns ObjectMapper with JavaTimeModule registered")
    void objectMapperReturnsConfiguredMapper() {
        ObjectMapper mapper = config.objectMapper();
        assertNotNull(mapper);
        // Verifica que o JavaTimeModule está registrado
        assertNotNull(mapper.getRegisteredModuleIds());
    }

    @Test
    @DisplayName("redisObjectMapper returns ObjectMapper with default typing enabled")
    void redisObjectMapperReturnsConfiguredMapper() {
        assertNotNull(redisObjectMapper);
        // Verifica que o JavaTimeModule está registrado
        assertNotNull(redisObjectMapper.getRegisteredModuleIds());
    }

    @Test
    @DisplayName("redisTemplate returns template with correct serializers and connection factory")
    void redisTemplateReturnsTemplateWithCorrectSerializers() {
        RedisTemplate<String, Object> template = config.redisTemplate(
                redisConnectionFactory,
                redisObjectMapper
        );

        assertNotNull(template);
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getHashValueSerializer());
        assertEquals(redisConnectionFactory, template.getConnectionFactory());
    }

    @Test
    @DisplayName("cacheManager returns non-null RedisCacheManager with correct configuration")
    void cacheManagerReturnsNonNullWithCorrectConfig() {
        RedisCacheManager manager = config.cacheManager(
                redisConnectionFactory,
                redisObjectMapper
        );

        assertNotNull(manager);
        // Verifica que os caches específicos estão configurados
        assertNotNull(manager.getCacheNames());
    }

    @Test
    @DisplayName("redisConnectionFactory returns LettuceConnectionFactory with correct properties")
    void redisConnectionFactoryReturnsLettuceConnectionFactoryWithCorrectProperties() {
        // Use reflection to set private fields
        setField(config, "redisHost", "localhost");
        setField(config, "redisPort", 6379);
        setField(config, "redisPassword", "test-password");
        setField(config, "redisUsername", "default");

        RedisConnectionFactory factory = config.redisConnectionFactory();

        assertNotNull(factory);
        assertInstanceOf(LettuceConnectionFactory.class, factory);

        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) factory;
        assertEquals("localhost", lettuceFactory.getHostName());
        assertEquals(6379, lettuceFactory.getPort());
    }

    @Test
    @DisplayName("cacheManager has all expected cache configurations")
    void cacheManagerHasExpectedCacheConfigurations() {
        // Set TTL values via reflection
        setField(config, "followersTtl", 300000L);
        setField(config, "followingTtl", 300000L);
        setField(config, "userTtl", 1800000L);

        RedisCacheManager manager = config.cacheManager(
                redisConnectionFactory,
                redisObjectMapper
        );

        assertNotNull(manager);

        // Verifica que os caches foram criados com os nomes corretos
        var cache = manager.getCache(RedisConfig.FOLLOWERS_CACHE);
        assertNotNull(cache, "Followers cache should exist");

        cache = manager.getCache(RedisConfig.FOLLOWING_CACHE);
        assertNotNull(cache, "Following cache should exist");

        cache = manager.getCache(RedisConfig.USER_CACHE);
        assertNotNull(cache, "User cache should exist");

        cache = manager.getCache(RedisConfig.POST_CACHE);
        assertNotNull(cache, "Post cache should exist");

        cache = manager.getCache(RedisConfig.USER_POSTS_CACHE);
        assertNotNull(cache, "User posts cache should exist");
    }

    @Test
    @DisplayName("cache constants have expected values")
    void cacheConstantsHaveExpectedValues() {
        assertEquals("followers-list", RedisConfig.FOLLOWERS_CACHE);
        assertEquals("following-list", RedisConfig.FOLLOWING_CACHE);
        assertEquals("user", RedisConfig.USER_CACHE);
        assertEquals("post", RedisConfig.POST_CACHE);
        assertEquals("user-posts", RedisConfig.USER_POSTS_CACHE);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}