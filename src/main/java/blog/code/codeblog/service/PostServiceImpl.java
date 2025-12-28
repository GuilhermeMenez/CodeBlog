package blog.code.codeblog.service;

import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.*;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.interfaces.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class PostServiceImpl implements PostService {


    @Autowired
    PostRepository postRepository;

    @Autowired
    @Lazy
    TokenService tokenService;


    @Autowired
    UserRepository userRepository;

    @Override
    public List<PostResponseDTO> findAll() {
        log.info("[findAll] Retrieving all posts");
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(this::convertToPostResponseDTO)
                .toList();
    }


    private PostResponseDTO convertToPostResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                new PostAuthorDTO(
                        post.getUser().getId(),
                        post.getUser().getName()
                ),
                post.getDate(),
                post.getComments().stream()
                        .map(this::convertToCommentResponseDTO)
                        .toList()
        );
    }


    private CommentResponseDTO convertToCommentResponseDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getAutor(),
                comment.getCreatedAt()
        );
    }

    @Override
    public PostResponseDTO findById(UUID id) {
        log.info("[findById] Attempting to find post with id: {}", id);
        return postRepository.findById(id).map(this::convertToPostResponseDTO)
                .orElseThrow(() -> {
                    log.warn("[findById] Post not found. id: {}", id);
                    return new RuntimeException("Post not found");
                });
    }

    private Optional<Post> findEntityById(UUID id) {
        return postRepository.findById(id);
    }


    @Override
    public String save(CreatePostRequestDTO post) {
        log.info("[save] Attempting to save new post for authorId: {}", post.authorId());
        User user = userRepository.findById(post.authorId())
                .orElseThrow(() -> {
                    log.warn("[save] Author not found. authorId: {}", post.authorId());
                    return new EntityNotFoundException("Author not found");
                });
        Post newPost = new Post();
        newPost.setTitle(post.title());
        newPost.setContent(post.content());
        newPost.setDate(LocalDate.now());
        newPost.setUser(user);
        newPost.setAuthor(user.getName());

        Post savedPost = postRepository.save(newPost);
        log.info("[save] Post saved successfully. postId: {}", savedPost.getId());
        return savedPost.getId().toString();
    }

    @Override
    public void deletePost(UUID postId, String token) {
        log.info("[deletePost] Attempting to delete post. postId: {}", postId);
        UUID userIdFromToken = UUID.fromString(tokenService.getSubjectIdFromToken(token));
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("[deletePost] Post not found. postId: {}", postId);
            return new RuntimeException("Post not found");
        });
        if (!post.getUser().getId().equals(userIdFromToken)) {
            log.warn("[deletePost] User not authorized to delete post. userId: {}, postId: {}", userIdFromToken, postId);
            throw new RuntimeException("User not authorized to delete this post");
        }
        postRepository.deleteById(postId);
        log.info("[deletePost] Post deleted successfully. postId: {}", postId);
    }


    @Override
    public List<Post> getBalancedFeed(UUID userId, int page, int size) {
        log.info("[getBalancedFeed] Getting balanced feed for userId: {} (page: {}, size: {})", userId, page, size);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[getBalancedFeed] User not found. userId: {}", userId);
                    return new RuntimeException("User not found");
                });
        Set<User> following = user.getFollowing();

        int recentSize = (int) (size * 0.7);
        int randomSize = size - recentSize;

        Pageable recentPageable = PageRequest.of(page, recentSize);
        Pageable randomPageable = PageRequest.of(page, randomSize);

        List<Post> recentPosts = postRepository.findRecentPosts(following, recentPageable);
        List<Post> randomPosts = postRepository.findRandomPosts(following, randomPageable);

        List<Post> combined = new ArrayList<>();
        combined.addAll(recentPosts);
        combined.addAll(randomPosts);

        Collections.shuffle(combined);

        return combined;
    }

    @Override
    public List<Post> getAllUserPosts(UUID userId) throws RuntimeException {
        log.info("[getAllUserPosts] Getting all posts for userId: {}", userId);
        return userRepository.findById(userId)
                .map(User::getPosts)
                .orElseThrow(() -> {
                    log.warn("[getAllUserPosts] User not found. userId: {}", userId);
                    return new RuntimeException("User not found");
                });
    }

    @Override
    public PostResponseDTO updatePost(UUID postId, PutPostDTO updatedPost) throws RuntimeException {
        log.info("[updatePost] Attempting to update post. postId: {}", postId);
        if (!updatedPost.authorId().equals(updatedPost.userId())) {
            log.warn("[updatePost] User not authorized to update post. userId: {}, postId: {}", updatedPost.userId(), postId);
            throw new RuntimeException("User not authorized to update this post");
        }
        Post existingPost = findEntityById(postId)
                .orElseThrow(() -> {
                    log.warn("[updatePost] Post not found. postId: {}", postId);
                    return new RuntimeException("Post not found: " + postId);
                });
        existingPost.setTitle(updatedPost.title());
        existingPost.setContent(updatedPost.content());
        existingPost.setDate(LocalDate.now());
        postRepository.save(existingPost);
        log.info("[updatePost] Post updated successfully. postId: {}", postId);
        return convertToPostResponseDTO(existingPost);
    }


    public Post getReference(UUID id) {
        return postRepository.getReferenceById(id);
    }

}
