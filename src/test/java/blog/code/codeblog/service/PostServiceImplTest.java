package blog.code.codeblog.service;

import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("Should save post successfully and return postId")
    void savePostShouldReturnId() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        CreatePostRequestDTO request = new CreatePostRequestDTO("Test Title", "Test Content", userId, null);
        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle(request.title());
        mockPost.setContent(request.content());
        mockPost.setDate(LocalDate.now());
        mockPost.setUser(mockUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);
        String result = postService.save(request);
        assertEquals(mockPost.getId().toString(), result);
        verify(postRepository).save(any(Post.class));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when author not found on save")
    void savePostAuthorNotFoundShouldThrow() {
        UUID userId = UUID.randomUUID();
        CreatePostRequestDTO request = new CreatePostRequestDTO("Test Title", "Test Content", userId, null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.save(request));
        assertEquals("Author not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should find post by id and return PostResponseDTO")
    void findByIdShouldReturnResponseDTO() {
        UUID postId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Author");
        Post post = new Post();
        post.setId(postId);
        post.setTitle("Title");
        post.setContent("Content");
        post.setDate(LocalDate.now());
        post.setUser(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        PostResponseDTO result = postService.findById(postId);
        assertNotNull(result);
        assertEquals(postId, result.postId());
        assertEquals("Title", result.title());
        assertEquals("Content", result.content());
        assertEquals(user.getId(), result.author().id());
        assertEquals(user.getName(), result.author().name());
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when post not found by id")
    void findByIdNotFoundShouldThrow() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.findById(postId));
        assertEquals("Post not found", exception.getMessage());
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("Should update post successfully and return PostResponseDTO")
    void updatePostShouldReturnResponseDTO() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PutPostDTO updateDTO = new PutPostDTO("Updated Title", "Updated Content", userId, userId);
        User user = new User();
        user.setId(userId);
        user.setName("Author");
        Post post = new Post();
        post.setId(postId);
        post.setTitle("Old Title");
        post.setContent("Old Content");
        post.setDate(LocalDate.now().minusDays(1));
        post.setUser(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        PostResponseDTO result = postService.updatePost(postId, updateDTO);
        assertNotNull(result);
        assertEquals(postId, result.postId());
        assertEquals("Updated Title", result.title());
        assertEquals("Updated Content", result.content());
        assertEquals(user.getId(), result.author().id());
        assertEquals(user.getName(), result.author().name());
        verify(postRepository).findById(postId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when post not found on update")
    void updatePostNotFoundShouldThrow() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PutPostDTO updateDTO = new PutPostDTO("Title", "Content", userId, userId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.updatePost(postId, updateDTO));
        assertEquals("Post not found: " + postId, exception.getMessage());
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not authorized to update post")
    void updatePostUnauthorizedShouldThrow() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        PutPostDTO updateDTO = new PutPostDTO("Title", "Content", userId, otherUserId);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.updatePost(postId, updateDTO));
        assertEquals("User not authorized to update this post", exception.getMessage());
        verify(postRepository, never()).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should delete post successfully when authorized")
    void deletePostShouldSucceed() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = userId.toString();
        User user = new User();
        user.setId(userId);
        Post post = new Post();
        post.setId(postId);
        post.setUser(user);
        when(tokenService.getSubjectIdFromToken(token)).thenReturn(userId.toString());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).deleteById(postId);
        assertDoesNotThrow(() -> postService.deletePost(postId, token));
        verify(tokenService).getSubjectIdFromToken(token);
        verify(postRepository).findById(postId);
        verify(postRepository).deleteById(postId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when post not found on delete")
    void deletePostNotFoundShouldThrow() {
        UUID postId = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        when(tokenService.getSubjectIdFromToken(token)).thenReturn(token);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.deletePost(postId, token));
        assertEquals("Post not found", exception.getMessage());
        verify(tokenService).getSubjectIdFromToken(token);
        verify(postRepository).findById(postId);
        verify(postRepository, never()).deleteById(postId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not authorized to delete post")
    void deletePostUnauthorizedShouldThrow() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        String token = userId.toString();
        User user = new User();
        user.setId(otherUserId);
        Post post = new Post();
        post.setId(postId);
        post.setUser(user);
        when(tokenService.getSubjectIdFromToken(token)).thenReturn(userId.toString());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.deletePost(postId, token));
        assertEquals("User not authorized to delete this post", exception.getMessage());
        verify(tokenService).getSubjectIdFromToken(token);
        verify(postRepository).findById(postId);
        verify(postRepository, never()).deleteById(postId);
    }

    @Test
    @DisplayName("Should get all posts for user")
    void getAllUserPostsShouldReturnPosts() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 10;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        Post post1 = new Post();
        post1.setId(UUID.randomUUID());
        post1.setTitle("First Post");
        post1.setContent("Content 1");
        post1.setAuthor("Test User");
        post1.setDate(java.time.LocalDate.now());
        post1.setUser(user);

        Post post2 = new Post();
        post2.setId(UUID.randomUUID());
        post2.setTitle("Second Post");
        post2.setContent("Content 2");
        post2.setAuthor("Test User");
        post2.setDate(java.time.LocalDate.now());
        post2.setUser(user);

        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), PageRequest.of(page, size), 2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.findByAuthorId(eq(userId), any())).thenReturn(postPage);

        var result = postService.getAllUserPosts(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("First Post", result.content().get(0).title());
        assertEquals("Second Post", result.content().get(1).title());
        assertEquals(0, result.currentPage());
        assertEquals(1, result.totalPages());
        assertEquals(2, result.totalElements());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(userRepository).existsById(userId);
        verify(postRepository).findByAuthorId(eq(userId), any());
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found for getAllUserPosts")
    void getAllUserPostsUserNotFoundShouldThrow() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 10;
        when(userRepository.existsById(userId)).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.getAllUserPosts(userId, page, size));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when title is null")
    void savePostShouldThrowWhenTitleIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO(null, "content", UUID.randomUUID(), null);
        assertThrows(EntityNotFoundException.class, () -> postService.save(postDTO));
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when content is null")
    void savePostShouldThrowWhenContentIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO("title", null, UUID.randomUUID(), null);
        assertThrows(EntityNotFoundException.class, () -> postService.save(postDTO));
    }

    @Test
    @DisplayName("Should throw exception when authorId is null")
    void savePostShouldThrowWhenAuthorIdIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO("title", "content", null, null);
        Exception exception = assertThrows(Exception.class, () -> postService.save(postDTO));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw RuntimeException when postId is null")
    void updatePostShouldThrowWhenPostIdIsNull() {
        UUID userId = UUID.randomUUID();
        PutPostDTO updateDTO = new PutPostDTO("title", "content", userId, userId);
        assertThrows(RuntimeException.class, () -> postService.updatePost(null, updateDTO));
    }

    @Test
    @DisplayName("Should throw RuntimeException when post not found")
    void updatePostShouldThrowWhenPostNotFound() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PutPostDTO updateDTO = new PutPostDTO("title", "content", userId, userId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> postService.updatePost(postId, updateDTO));
    }

    @Test
    @DisplayName("Should save uploaded image successfully")
    void saveUploadedImageSuccess() {
        UUID postId = UUID.randomUUID();
        String imageUrl = "https://cloudinary.com/test-image.jpg";
        String publicId = "post_pics/test-image";

        Post post = new Post();
        post.setId(postId);
        post.setImages(new HashMap<>());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = postService.saveUploadedImage(postId, imageUrl, publicId);

        assertNotNull(result);
        assertEquals("Image uploaded", result.message());
        assertEquals(imageUrl, result.imageUrl());
        assertEquals(publicId, result.publicId());
        assertTrue(post.getImages().containsKey(publicId));
        assertEquals(imageUrl, post.getImages().get(publicId));
        verify(postRepository).findById(postId);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when saving image for non-existent post")
    void saveUploadedImagePostNotFound() {
        UUID postId = UUID.randomUUID();
        String imageUrl = "https://cloudinary.com/test-image.jpg";
        String publicId = "post_pics/test-image";

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> postService.saveUploadedImage(postId, imageUrl, publicId));

        assertEquals("Post não encontrado", exception.getMessage());
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete image from post successfully")
    void deleteImageSuccess() {
        String publicId = "post_pics/test-image";
        Post post = new Post();
        post.setId(UUID.randomUUID());
        Map<String, String> images = new HashMap<>();
        images.put(publicId, "https://cloudinary.com/test-image.jpg");
        post.setImages(images);

        when(postRepository.findByImagePublicId(publicId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = postService.deleteImage(publicId);

        assertTrue(result);
        assertFalse(post.getImages().containsKey(publicId));
        verify(postRepository).findByImagePublicId(publicId);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Should return false when image not found for deletion")
    void deleteImageNotFound() {
        String publicId = "post_pics/nonexistent-image";

        when(postRepository.findByImagePublicId(publicId)).thenReturn(Optional.empty());

        boolean result = postService.deleteImage(publicId);

        assertFalse(result);
        verify(postRepository).findByImagePublicId(publicId);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get post reference by ID")
    void getReferenceSuccess() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setId(postId);

        when(postRepository.getReferenceById(postId)).thenReturn(post);

        Post result = postService.getReference(postId);

        assertEquals(post, result);
        verify(postRepository).getReferenceById(postId);
    }

  }

