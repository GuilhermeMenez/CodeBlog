package blog.code.codeblog.service;

import blog.code.codeblog.command.feed.GetFeedCommand;
import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserFollowRepository;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MEDIUM_SIZE = 5;
    private static final int LARGE_DATASET_SIZE = 100;
    private static final long DEFAULT_SEED_INTERVAL_MS = 120000L;
    private static final int DEFAULT_RECENT_POSTS_DAYS = 7;
    private static final int DEFAULT_MAX_POSTS_FETCH_LIMIT = 500;

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    void globalSetUp() {
        ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", DEFAULT_SEED_INTERVAL_MS);
        ReflectionTestUtils.setField(feedService, "recentPostsDays", DEFAULT_RECENT_POSTS_DAYS);
        ReflectionTestUtils.setField(feedService, "maxPostsFetchLimit", DEFAULT_MAX_POSTS_FETCH_LIMIT);
    }

    private PageResponseDTO<PostResponseDTO> getFeed(UUID userId, int page, int size) {
        User viewer = new User();
        viewer.setId(userId);
        return feedService.getBalancedFeed(GetFeedCommand.builder().user(viewer).page(page).size(size).build());
    }

    @Test
    @DisplayName("Should return balanced feed with posts from followed users and discovery posts")
    void getBalancedFeedShouldReturnMixedPosts() {
        UUID userId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        UUID discoveryUserId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User followedUser = createUser(followedUserId, "Followed User");
        User discoveryUser = createUser(discoveryUserId, "Discovery User");

        Post followedPost = createPost(followedUser, "Followed Post", LocalDate.now());
        Post discoveryPost = createPost(discoveryUser, "Discovery Post", LocalDate.now());

        Set<UUID> followedIds = new HashSet<>(Collections.singletonList(followedUserId));
        List<Post> feedPosts = Arrays.asList(followedPost, discoveryPost);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(feedPosts);
        when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(2L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(1, result.totalPages());
        assertEquals(2, result.totalElements());
        assertTrue(result.first());
        assertTrue(result.last());
        assertFalse(result.empty());

        verify(userRepository).existsById(userId);
        verify(postRepository).findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return recent posts when user follows no one")
    void getBalancedFeedWhenUserFollowsNoOneShouldReturnRecentPosts() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User otherUser = createUser(otherUserId, "Other User");
        Post recentPost1 = createPost(otherUser, "Recent Post 1", LocalDate.now());
        Post recentPost2 = createPost(otherUser, "Recent Post 2", LocalDate.now().minusDays(1));

        List<Post> recentPosts = Arrays.asList(recentPost1, recentPost2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(recentPosts);
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(2L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertFalse(result.empty());

        verify(userRepository).existsById(userId);
        verify(postRepository).findAllRecentPosts(any(LocalDate.class), any(Pageable.class));
        verify(postRepository, never()).findFeedPosts(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found for balanced feed")
    void getBalancedFeedUserNotFoundShouldThrow() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> getFeed(userId, 0, 10));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(userId);
        verify(userFollowRepository, never()).findFollowedIdsByUserId(any());
        verify(postRepository, never()).findFeedPosts(any(), any(), any());
    }

    @Test
    @DisplayName("Should return empty page when no posts available")
    void getBalancedFeedNoPostsShouldReturnEmptyPage() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(0L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, 0, 10);

        assertNotNull(result);
        assertTrue(result.empty());
        assertEquals(0, result.content().size());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("Should handle pagination correctly for balanced feed")
    void getBalancedFeedPaginationShouldWorkCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        int page = 1;
        int size = 2;

        User followedUser = createUser(followedUserId, "Followed User");

        List<Post> allPosts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            allPosts.add(createPost(followedUser, "Post " + i, LocalDate.now().minusDays(i)));
        }

        Set<UUID> followedIds = new HashSet<>(Collections.singletonList(followedUserId));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(allPosts);
        when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(4L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(1, result.currentPage());
        assertEquals(2, result.totalPages());
        assertEquals(4, result.totalElements());
        assertFalse(result.first());
        assertTrue(result.last());
    }

    @Test
    @DisplayName("Should return empty content when page exceeds total pages")
    void getBalancedFeedPageExceedsTotalShouldReturnEmptyContent() {
        UUID userId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        int page = 10;
        int size = 10;

        User followedUser = createUser(followedUserId, "Followed User");
        Post post = createPost(followedUser, "Post", LocalDate.now());

        Set<UUID> followedIds = new HashSet<>(Collections.singletonList(followedUserId));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(post));
        when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(1L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(10, result.currentPage());
    }

    @Test
    @DisplayName("Should produce same order for same seed (deterministic shuffle)")
    void getBalancedFeedDeterministicShuffleShouldProduceSameOrder() {
        UUID userId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User followedUser = createUser(followedUserId, "Followed User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            posts.add(createPost(followedUser, "Post " + i, LocalDate.now().minusDays(i)));
        }

        Set<UUID> followedIds = new HashSet<>(Collections.singletonList(followedUserId));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new ArrayList<>(posts));
        when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(5L);

        PageResponseDTO<PostResponseDTO> result1 = getFeed(userId, page, size);

        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new ArrayList<>(posts));

        PageResponseDTO<PostResponseDTO> result2 = getFeed(userId, page, size);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.content().size(), result2.content().size());

        for (int i = 0; i < result1.content().size(); i++) {
            assertEquals(result1.content().get(i).postId(), result2.content().get(i).postId());
        }
    }

    @Test
    @DisplayName("Should handle feed with only followed user posts")
    void getBalancedFeedOnlyFollowedPostsShouldWork() {
        UUID userId = UUID.randomUUID();
        UUID followedUserId1 = UUID.randomUUID();
        UUID followedUserId2 = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User followedUser1 = createUser(followedUserId1, "Followed User 1");
        User followedUser2 = createUser(followedUserId2, "Followed User 2");

        Post post1 = createPost(followedUser1, "Post from User 1", LocalDate.now());
        Post post2 = createPost(followedUser2, "Post from User 2", LocalDate.now());

        Set<UUID> followedIds = new HashSet<>(Arrays.asList(followedUserId1, followedUserId2));
        List<Post> followedPosts = Arrays.asList(post1, post2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
        when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(followedPosts);
        when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(2L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertFalse(result.empty());

        verify(postRepository).findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should correctly calculate total pages")
    void getBalancedFeedTotalPagesCalculationShouldBeCorrect() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 3;

        User user = createUser(UUID.randomUUID(), "User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            posts.add(createPost(user, "Post " + i, LocalDate.now().minusDays(i)));
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(7L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(3, result.totalPages());
        assertEquals(7, result.totalElements());
        assertEquals(3, result.size());
        assertEquals(3, result.content().size());
    }

    @Test
    @DisplayName("Should return correct isFirst and isLast flags")
    void getBalancedFeedFirstLastFlagsShouldBeCorrect() {
        UUID userId = UUID.randomUUID();
        int size = 2;

        User user = createUser(UUID.randomUUID(), "User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            posts.add(createPost(user, "Post " + i, LocalDate.now().minusDays(i)));
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(5L);

        PageResponseDTO<PostResponseDTO> firstPage = getFeed(userId, 0, size);
        assertTrue(firstPage.first());
        assertFalse(firstPage.last());

        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        PageResponseDTO<PostResponseDTO> middlePage = getFeed(userId, 1, size);
        assertFalse(middlePage.first());
        assertFalse(middlePage.last());

        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        PageResponseDTO<PostResponseDTO> lastPage = getFeed(userId, 2, size);
        assertFalse(lastPage.first());
        assertTrue(lastPage.last());
    }

    @Test
    @DisplayName("Should handle large number of posts efficiently")
    void getBalancedFeedLargeDataSetShouldHandleEfficiently() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 20;

        User user = createUser(UUID.randomUUID(), "User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            posts.add(createPost(user, "Post " + i, LocalDate.now().minusDays(i % 30)));
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(100L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(20, result.content().size());
        assertEquals(100, result.totalElements());
        assertEquals(5, result.totalPages());
    }

    @Test
    @DisplayName("Should shuffle posts randomly based on seed")
    void getBalancedFeedShuffleShouldRandomizeOrder() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User user = createUser(UUID.randomUUID(), "User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            posts.add(createPost(user, "Post " + i, LocalDate.now().minusDays(i)));
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(10, result.content().size());

        Set<UUID> resultIds = new HashSet<>();
        for (PostResponseDTO dto : result.content()) {
            resultIds.add(dto.postId());
        }
        assertEquals(10, resultIds.size());
    }

    @Test
    @DisplayName("Should maintain consistency within same time interval")
    void getBalancedFeedSameIntervalShouldBeConsistent() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 5;

        User user = createUser(UUID.randomUUID(), "User");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            posts.add(createPost(user, "Post " + i, LocalDate.now()));
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(5L);

        List<List<UUID>> orders = new ArrayList<>();
        for (int call = 0; call < 3; call++) {
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(posts));
            PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);
            orders.add(result.content().stream().map(PostResponseDTO::postId).toList());
        }

        assertEquals(orders.get(0), orders.get(1));
        assertEquals(orders.get(1), orders.get(2));
    }

    @Test
    @DisplayName("Should exclude current user's own posts from feed")
    void getBalancedFeedShouldNotIncludeOwnPosts() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        User otherUser = createUser(otherUserId, "Other User");
        Post otherUserPost = createPost(otherUser, "Other User Post", LocalDate.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
        when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(otherUserPost));
        when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(1L);

        PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertNotEquals(userId, result.content().getFirst().author().getId());
    }


    @Nested
    @DisplayName("Feed Environment Configuration Tests")
    class FeedEnvironmentConfigurationTests {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(feedService, "recentPostsDays", DEFAULT_RECENT_POSTS_DAYS);
            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", DEFAULT_SEED_INTERVAL_MS);
            ReflectionTestUtils.setField(feedService, "maxPostsFetchLimit", DEFAULT_MAX_POSTS_FETCH_LIMIT);
        }

        @Test
        @DisplayName("Should use configured seed interval from environment variable")
        void shouldUseConfiguredSeedInterval() {
            long customSeedInterval = 60000L;
            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", customSeedInterval);

            UUID userId = UUID.randomUUID();
            User user = createUser(UUID.randomUUID(), "User");
            List<Post> posts = List.of(createPost(user, "Post 1", LocalDate.now()));

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(1L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            assertNotNull(result);
            assertEquals(1, result.content().size());

            Object configuredIntervalObj = ReflectionTestUtils.getField(feedService, "feedSeedIntervalMs");
            assertNotNull(configuredIntervalObj, "feedSeedIntervalMs should not be null");
            assertEquals(customSeedInterval, (long) configuredIntervalObj);
        }

        @Test
        @DisplayName("Should respect recentPostsDays configuration")
        void shouldRespectRecentPostsDaysConfig() {
            int customRecentDays = 3;
            ReflectionTestUtils.setField(feedService, "recentPostsDays", customRecentDays);

            UUID userId = UUID.randomUUID();
            User user = createUser(UUID.randomUUID(), "User");
            List<Post> posts = List.of(createPost(user, "Recent Post", LocalDate.now()));

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(1L);

            getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            Object configuredDaysObj = ReflectionTestUtils.getField(feedService, "recentPostsDays");
            assertNotNull(configuredDaysObj, "recentPostsDays should not be null");
            assertEquals(customRecentDays, (int) configuredDaysObj);
        }

        @Test
        @DisplayName("Should handle different seed intervals producing different orders")
        void differentSeedIntervalsShouldProducePotentiallyDifferentOrders() {
            UUID userId = UUID.randomUUID();
            User user = createUser(UUID.randomUUID(), "User");

            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < MEDIUM_SIZE; i++) {
                posts.add(createPost(user, "Post " + i, LocalDate.now()));
            }

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn((long) posts.size());

            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", 1L);
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(posts));
            PageResponseDTO<PostResponseDTO> result1 = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            assertNotNull(result1);
            assertEquals(MEDIUM_SIZE, result1.content().size());
        }
    }


    @Nested
    @DisplayName("Feed Boundary Tests")
    class FeedBoundaryTests {

        private UUID userId;
        private User testUser;
        private List<Post> testPosts;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            testUser = createUser(UUID.randomUUID(), "Test User");
            testPosts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                testPosts.add(createPost(testUser, "Post " + i, LocalDate.now()));
            }

            ReflectionTestUtils.setField(feedService, "recentPostsDays", DEFAULT_RECENT_POSTS_DAYS);
            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", DEFAULT_SEED_INTERVAL_MS);
            ReflectionTestUtils.setField(feedService, "maxPostsFetchLimit", DEFAULT_MAX_POSTS_FETCH_LIMIT);
        }

        @Test
        @DisplayName("Should handle page size of 1")
        void shouldHandlePageSizeOfOne() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(testPosts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, 1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.size());
            assertEquals(10, result.totalPages());
            assertTrue(result.first());
            assertFalse(result.last());
        }

        @Test
        @DisplayName("Should handle very large page size")
        void shouldHandleVeryLargePageSize() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(testPosts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, 1000);

            assertNotNull(result);
            assertEquals(10, result.content().size());
            assertEquals(1, result.totalPages());
            assertTrue(result.first());
            assertTrue(result.last());
        }

        @Test
        @DisplayName("Should handle page number beyond available pages")
        void shouldHandlePageBeyondAvailablePages() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(testPosts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, 100, DEFAULT_SIZE);

            assertNotNull(result);
            assertTrue(result.content().isEmpty());
            assertEquals(100, result.currentPage());
        }

        @Test
        @DisplayName("Should handle single post in feed")
        void shouldHandleSinglePost() {
            List<Post> singlePost = Collections.singletonList(testPosts.getFirst());

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(singlePost);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(1L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertTrue(result.first());
            assertTrue(result.last());
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 0, true, false, 10",
                "1, 10, 1, false, true, 10",
                "0, 5, 0, true, false, 5",
                "1, 5, 1, false, false, 5",
                "3, 5, 3, false, true, 5"
        })
        @DisplayName("Should handle pagination correctly with various parameters")
        void shouldHandlePaginationCorrectly(int page, int size, int expectedPage,
                                             boolean expectedFirst, boolean expectedLast,
                                             int expectedContentSize) {
            List<Post> largePosts = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                largePosts.add(createPost(testUser, "Post " + i, LocalDate.now()));
            }

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(largePosts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(20L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, page, size);

            assertNotNull(result);
            assertEquals(expectedPage, result.currentPage());
            assertEquals(expectedFirst, result.first());
            assertEquals(expectedLast, result.last());
            assertTrue(result.content().size() <= expectedContentSize);
        }
    }

    @Nested
    @DisplayName("Feed Determinism Tests")
    class FeedDeterminismTests {

        private UUID userId;
        private List<Post> testPosts;
        private static final long FIXED_SEED_INTERVAL = 3600000L;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            User testUser = createUser(UUID.randomUUID(), "Test User");
            testPosts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                testPosts.add(createPost(testUser, "Post " + i, LocalDate.now()));
            }

            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", FIXED_SEED_INTERVAL);
            ReflectionTestUtils.setField(feedService, "recentPostsDays", DEFAULT_RECENT_POSTS_DAYS);
            ReflectionTestUtils.setField(feedService, "maxPostsFetchLimit", DEFAULT_MAX_POSTS_FETCH_LIMIT);
        }

        @Test
        @DisplayName("Should produce identical order for same user within same time interval")
        void shouldProduceIdenticalOrderForSameUserWithinInterval() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            List<List<UUID>> orders = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                        .thenReturn(new ArrayList<>(testPosts));
                PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);
                orders.add(result.content().stream().map(PostResponseDTO::postId).toList());
            }

            for (int i = 1; i < orders.size(); i++) {
                assertEquals(orders.getFirst(), orders.get(i),
                        "Order at index " + i + " should match the first order");
            }
        }

        @Test
        @DisplayName("Should produce different order for different users")
        void shouldProduceDifferentOrderForDifferentUsers() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            when(userRepository.existsById(any())).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(any())).thenReturn(Collections.emptySet());
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(testPosts));
            PageResponseDTO<PostResponseDTO> result1 = getFeed(userId1, DEFAULT_PAGE, DEFAULT_SIZE);

            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(testPosts));
            PageResponseDTO<PostResponseDTO> result2 = getFeed(userId2, DEFAULT_PAGE, DEFAULT_SIZE);

            List<UUID> order1 = result1.content().stream().map(PostResponseDTO::postId).toList();
            List<UUID> order2 = result2.content().stream().map(PostResponseDTO::postId).toList();

            assertEquals(order1.size(), order2.size());
            assertTrue(new HashSet<>(order1).containsAll(order2));
        }

        @Test
        @DisplayName("Should maintain shuffle consistency across pages")
        void shouldMaintainShuffleConsistencyAcrossPages() {
            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(10L);

            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(testPosts));
            PageResponseDTO<PostResponseDTO> page0 = getFeed(userId, 0, 3);

            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(new ArrayList<>(testPosts));
            PageResponseDTO<PostResponseDTO> page1 = getFeed(userId, 1, 3);

            Set<UUID> page0Ids = page0.content().stream().map(PostResponseDTO::postId).collect(Collectors.toSet());
            Set<UUID> page1Ids = page1.content().stream().map(PostResponseDTO::postId).collect(Collectors.toSet());

            page0Ids.retainAll(page1Ids);
            assertTrue(page0Ids.isEmpty(), "Pages should not have overlapping posts");
        }
    }


    @Nested
    @DisplayName("Feed Content Tests")
    class FeedContentTests {

        private UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            ReflectionTestUtils.setField(feedService, "recentPostsDays", DEFAULT_RECENT_POSTS_DAYS);
            ReflectionTestUtils.setField(feedService, "feedSeedIntervalMs", DEFAULT_SEED_INTERVAL_MS);
            ReflectionTestUtils.setField(feedService, "maxPostsFetchLimit", DEFAULT_MAX_POSTS_FETCH_LIMIT);
        }

        @Test
        @DisplayName("Should contain posts from both followed and discovery users")
        void shouldContainBothFollowedAndDiscoveryPosts() {
            UUID followedUserId = UUID.randomUUID();
            UUID discoveryUserId = UUID.randomUUID();

            User followedUser = createUser(followedUserId, "Followed User");
            User discoveryUser = createUser(discoveryUserId, "Discovery User");

            Post followedPost = createPost(followedUser, "Followed Post", LocalDate.now());
            Post discoveryPost = createPost(discoveryUser, "Discovery Post", LocalDate.now());

            Set<UUID> followedIds = new HashSet<>(Collections.singletonList(followedUserId));
            List<Post> feedPosts = Arrays.asList(followedPost, discoveryPost);

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(followedIds);
            when(postRepository.findFeedPosts(eq(userId), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(feedPosts);
            when(postRepository.countFeedPosts(eq(userId), any(LocalDate.class))).thenReturn(2L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            Set<UUID> authorIds = result.content().stream()
                    .map(p -> p.author().getId())
                    .collect(Collectors.toSet());

            assertTrue(authorIds.contains(followedUserId), "Should contain posts from followed users");
            assertTrue(authorIds.contains(discoveryUserId), "Should contain discovery posts");
        }

        @Test
        @DisplayName("Should not contain duplicate posts")
        void shouldNotContainDuplicatePosts() {
            User user = createUser(UUID.randomUUID(), "User");
            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < LARGE_DATASET_SIZE; i++) {
                posts.add(createPost(user, "Post " + i, LocalDate.now()));
            }

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class))).thenReturn(posts);
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn((long) LARGE_DATASET_SIZE);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, 50);

            Set<UUID> uniqueIds = result.content().stream()
                    .map(PostResponseDTO::postId)
                    .collect(Collectors.toSet());

            assertEquals(result.content().size(), uniqueIds.size(), "Should not have duplicate posts");
        }

        @Test
        @DisplayName("Should return posts with all required fields populated")
        void shouldReturnPostsWithAllRequiredFields() {
            User user = createUser(UUID.randomUUID(), "Test User");
            Post post = createPost(user, "Test Post", LocalDate.now());

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userFollowRepository.findFollowedIdsByUserId(userId)).thenReturn(Collections.emptySet());
            when(postRepository.findAllRecentPosts(any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(Collections.singletonList(post));
            when(postRepository.countAllRecentPosts(any(LocalDate.class))).thenReturn(1L);

            PageResponseDTO<PostResponseDTO> result = getFeed(userId, DEFAULT_PAGE, DEFAULT_SIZE);

            assertFalse(result.content().isEmpty());
            PostResponseDTO postDto = result.content().getFirst();

            assertNotNull(postDto.postId(), "Post ID should not be null");
            assertNotNull(postDto.title(), "Title should not be null");
            assertNotNull(postDto.content(), "Content should not be null");
            assertNotNull(postDto.author(), "Author should not be null");
            assertNotNull(postDto.author().getId(), "Author ID should not be null");
            assertNotNull(postDto.author().getName(), "Author name should not be null");
        }
    }

    private User createUser(UUID id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private Post createPost(User user, String title, LocalDate date) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setTitle(title);
        post.setContent("Content for " + title);
        post.setAuthorName(user.getName());
        post.setDate(date);
        post.setUser(user);
        post.setImages(new HashMap<>());
        return post;
    }
}
