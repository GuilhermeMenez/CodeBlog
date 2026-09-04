package blog.code.codeblog.integration;

import blog.code.codeblog.config.RedisConfig;
import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PostAuthorDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.integration.support.RedisContainerSupport;
import blog.code.codeblog.mapper.PageMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisIntegrationTest extends RedisContainerSupport {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void clearRedis() {
        clearTestKeys(redisTemplate);
    }

    private Object roundTrip(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        return redisTemplate.opsForValue().get(key);
    }

    @Test
    @DisplayName("Should store and retrieve a value in Redis")
    void shouldStoreAndRetrieveValue() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = "integration:test:key";
        String value = "integration-value";
        ops.set(key, value);
        Object retrieved = ops.get(key);
        assertEquals(value, retrieved);
    }

    @Test
    @DisplayName("Should overwrite existing value in Redis")
    void shouldOverwriteExistingValue() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = "integration:test:overwrite";
        ops.set(key, "first");
        ops.set(key, "second");
        assertEquals("second", ops.get(key));
    }

    @Test
    @DisplayName("Should delete a value from Redis")
    void shouldDeleteValue() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = "integration:test:delete";
        ops.set(key, "to-be-deleted");
        assertTrue(redisTemplate.delete(key));
        assertNull(ops.get(key));
    }

    @Test
    @DisplayName("Should connect to the Redis server and answer PING")
    void shouldConnectToRedisServer() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            assertFalse(connection.isClosed());
            assertEquals("PONG", connection.ping());
        }
    }

    @Test
    @DisplayName("Should apply the configured TTL when writing a key with expiration")
    void shouldApplyTtlOnWrite() {
        String key = "integration:test:ttl";
        redisTemplate.opsForValue().set(key, "ttl-value", Duration.ofSeconds(30));

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 25 && ttl <= 30, "Expected TTL close to 30s but was " + ttl);
    }

    @Test
    @DisplayName("Cache manager should apply the TTL configured for each cache")
    void cacheShouldApplyConfiguredTtl() {
        var cache = redisCacheManager.getCache(RedisConfig.FEED_CACHE);
        assertNotNull(cache);

        cache.put("feed-key", "feed-value");
        assertEquals("feed-value", cache.get("feed-key", String.class));

        Long ttl = redisTemplate.getExpire(RedisConfig.FEED_CACHE + "::feed-key", TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 100 && ttl <= 120, "feed cache should expire in ~2min but TTL was " + ttl);
    }

    @Test
    @DisplayName("Cache manager should reject null values (disableCachingNullValues)")
    void cacheShouldRejectNullValues() {
        var cache = redisCacheManager.getCache(RedisConfig.USER_CACHE);
        assertNotNull(cache);

        assertThrows(IllegalArgumentException.class, () -> cache.put("null-key", null));
        assertNull(cache.get("null-key"));
    }

    @Test
    @DisplayName("Should serialize and deserialize UserResponseDTO in Redis")
    void shouldSerializeAndDeserializeUserResponseDTO() {
        UserResponseDTO dto = new UserResponseDTO(
                UUID.randomUUID(),
                "TestUser",
                "testuser@test.com",
                "http://profile.pic",
                10,
                5
        );

        Object retrieved = roundTrip("integration:test:user", dto);

        assertInstanceOf(UserResponseDTO.class, retrieved);
        assertEquals(dto, retrieved);
    }

    @Test
    @DisplayName("Should serialize and deserialize UserFollowDTO in Redis")
    void shouldSerializeAndDeserializeUserFollowDTO() {
        UserFollowDTO dto = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Follower")
                .login("follower@test.com")
                .urlProfilePic("http://profile.pic")
                .build();

        Object retrieved = roundTrip("integration:test:userfollow", dto);

        assertInstanceOf(UserFollowDTO.class, retrieved);
        assertEquals(dto, retrieved);
    }

    @Test
    @DisplayName("Should serialize and deserialize PostResponseDTO in Redis")
    void shouldSerializeAndDeserializePostResponseDTO() {
        PostAuthorDTO author = new PostAuthorDTO(UUID.randomUUID(), "AuthorName");
        PostResponseDTO dto = new PostResponseDTO(
                UUID.randomUUID(),
                "PostTitle",
                "PostContent",
                author,
                LocalDate.now(),
                Map.of("img1", "url1")
        );

        Object retrieved = roundTrip("integration:test:post", dto);

        assertInstanceOf(PostResponseDTO.class, retrieved);
        PostResponseDTO result = (PostResponseDTO) retrieved;
        assertEquals(dto.postId(), result.postId());
        assertEquals(dto.title(), result.title());
        assertEquals(dto.content(), result.content());
        assertEquals(dto.author(), result.author());
        assertEquals(dto.createdAt(), result.createdAt());
        assertEquals(dto.images(), result.images());
    }

    @Test
    @DisplayName("Should serialize and deserialize List<PostResponseDTO> in Redis")
    void shouldSerializeAndDeserializeListOfPostResponseDTO() {
        PostResponseDTO dto1 = postResponseDTO("PostTitle1", "PostContent1", "Author1", "img1", "url1");
        PostResponseDTO dto2 = postResponseDTO("PostTitle2", "PostContent2", "Author2", "img2", "url2");

        List<PostResponseDTO> list = new ArrayList<>(Arrays.asList(dto1, dto2));

        Object retrieved = roundTrip("integration:test:postlist", list);
        assertNotNull(retrieved);

        @SuppressWarnings("unchecked")
        List<PostResponseDTO> resultList = (List<PostResponseDTO>) retrieved;

        assertEquals(2, resultList.size());
        assertInstanceOf(PostResponseDTO.class, resultList.get(0));
        assertEquals(dto1, resultList.get(0));
        assertEquals(dto2, resultList.get(1));
    }

    @Test
    @DisplayName("Should serialize and deserialize List<UserResponseDTO> in Redis")
    void shouldSerializeAndDeserializeListOfUserResponseDTO() {
        UserResponseDTO dto1 = new UserResponseDTO(
                UUID.randomUUID(), "User1", "user1@test.com", "http://profile1.pic", 100, 50);
        UserResponseDTO dto2 = new UserResponseDTO(
                UUID.randomUUID(), "User2", "user2@test.com", "http://profile2.pic", 200, 150);

        List<UserResponseDTO> list = new ArrayList<>(Arrays.asList(dto1, dto2));

        Object retrieved = roundTrip("integration:test:userlist", list);
        assertNotNull(retrieved);

        @SuppressWarnings("unchecked")
        List<UserResponseDTO> resultList = (List<UserResponseDTO>) retrieved;

        assertEquals(2, resultList.size());
        assertInstanceOf(UserResponseDTO.class, resultList.get(0));
        assertEquals(dto1, resultList.get(0));
        assertEquals(dto2, resultList.get(1));
    }


    @Test
    @DisplayName("Should serialize and deserialize PageResponseDTO<UserFollowDTO> built from a Page")
    void shouldSerializeAndDeserializePageOfUserFollowDTO() {
        UserFollowDTO follower = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Follower")
                .login("follower@test.com")
                .urlProfilePic("http://profile.pic")
                .build();

        PageResponseDTO<UserFollowDTO> page = PageMapper.toPageResponseDTO(
                new PageImpl<>(List.of(follower), PageRequest.of(0, 10), 1),
                dto -> dto
        );

        Object retrieved = roundTrip("integration:test:followerspage", page);

        assertInstanceOf(PageResponseDTO.class, retrieved);

        @SuppressWarnings("unchecked")
        PageResponseDTO<UserFollowDTO> result = (PageResponseDTO<UserFollowDTO>) retrieved;

        assertEquals(1, result.content().size());
        assertInstanceOf(UserFollowDTO.class, result.content().get(0));
        assertEquals(follower, result.content().get(0));
        assertEquals(page.currentPage(), result.currentPage());
        assertEquals(page.totalPages(), result.totalPages());
        assertEquals(page.totalElements(), result.totalElements());
        assertEquals(page.size(), result.size());
        assertEquals(page.first(), result.first());
        assertEquals(page.last(), result.last());
        assertEquals(page.empty(), result.empty());
    }

    @Test
    @DisplayName("Should serialize and deserialize PageResponseDTO<PostResponseDTO> built from a List")
    void shouldSerializeAndDeserializePageOfPostResponseDTO() {
        PostResponseDTO post = postResponseDTO("PostTitle", "PostContent", "Author", "img1", "url1");

        PageResponseDTO<PostResponseDTO> page =
                PageMapper.toPageResponseDTO(List.of(post), 0, 10, 1);

        Object retrieved = roundTrip("integration:test:feedpage", page);

        assertInstanceOf(PageResponseDTO.class, retrieved);

        @SuppressWarnings("unchecked")
        PageResponseDTO<PostResponseDTO> result = (PageResponseDTO<PostResponseDTO>) retrieved;

        assertEquals(1, result.content().size());
        assertInstanceOf(PostResponseDTO.class, result.content().get(0));
        assertEquals(post, result.content().get(0));
        assertEquals(page.totalElements(), result.totalElements());
        assertFalse(result.empty());
    }

    private PostResponseDTO postResponseDTO(String title, String content, String author,
                                            String imageId, String imageUrl) {
        return new PostResponseDTO(
                UUID.randomUUID(),
                title,
                content,
                new PostAuthorDTO(UUID.randomUUID(), author),
                LocalDate.now(),
                Map.of(imageId, imageUrl)
        );
    }
}
