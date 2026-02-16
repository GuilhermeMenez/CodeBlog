package blog.code.codeblog.service;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.*;
import blog.code.codeblog.enums.FlowImageFlag;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.interfaces.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Lazy
    @Autowired
    CloudinaryService cloudinaryService;

    @Override
    @Cacheable("posts_v4")
    public List<PostResponseDTO> findAll() {
        log.info("[findAll] Retrieving all posts");
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(this::convertToPostResponseDTO)
                .toList();
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

        Post newPost = Post.builder()
                .title(post.title())
                .content(post.content())
                .date(LocalDate.now())
                .user(user)
                .author(user.getName())
                .build();

        Post savedPost = postRepository.save(newPost);

        if (post.images() != null && !post.images().isEmpty()) {
            log.info("[save] Processing {} images for post: {}", post.images().size(), savedPost.getId());
            for (MultipartFile image : post.images()) {
                if (image != null && !image.isEmpty()) {
                    try {
                        cloudinaryService.uploadFile(image, FlowImageFlag.POST, null, savedPost.getId().toString());
                    } catch (IOException e) {
                        log.error("[save] Failed to upload image for post: {}. Error: {}", savedPost.getId(), e.getMessage());

                    }
                }
            }
        }

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

//    @Override
//    public List<PostResponseDTO> getBalancedFeed(UUID userId, int page, int size) {
//        log.info("[getBalancedFeed] Getting balanced feed for userId: {} (page: {}, size: {})", userId, page, size);
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> {
//                    log.warn("[getBalancedFeed] User not found. userId: {}", userId);
//                    return new RuntimeException("User not found");
//                });
//        Set<User> following = user.getFollowing();
//
//        int recentSize = (int) (size * 0.7);
//        int randomSize = size - recentSize;
//
//        Pageable recentPageable = PageRequest.of(page, recentSize);
//        Pageable randomPageable = PageRequest.of(page, randomSize);
//
//        List<Post> recentPosts = postRepository.findRecentPosts(following, recentPageable);
//        List<Post> randomPosts = postRepository.findRandomPosts(following, randomPageable);
//
//        List<Post> combined = new ArrayList<>();
//        combined.addAll(recentPosts);
//        combined.addAll(randomPosts);
//
//        Collections.shuffle(combined);
//
//        return combined.stream()
//                .map(this::convertToPostResponseDTO)
//                .toList();
//    }

    @Override
    public PageResponseDTO<PostResponseDTO> getAllUserPosts(UUID userId, int page, int size) throws EntityNotFoundException {
        log.info("[getAllUserPosts] Getting all posts for userId: {} (page: {}, size: {})", userId, page, size);

        if (!userRepository.existsById(userId)) {
            log.warn("[getAllUserPosts] User not found. userId: {}", userId);
            throw new EntityNotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByAuthorId(userId, pageable);

        List<PostResponseDTO> content = postPage.getContent().stream()
                .map(this::convertToPostResponseDTO)
                .toList();

        return PageResponseDTO.<PostResponseDTO>builder()
                .content(content)
                .currentPage(postPage.getNumber())
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .size(postPage.getSize())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .empty(postPage.isEmpty())
                .build();
    }

    @Override
    public PostResponseDTO updatePost(UUID postId, PutPostDTO updatedPost) throws EntityNotFoundException {
        log.info("[updatePost] Attempting to update post. postId: {}", postId);
        if (!updatedPost.authorId().equals(updatedPost.userId())) {
            log.warn("[updatePost] User not authorized to update post. userId: {}, postId: {}", updatedPost.userId(), postId);
            throw new RuntimeException("User not authorized to update this post");
        }
        Post existingPost = findEntityById(postId)
                .orElseThrow(() -> {
                    log.warn("[updatePost] Post not found. postId: {}", postId);
                    return new EntityNotFoundException("Post not found: " + postId);
                });
        existingPost.setTitle(updatedPost.title());
        existingPost.setContent(updatedPost.content());
        existingPost.setDate(LocalDate.now());
        postRepository.save(existingPost);
        log.info("[updatePost] Post updated successfully. postId: {}", postId);
        return convertToPostResponseDTO(existingPost);
    }

    public ImageUploadResponseDTO saveUploadedImage(UUID postId, String imageUrl, String publicId) {
        log.info("[uploadImage] Uploading image for: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("[uploadImage] Post not found id: {}", postId);
                    return new EntityNotFoundException("Post não encontrado");
                });
        post.getImages().put(publicId, imageUrl);
        postRepository.save(post);
        log.info("[uploadImage] Image uploaded for postId: {}", postId);
        return ImageUploadResponseDTO.builder()
                .message("Image uploaded")
                .imageUrl(imageUrl)
                .publicId(publicId)
                .build();
    }

    public Post getReference(UUID id) {
        return postRepository.getReferenceById(id);
    }

    public boolean deleteImage(String publicId) {
        log.info("[deleteImage] Attempting to delete image with publicId: {}", publicId);
        Optional<Post> postOpt = postRepository.findByImagePublicId(publicId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.getImages().remove(publicId);
            postRepository.save(post);
            log.info("[deleteImage] Image removed from post: {}", post.getId());
            return true;
        }
        return false;
    }

    private PostResponseDTO convertToPostResponseDTO(Post post) {
        Map<String, String> imagesCopy = post.getImages() != null
                ? new HashMap<>(post.getImages())
                : null;

        PostAuthorDTO author = new PostAuthorDTO(
                post.getUser().getId(),
                post.getUser().getName()
        );

        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                author,
                post.getDate(),
                imagesCopy
        );
    }


    private CommentResponseDTO convertToCommentResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAutor())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
