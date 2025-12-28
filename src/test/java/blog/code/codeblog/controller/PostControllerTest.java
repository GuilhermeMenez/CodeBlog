package blog.code.codeblog.controller;

import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.interfaces.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    Post mockPost1;
    Post mockPost2;
    List<Post> mockPostList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockPost1 = new Post();
        mockPost1.setId(UUID.randomUUID());
        mockPost1.setTitle("Primeiro Post");
        mockPost1.setAuthor("Autor 1");
        mockPost1.setContent("Conteúdo do primeiro post");
        mockPost1.setDate(LocalDate.of(2024, 7, 29));

        mockPost2 = new Post();
        mockPost2.setId(UUID.randomUUID());
        mockPost2.setTitle("Segundo Post");
        mockPost2.setAuthor("Autor 2");
        mockPost2.setContent("Conteúdo do segundo post");
        mockPost2.setDate(LocalDate.of(2024, 7, 28));

        mockPostList = Arrays.asList(mockPost1, mockPost2);
    }

    @Test
    @DisplayName("Should return all posts for a specific user (mock)")
    void getAllUserPosts() {
        UUID userId = UUID.randomUUID();
        when(postService.getAllUserPosts(userId)).thenReturn(mockPostList);

        List<Post> result = postController.getAllUserPosts(userId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Primeiro Post", result.get(0).getTitle());
        assertEquals("Segundo Post", result.get(1).getTitle());

        verify(postService, times(1)).getAllUserPosts(userId);
    }

    @Test
    @DisplayName("Should create a new post (mock)")
    void createPost() {
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO(
                "First Post",
                "Content of the first post",
                UUID.randomUUID()
        );

        String generatedPostId = UUID.randomUUID().toString();
        when(postService.save(any(CreatePostRequestDTO.class))).thenReturn(generatedPostId);

        String response = postController.createPost(requestDTO);

        assertNotNull(response);
        assertEquals(generatedPostId, response);
        verify(postService, times(1)).save(any(CreatePostRequestDTO.class));
    }

    @Test
    @DisplayName("Should update a post (mock)")
    void updatePost() {
        UUID postId = UUID.randomUUID();
        PutPostDTO updatedPost = new PutPostDTO("New Title", "New Content", UUID.randomUUID(), UUID.randomUUID());

        PostResponseDTO mockUpdatedPost = new PostResponseDTO(postId, "New Title", "New Content", null, LocalDate.now(), List.of());
        when(postService.updatePost(postId, updatedPost)).thenReturn(mockUpdatedPost);

        assertDoesNotThrow(() -> postController.updatePost(postId, updatedPost));
        verify(postService, times(1)).updatePost(postId, updatedPost);
    }

    @Test
    @DisplayName("Should handle generic error when updating a post")
    void updatePost_ServiceException() {
        UUID postId = UUID.randomUUID();
        PutPostDTO updatedPost = new PutPostDTO("New Title", "New Content", UUID.randomUUID(), UUID.randomUUID());

        when(postService.updatePost(postId, updatedPost)).thenThrow(new RuntimeException("Unexpected error updating post"));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> postController.updatePost(postId, updatedPost));

        assertEquals("Unexpected error updating post", exception.getMessage());
        verify(postService, times(1)).updatePost(postId, updatedPost);
    }

    @Test
    @DisplayName("Should delete a post (mock)")
    void deletePost() {
        UUID postId = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        doNothing().when(postService).deletePost(postId, token);

        assertDoesNotThrow(() -> postController.deletePost(postId, token));
        verify(postService, times(1)).deletePost(postId, token);
    }


}
