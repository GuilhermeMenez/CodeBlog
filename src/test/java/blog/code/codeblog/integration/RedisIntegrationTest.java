package blog.code.codeblog.integration;

import blog.code.codeblog.dto.post.PostAuthorDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;


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
        redisTemplate.delete(key);
        assertNull(ops.get(key));
    }

    @Test
    @DisplayName("Should store and retrieve null values correctly")
    void shouldHandleNullValues() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = "integration:test:null";
        redisTemplate.delete(key);
        assertNull(ops.get(key));
    }

    @Test
    @DisplayName("Should use cache manager to cache and retrieve values")
    void shouldCacheAndRetrieveValues() {
        var cache = redisCacheManager.getCache("integration-cache");
        assertNotNull(cache);
        String key = "cache-key";
        String value = "cache-value";
        cache.put(key, value);
        assertEquals(value, cache.get(key, String.class));
    }

    @Test
    @DisplayName("Should connect to Redis server")
    void shouldConnectToRedisServer() {
        assertNotNull(redisConnectionFactory.getConnection());
        assertFalse(redisConnectionFactory.getConnection().isClosed());
    }

    @Test
    @DisplayName("Should expire key after TTL")
    void shouldExpireKeyAfterTtl() throws InterruptedException {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = "integration:test:ttl";
        String value = "ttl-value";
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(1));
        assertEquals(value, ops.get(key));
        Thread.sleep(1500);
        assertNull(ops.get(key), "Key should be expired and not retrievable");
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
        redisTemplate.opsForValue().set("integration:test:user", dto);
        Object retrieved = redisTemplate.opsForValue().get("integration:test:user");
        assertNotNull(retrieved);
        assertInstanceOf(UserResponseDTO.class, retrieved);
        UserResponseDTO result = (UserResponseDTO) retrieved;
        assertEquals(dto.id(), result.id());
        assertEquals(dto.name(), result.name());
        assertEquals(dto.login(), result.login());
        assertEquals(dto.urlProfilePic(), result.urlProfilePic());
        assertEquals(dto.followersCount(), result.followersCount());
        assertEquals(dto.followingCount(), result.followingCount());
        redisTemplate.delete("integration:test:user");
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
        redisTemplate.opsForValue().set("integration:test:post", dto);
        Object retrieved = redisTemplate.opsForValue().get("integration:test:post");
        assertNotNull(retrieved);
        assertInstanceOf(PostResponseDTO.class, retrieved);
        PostResponseDTO result = (PostResponseDTO) retrieved;
        assertEquals(dto.postId(), result.postId());
        assertEquals(dto.title(), result.title());
        assertEquals(dto.content(), result.content());
        assertEquals(dto.author().getId(), result.author().getId());
        assertEquals(dto.author().getName(), result.author().getName());
        assertEquals(dto.createdAt(), result.createdAt());
        assertEquals(dto.images(), result.images());
        redisTemplate.delete("integration:test:post");
    }

    @Test
    @DisplayName("Should serialize and deserialize List<PostResponseDTO> in Redis")
    void shouldSerializeAndDeserializeListOfPostResponseDTO() {

        PostAuthorDTO author1 = new PostAuthorDTO(UUID.randomUUID(), "Author1");
        PostAuthorDTO author2 = new PostAuthorDTO(UUID.randomUUID(), "Author2");

        PostResponseDTO dto1 = new PostResponseDTO(
                UUID.randomUUID(),
                "PostTitle1",
                "PostContent1",
                author1,
                LocalDate.now(),
                Map.of("img1", "url1")
        );

        PostResponseDTO dto2 = new PostResponseDTO(
                UUID.randomUUID(),
                "PostTitle2",
                "PostContent2",
                author2,
                LocalDate.now(),
                Map.of("img2", "url2")
        );

        List<PostResponseDTO> list = new ArrayList<>(Arrays.asList(dto1, dto2));

        redisTemplate.opsForValue().set("integration:test:postlist", list);

        Object rawRetrieved = redisTemplate.opsForValue().get("integration:test:postlist");
        assertNotNull(rawRetrieved);

        @SuppressWarnings("unchecked")
        List<PostResponseDTO> resultList = (List<PostResponseDTO>) rawRetrieved;

        assertEquals(2, resultList.size());

        PostResponseDTO result1 = resultList.get(0);
        PostResponseDTO result2 = resultList.get(1);

        assertEquals(dto1.postId(), result1.postId());
        assertEquals(dto1.title(), result1.title());
        assertEquals(dto1.content(), result1.content());
        assertEquals(dto1.author().getId(), result1.author().getId());
        assertEquals(dto1.author().getName(), result1.author().getName());

        assertEquals(dto2.postId(), result2.postId());
        assertEquals(dto2.title(), result2.title());
        assertEquals(dto2.content(), result2.content());
        assertEquals(dto2.author().getId(), result2.author().getId());
        assertEquals(dto2.author().getName(), result2.author().getName());

        redisTemplate.delete("integration:test:postlist");
    }

    @Test
    @DisplayName("Should serialize and deserialize List<UserResponseDTO> in Redis")
    void shouldSerializeAndDeserializeListOfUserResponseDTO() {

        UserResponseDTO dto1 = new UserResponseDTO(
                UUID.randomUUID(),
                "User1",
                "user1@test.com",
                "http://profile1.pic",
                100,
                50
        );

        UserResponseDTO dto2 = new UserResponseDTO(
                UUID.randomUUID(),
                "User2",
                "user2@test.com",
                "http://profile2.pic",
                200,
                150
        );

        List<UserResponseDTO> list = new ArrayList<>(Arrays.asList(dto1, dto2));

        redisTemplate.opsForValue().set("integration:test:userlist", list);

        Object rawRetrieved = redisTemplate.opsForValue().get("integration:test:userlist");
        assertNotNull(rawRetrieved);

        @SuppressWarnings("unchecked")
        List<UserResponseDTO> resultList = (List<UserResponseDTO>) rawRetrieved;

        assertEquals(2, resultList.size());

        UserResponseDTO result1 = resultList.get(0);
        UserResponseDTO result2 = resultList.get(1);

        assertEquals(dto1.id(), result1.id());
        assertEquals(dto1.name(), result1.name());
        assertEquals(dto1.login(), result1.login());
        assertEquals(dto1.urlProfilePic(), result1.urlProfilePic());
        assertEquals(dto1.followersCount(), result1.followersCount());
        assertEquals(dto1.followingCount(), result1.followingCount());

        assertEquals(dto2.id(), result2.id());
        assertEquals(dto2.name(), result2.name());
        assertEquals(dto2.login(), result2.login());
        assertEquals(dto2.urlProfilePic(), result2.urlProfilePic());
        assertEquals(dto2.followersCount(), result2.followersCount());
        assertEquals(dto2.followingCount(), result2.followingCount());

        redisTemplate.delete("integration:test:userlist");
    }

}