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
        CreatePostRequestDTO request = new CreatePostRequestDTO("Test Title", "Test Content", userId);
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
        CreatePostRequestDTO request = new CreatePostRequestDTO("Test Title", "Test Content", userId);
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
        Post post1 = new Post();
        post1.setTitle("First Post");
        Post post2 = new Post();
        post2.setTitle("Second Post");
        User user = new User();
        user.setPosts(List.of(post1, post2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        List<Post> result = postService.getAllUserPosts(userId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("First Post", result.get(0).getTitle());
        assertEquals("Second Post", result.get(1).getTitle());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found for getAllUserPosts")
    void getAllUserPostsUserNotFoundShouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.getAllUserPosts(userId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when title is null")
    void savePostShouldThrowWhenTitleIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO(null, "content", UUID.randomUUID());
        assertThrows(EntityNotFoundException.class, () -> postService.save(postDTO));
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when content is null")
    void savePostShouldThrowWhenContentIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO("title", null, UUID.randomUUID());
        assertThrows(EntityNotFoundException.class, () -> postService.save(postDTO));
    }

    @Test
    @DisplayName("Should throw exception when authorId is null")
    void savePostShouldThrowWhenAuthorIdIsNull() {
        CreatePostRequestDTO postDTO = new CreatePostRequestDTO("title", "content", null);
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

  }

