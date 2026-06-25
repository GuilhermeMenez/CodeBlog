package blog.code.codeblog.service;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.model.Post;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static blog.code.codeblog.config.RedisConfig.FEED_CACHE;

@Slf4j
@Service
public class FeedService extends PostService {
    @Value("${feed.recent-posts-days}")
    private int recentPostsDays;
    @Getter
    @Value("${feed.seed-interval-ms}")
    private long feedSeedIntervalMs;
    @Value("${feed.max-posts-fetch-limit}")
    private int maxPostsFetchLimit;


    @Cacheable(
            value = FEED_CACHE,
            key = "#userId + '_' + (T(System).currentTimeMillis() / @postService.feedSeedIntervalMs) + '_' + #page + '_' + #size",
            unless = "#result.empty == true"
    )
    public PageResponseDTO<PostResponseDTO> getBalancedFeed(UUID userId, int page, int size) {
        log.info("[getBalancedFeed] Getting balanced feed for userId: {} (page: {}, size: {})", userId, page, size);

        this.validateUserExists(userId);

        LocalDate since = LocalDate.now().minusDays(recentPostsDays);

        // Busca os IDs dos usuários seguidos uma única vez
        Set<UUID> followedUserIds = userFollowRepository.findFollowedIdsByUserId(userId);

        long totalElements = calculateTotalElements(userId, since, followedUserIds);

        long seed = generateDeterministicSeed(userId, totalElements);

        List<Post> allPosts = fetchFeedPosts(userId, since, page, size, followedUserIds);

        List<Post> shuffledPosts = shuffleWithSeed(allPosts, seed);

        List<PostResponseDTO> content = paginateAndConvert(shuffledPosts, page, size);

        return buildFeedResponse(content, page, size, totalElements);
    }

    private List<Post> shuffleWithSeed(List<Post> posts, long seed) {
        List<Post> shuffled = new ArrayList<>(posts);
        Collections.shuffle(shuffled, new Random(seed));
        return shuffled;
    }

    private List<Post> fetchFeedPosts(UUID userId, LocalDate since, int page, int size, Set<UUID> followedUserIds) {
        // Limita a quantidade de posts buscados para evitar sobrecarga em páginas distantes
        int totalPostsNeeded = Math.min((page + 1) * size, maxPostsFetchLimit);
        Pageable pageable = PageRequest.of(0, totalPostsNeeded);

        if (followedUserIds.isEmpty()) {
            log.info("[getBalancedFeed] User follows no one, returning recent posts");
            return postRepository.findAllRecentPosts(since, pageable);
        }

        return postRepository.findFeedPosts(userId, since, pageable);
    }

    private List<PostResponseDTO> paginateAndConvert(List<Post> posts, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, posts.size());

        if (fromIndex >= posts.size()) {
            return Collections.emptyList();
        }
        return posts.subList(fromIndex, toIndex).stream()
                .map(this::convertToPostResponseDTO)
                .collect(Collectors.toList());
    }

    private long calculateTotalElements(UUID userId, LocalDate since, Set<UUID> followedUserIds) {
        if (followedUserIds.isEmpty()) {
            return postRepository.countAllRecentPosts(since);
        }
        return postRepository.countFeedPosts(userId, since);
    }


    private long generateDeterministicSeed(UUID userId, long totalPosts) {
        long intervalMs = Math.max(feedSeedIntervalMs, 1L);
        long currentInterval = System.currentTimeMillis() / intervalMs;
        return userId.hashCode() + currentInterval + totalPosts;
    }


    private PageResponseDTO<PostResponseDTO> buildFeedResponse(
            List<PostResponseDTO> content,
            int page,
            int size,
            long totalElements) {

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponseDTO.<PostResponseDTO>builder()
                .content(content)
                .currentPage(page)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .size(size)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }

    protected void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("[getBalancedFeed] User not found. userId: {}", userId);
            throw new EntityNotFoundException("User not found");
        }


    }
}