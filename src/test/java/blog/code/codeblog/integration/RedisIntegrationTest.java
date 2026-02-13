//package blog.code.codeblog.integration;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class RedisIntegrationTest {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private RedisCacheManager redisCacheManager;
//
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;
//
//    @Test
//    @DisplayName("Should store and retrieve a value in Redis")
//    void shouldStoreAndRetrieveValue() {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//        String key = "integration:test:key";
//        String value = "integration-value";
//        ops.set(key, value);
//        Object retrieved = ops.get(key);
//        assertEquals(value, retrieved);
//    }
//
//    @Test
//    @DisplayName("Should overwrite existing value in Redis")
//    void shouldOverwriteExistingValue() {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//        String key = "integration:test:overwrite";
//        ops.set(key, "first");
//        ops.set(key, "second");
//        assertEquals("second", ops.get(key));
//    }
//
//    @Test
//    @DisplayName("Should delete a value from Redis")
//    void shouldDeleteValue() {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//        String key = "integration:test:delete";
//        ops.set(key, "to-be-deleted");
//        redisTemplate.delete(key);
//        assertNull(ops.get(key));
//    }
//
//    @Test
//    @DisplayName("Should store and retrieve null values correctly")
//    void shouldHandleNullValues() {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//        String key = "integration:test:null";
//        redisTemplate.delete(key);
//        assertNull(ops.get(key));
//    }
//
//    @Test
//    @DisplayName("Should use cache manager to cache and retrieve values")
//    void shouldCacheAndRetrieveValues() {
//        var cache = redisCacheManager.getCache("integration-cache");
//        assertNotNull(cache);
//        String key = "cache-key";
//        String value = "cache-value";
//        cache.put(key, value);
//        assertEquals(value, cache.get(key, String.class));
//    }
//
//    @Test
//    @DisplayName("Should connect to Redis server")
//    void shouldConnectToRedisServer() {
//        assertNotNull(redisConnectionFactory.getConnection());
//        assertFalse(redisConnectionFactory.getConnection().isClosed());
//    }
//
//    @Test
//    @DisplayName("Should expire key after TTL")
//    void shouldExpireKeyAfterTtl() throws InterruptedException {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//        String key = "integration:test:ttl";
//        String value = "ttl-value";
//        redisTemplate.opsForValue().set(key, value, java.time.Duration.ofSeconds(1));
//        assertEquals(value, ops.get(key));
//        Thread.sleep(1500);
//        assertNull(ops.get(key), "Key should be expired and not retrievable");
//    }
//}
