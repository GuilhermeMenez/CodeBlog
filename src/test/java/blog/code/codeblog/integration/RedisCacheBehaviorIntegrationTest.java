package blog.code.codeblog.integration;

import blog.code.codeblog.command.feed.GetFeedCommand;
import blog.code.codeblog.command.user.FollowCommand;
import blog.code.codeblog.command.user.GetFollowersCommand;
import blog.code.codeblog.command.user.GetFollowingCommand;
import blog.code.codeblog.config.RedisConfig;
import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.integration.support.RedisContainerSupport;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.model.UserFollow;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserFollowRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.FeedService;
import blog.code.codeblog.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class RedisCacheBehaviorIntegrationTest extends RedisContainerSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserFollowRepository userFollowRepository;

    @MockitoBean
    private PostRepository postRepository;

    @BeforeEach
    void clearRedis() {
        clearTestKeys(redisTemplate);
    }

    private User user(String name, String login) {
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .login(login)
                .password("secret")
                .build();
    }

    @Test
    @DisplayName("getFollowers should hit the repository once and cache under the SpEL key")
    void getFollowersShouldBeCached() {
        UUID userId = UUID.randomUUID();
        User follower = user("Follower", "follower@test.com");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowersByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(follower), PageRequest.of(0, 10), 1));

        GetFollowersCommand command = GetFollowersCommand.builder()
                .userId(userId).page(0).size(10).build();

        PageResponseDTO<UserFollowDTO> first = userService.getFollowers(command);
        PageResponseDTO<UserFollowDTO> second = userService.getFollowers(command);

        verify(userFollowRepository, times(1)).findFollowersByUserId(eq(userId), any(Pageable.class));
        assertEquals(first.content(), second.content());
        assertEquals(follower.getId(), second.content().get(0).id());

        String expectedKey = RedisConfig.FOLLOWERS_CACHE + "::" + userId + "_0_10";
        assertTrue(redisTemplate.hasKey(expectedKey),
                "Expected the followers page to be cached under " + expectedKey);
    }

    @Test
    @DisplayName("getFollowing should not cache an empty page (unless = #result.empty)")
    void getFollowingShouldNotCacheEmptyPage() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowingByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        GetFollowingCommand command = GetFollowingCommand.builder()
                .userId(userId).page(0).size(10).build();

        userService.getFollowing(command);
        userService.getFollowing(command);

        verify(userFollowRepository, times(2)).findFollowingByUserId(eq(userId), any(Pageable.class));
        assertFalse(redisTemplate.hasKey(RedisConfig.FOLLOWING_CACHE + "::" + userId + "_0_10"));
    }

    @Test
    @DisplayName("follow should evict the followers and following caches")
    void followShouldEvictFollowCaches() {
        UUID userId = UUID.randomUUID();
        User follower = user("Follower", "follower@test.com");
        User followed = user("Followed", "followed@test.com");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowersByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(follower), PageRequest.of(0, 10), 1));

        GetFollowersCommand command = GetFollowersCommand.builder()
                .userId(userId).page(0).size(10).build();

        userService.getFollowers(command);
        assertTrue(redisTemplate.hasKey(RedisConfig.FOLLOWERS_CACHE + "::" + userId + "_0_10"));

        when(userRepository.findById(follower.getId())).thenReturn(Optional.of(follower));
        when(userRepository.findById(followed.getId())).thenReturn(Optional.of(followed));
        when(userFollowRepository.save(any(UserFollow.class))).thenAnswer(call -> call.getArgument(0));

        userService.follow(FollowCommand.builder()
                .followerId(follower.getId())
                .followedId(followed.getId())
                .build());

        assertFalse(redisTemplate.hasKey(RedisConfig.FOLLOWERS_CACHE + "::" + userId + "_0_10"),
                "follow() should have evicted the followers cache");

        userService.getFollowers(command);
        verify(userFollowRepository, times(2)).findFollowersByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("getBalancedFeed should cache using the time-bucketed SpEL key")
    void feedShouldBeCached() {
        User author = user("Author", "author@test.com");
        User reader = user("Reader", "reader@test.com");

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .content("Content")
                .authorName(author.getName())
                .date(LocalDate.now())
                .user(author)
                .build();

        when(userRepository.existsById(reader.getId())).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(reader.getId()))
                .thenReturn(Set.of(author.getId()));
        when(postRepository.countFeedPosts(eq(reader.getId()), any(LocalDate.class))).thenReturn(1L);
        when(postRepository.findFeedPosts(eq(reader.getId()), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of(post));

        GetFeedCommand command = GetFeedCommand.builder().user(reader).page(0).size(10).build();

        PageResponseDTO<PostResponseDTO> first = feedService.getBalancedFeed(command);
        PageResponseDTO<PostResponseDTO> second = feedService.getBalancedFeed(command);

        assertEquals(1, first.content().size());
        assertEquals(first.content(), second.content());

        verify(postRepository, times(1))
                .findFeedPosts(eq(reader.getId()), any(LocalDate.class), any(Pageable.class));

        Set<String> feedKeys = redisTemplate.keys(RedisConfig.FEED_CACHE + "::*");
        assertNotNull(feedKeys);
        assertEquals(1, feedKeys.size(), "Expected exactly one feed cache entry, got " + feedKeys);
        assertTrue(feedKeys.iterator().next().contains(reader.getId().toString()));
    }

    @Test
    @DisplayName("Cached feed page should be deserialized back into PageResponseDTO<PostResponseDTO>")
    void cachedFeedShouldDeserializeIntoDtos() {
        User author = user("Author", "author@test.com");
        User reader = user("Reader", "reader@test.com");

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .content("Content")
                .authorName(author.getName())
                .date(LocalDate.now())
                .user(author)
                .build();

        when(userRepository.existsById(reader.getId())).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(reader.getId()))
                .thenReturn(Set.of(author.getId()));
        when(postRepository.countFeedPosts(eq(reader.getId()), any(LocalDate.class))).thenReturn(1L);
        when(postRepository.findFeedPosts(eq(reader.getId()), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of(post));

        GetFeedCommand command = GetFeedCommand.builder().user(reader).page(0).size(10).build();
        feedService.getBalancedFeed(command);

        PageResponseDTO<PostResponseDTO> fromCache = feedService.getBalancedFeed(command);

        PostResponseDTO cached = fromCache.content().getFirst();
        assertEquals(post.getId(), cached.postId());
        assertEquals(post.getTitle(), cached.title());
        assertEquals(post.getDate(), cached.createdAt());
        assertEquals(author.getId(), cached.author().getId());
        assertEquals(author.getName(), cached.author().getName());
    }
}
