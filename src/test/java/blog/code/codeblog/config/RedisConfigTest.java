//package blog.code.codeblog.config;
//
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import org.springframework.data.redis.serializer.GenericToStringSerializer;
//
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.Test;
//
//@ExtendWith(MockitoExtension.class)
//class RedisConfigTest {
//    @Mock
//    private RedisConnectionFactory redisConnectionFactory;
//
//    @Test
//    @DisplayName("redisTemplate returns template with correct serializers and connection factory")
//    void redisTemplateReturnsTemplateWithCorrectSerializers() {
//        CacheConfig config = new CacheConfig();
//        RedisTemplate<String, Object> template = config.redisTemplate(redisConnectionFactory);
//        assertNotNull(template);
//        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
//        assertInstanceOf(GenericToStringSerializer.class, template.getValueSerializer());
//        assertEquals(redisConnectionFactory, template.getConnectionFactory());
//    }
//
//    @Test
//    @DisplayName("cacheManager returns non-null RedisCacheManager with correct configuration")
//    void cacheManagerReturnsNonNullWithCorrectConfig() {
//        CacheConfig config = new CacheConfig();
//        RedisCacheManager manager = config.cacheManager(redisConnectionFactory);
//        assertNotNull(manager);
//    }
//
//    @Test
//    @DisplayName("redisConnectionFactory returns LettuceConnectionFactory with correct properties")
//    void redisConnectionFactoryReturnsLettuceConnectionFactoryWithCorrectProperties() {
//        CacheConfig config = new CacheConfig();
//        // Use reflection to set private fields
//        setField(config, "redisHost", "localhost");
//        setField(config, "redisPort", 6379);
//        setField(config, "redisPassword", "");
//        setField(config, "redisUsername", "default");
//        RedisConnectionFactory factory = config.redisConnectionFactory();
//        assertNotNull(factory);
//        assertInstanceOf(LettuceConnectionFactory.class, factory);
//        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) factory;
//        assertEquals("localhost", lettuceFactory.getHostName());
//        assertEquals(6379, lettuceFactory.getPort());
//    }
//
//    private static void setField(Object target, String fieldName, Object value) {
//        try {
//            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            field.set(target, value);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
