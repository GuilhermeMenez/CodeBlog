package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.service.interfaces.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    PostResponseDTO mockPost1;
    PostResponseDTO mockPost2;
    List<PostResponseDTO> mockPostList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockPost1 = PostResponseDTO.builder()
                .postId(UUID.randomUUID())
                .title("Primeiro Post")
                .content("Conteúdo do primeiro post")
                .author(null)
                .createdAt(LocalDate.of(2024, 7, 29))
                .images(Map.of())
                .build();

        mockPost2 = PostResponseDTO.builder()
                .postId(UUID.randomUUID())
                .title("Segundo Post")
                .content("Conteúdo do segundo post")
                .author(null)
                .createdAt(LocalDate.of(2024, 7, 28))
                .images(Map.of())
                .build();

        mockPostList = List.of(mockPost1, mockPost2);
    }

    @Test
    @DisplayName("Should return all posts for a specific user")
    void getAllUserPosts() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        PageResponseDTO<PostResponseDTO> pageResponse = PageResponseDTO.<PostResponseDTO>builder()
                .content(mockPostList)
                .currentPage(0)
                .totalPages(1)
                .totalElements(2)
                .size(10)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(postService.getAllUserPosts(userId, page, size)).thenReturn(pageResponse);

        PageResponseDTO<PostResponseDTO> result = postController.getAllUserPosts(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("Primeiro Post", result.content().get(0).title());
        assertEquals("Segundo Post", result.content().get(1).title());
        assertTrue(result.first());
        assertTrue(result.last());
        assertEquals(0, result.currentPage());
        assertEquals(1, result.totalPages());
        verify(postService, times(1)).getAllUserPosts(userId, page, size);
    }

//    @Test
//    @DisplayName("Should return balanced feed for a user")
//    void getBalancedFeed() {
//        UUID userId = UUID.randomUUID();
//        int page = 0;
//        int size = 10;
//        when(postService.getBalancedFeed(userId, page, size)).thenReturn(mockPostList);
//
//        List<PostResponseDTO> result = postController.getBalancedFeed(userId, page, size);
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("Primeiro Post", result.get(0).title());
//        verify(postService, times(1)).getBalancedFeed(userId, page, size);
//    }

    @Test
    @DisplayName("Should create a new post")
    void createPost() {
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO(
                "First Post",
                "Content of the first post",
                UUID.randomUUID(),
                null
        );

        String generatedPostId = UUID.randomUUID().toString();
        when(postService.save(any(CreatePostRequestDTO.class))).thenReturn(generatedPostId);

        String response = postController.createPost(requestDTO);

        assertNotNull(response);
        assertEquals(generatedPostId, response);
        verify(postService, times(1)).save(any(CreatePostRequestDTO.class));
    }

    @Test
    @DisplayName("Should update a post")
    void updatePost() {
        UUID postId = UUID.randomUUID();
        PutPostDTO updatedPost = new PutPostDTO("New Title", "New Content", UUID.randomUUID(), UUID.randomUUID());

        PostResponseDTO mockUpdatedPost = PostResponseDTO.builder()
                .postId(postId)
                .title("New Title")
                .content("New Content")
                .author(null)
                .createdAt(LocalDate.now())
                .images(Map.of())
                .build();
        when(postService.updatePost(postId, updatedPost)).thenReturn(mockUpdatedPost);

        PostResponseDTO result = postController.updatePost(postId, updatedPost);

        assertNotNull(result);
        assertEquals("New Title", result.title());
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
    @DisplayName("Should delete a post")
    void deletePost() {
        UUID postId = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        doNothing().when(postService).deletePost(postId, token);

        assertDoesNotThrow(() -> postController.deletePost(postId, token));
        verify(postService, times(1)).deletePost(postId, token);
    }

}
