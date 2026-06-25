package blog.code.codeblog.service;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.comment.CommentResponseDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostAuthorDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import blog.code.codeblog.model.Comment;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.CommentRepository;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserFollowRepository;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.interfaces.PostServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static blog.code.codeblog.config.RedisConfig.*;
import static java.util.UUID.fromString;

@Slf4j
@Service
public class PostService implements PostServiceInterface {

    @Autowired
    PostRepository postRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserFollowRepository userFollowRepository;

    @Override
    public List<PostResponseDTO> findAll() {
        log.info("[findAll] Retrieving all posts");
        return postRepository.findAll().stream()
                .map(this::convertToPostResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(
            value = USER_POSTS_CACHE,
            key = "#userId + '_' + #page + '_' + #size",
            unless = "#result.empty == true"
    )
    public PageResponseDTO<PostResponseDTO> getAllUserPosts(UUID userId, int page, int size) throws EntityNotFoundException {
        log.info("[getAllUserPosts] Getting all posts for userId: {} (page: {}, size: {})", userId, page, size);

        if (!userRepository.existsById(userId)) {
            log.warn("[getAllUserPosts] User not found. userId: {}", userId);
            throw new EntityNotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByAuthorId(userId, pageable);

        List<PostResponseDTO> content = postPage.getContent().stream()
                .map(this::convertToPostResponseDTO).collect(Collectors.toList());

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
    @Cacheable(value = POST_CACHE, key = "#id", unless = "#result == null")
    public PostResponseDTO findById(UUID id) {
        log.info("[findById] Attempting to find post with id: {}", id);
        return postRepository.findById(id).map(this::convertToPostResponseDTO)
                .orElseThrow(() -> {
                    log.warn("[findById] Post not found. id: {}", id);
                    return new RuntimeException("Post not found");
                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER_POSTS_CACHE, allEntries = true),
            @CacheEvict(value = FEED_CACHE, allEntries = true)
    })
    public Post save(CreatePostRequestDTO post) {
        //todo pegar o user id do token do contexto
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

        log.info("[save] Post saved successfully. postId: {}", savedPost.getId());
        return savedPost;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = POST_CACHE, key = "#postId"),
            @CacheEvict(value = USER_POSTS_CACHE, allEntries = true),
            @CacheEvict(value = FEED_CACHE, allEntries = true)
    })
    public void deletePost(UUID postId, String token) {
        log.info("[deletePost] Attempting to delete post. postId: {}", postId);
        UUID userIdFromToken = fromString(tokenService.getSubjectIdFromToken(token));
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
    @Caching(evict = {
            @CacheEvict(value = POST_CACHE, key = "#postId"),
            @CacheEvict(value = USER_POSTS_CACHE, allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = POST_CACHE, key = "#postId"),
            @CacheEvict(value = USER_POSTS_CACHE, allEntries = true)
    })
    public ImageUploadResponseDTO savePostImage(UUID postId, MultipartFile image) throws IOException {
        Post post = getAuthorizedPost(postId);
        log.info("[uploadImage] Uploading image for: {}", postId);

        var uploadResponse = cloudinaryService.uploadFile(image, FlowImageFlag.POST, post.getUser().getId().toString(), postId.toString());

        if (post.getImages() == null) {
            post.setImages(new HashMap<>());
        }

        post.getImages().put(uploadResponse.publicId(), uploadResponse.imageUrl());
        postRepository.save(post);
        log.info("[saveImage] Image uploaded for postId: {}", postId);

        return uploadResponse;
    }


    public void processImages(Post post, List<MultipartFile> images) throws IOException {
        if (images != null && !images.isEmpty()) {
            log.info("[save] Processing {} images for post: {}", images.size(), post.getId());
            for (MultipartFile image : images) {
                if (image != null) {
                    savePostImage(post.getId(), image);
                }
            }
        } else {
            log.info("[save] No images found for post: {}", post.getId());
        }
    }



    private Post getAuthorizedPost(UUID postId) {
        UUID userIdFromContext = tokenService.getUserIdFromContext();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("[getAuthorizedPost] Post not found. postId: {}", postId);
                    return new EntityNotFoundException("Post not found");
                });

        if (!post.getUser().getId().equals(userIdFromContext)) {
            log.warn("[getAuthorizedPost] User not authorized for this action. userId: {}, postId: {}", userIdFromContext, postId);
            throw new AccessDeniedException("User not authorized for this action");
        }

        return post;
    }




    public Post getReference(UUID id) {
        return postRepository.getReferenceById(id);
    }

    @CacheEvict(value = USER_POSTS_CACHE, allEntries = true)
    public boolean deleteImage(String publicId) throws IOException {
        log.info("[deleteImage] Attempting to delete image with publicId: {}", publicId);
        Optional<Post> postOpt = postRepository.findByImagePublicId(publicId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            getAuthorizedPost(post.getId());
            post.getImages().remove(publicId);
            cloudinaryService.deleteFile(publicId);
            postRepository.save(post);
            log.info("[deleteImage] Image removed from post: {}", post.getId());
            return true;
        }
        return false;
    }


    @Override
    @Cacheable(value = POST_COMMENTS_CACHE, key = "#postId + '_' + #page + '_' + #size")
    public PageResponseDTO<CommentResponseDTO> getPostComments(UUID postId, int page, int size) {
        log.info("[getAllComments] Getting all comments for postId: {} (page: {}, size: {})", postId, page, size);
        Pageable pageable = PageRequest.of(page, size);

        Page<Comment> commentPage = commentRepository.findByPost_Id(postId, pageable);

        if (commentPage.isEmpty()) {
            log.warn("[getAllComments] No comments found for postId: {}", postId);
            throw new EntityNotFoundException("No comments found for postId: " + postId);
        }


        List<CommentResponseDTO> comments = commentPage.getContent().stream()
                .map(this::convertToCommentResponseDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CommentResponseDTO>builder()
                .content(comments)
                .currentPage(commentPage.getNumber())
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .size(commentPage.getSize())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .empty(commentPage.isEmpty())
                .build();
    }


    private Optional<Post> findEntityById(UUID id) {
        return postRepository.findById(id);
    }

    protected PostResponseDTO convertToPostResponseDTO(Post post) {
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
