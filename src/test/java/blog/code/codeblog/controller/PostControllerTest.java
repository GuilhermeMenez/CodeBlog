package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.post.PutPostDTO;
import blog.code.codeblog.dto.post.CreatePostRequestDTO;
import blog.code.codeblog.dto.post.PostResponseDTO;
import blog.code.codeblog.facade.PostFacade;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.provider.UserProvider;
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
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostFacade postFacade;

    @Mock
    private UserProvider userProvider;

    @InjectMocks
    private PostController postController;

    private User currentUser;
    PostResponseDTO mockPost1;
    PostResponseDTO mockPost2;
    List<PostResponseDTO> mockPostList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());

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
    @DisplayName("Should return all posts for the current user")
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

        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(postFacade.getAllUserPosts(currentUser, page, size)).thenReturn(pageResponse);

        PageResponseDTO<PostResponseDTO> result = postController.getAllUserPosts(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("Primeiro Post", result.content().get(0).title());
        assertEquals("Segundo Post", result.content().get(1).title());
        assertTrue(result.first());
        assertTrue(result.last());
        assertEquals(0, result.currentPage());
        assertEquals(1, result.totalPages());
        verify(postFacade, times(1)).getAllUserPosts(currentUser, page, size);
    }

    @Test
    @DisplayName("Should return balanced feed for the current user")
    void getBalancedFeed() {
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

        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(postFacade.getFeed(currentUser, page, size)).thenReturn(pageResponse);

        PageResponseDTO<PostResponseDTO> result = postController.getBalancedFeed(page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("Primeiro Post", result.content().getFirst().title());
        verify(postFacade, times(1)).getFeed(currentUser, page, size);
    }

    @Test
    @DisplayName("Should create a new post")
    void createPost() {
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO(
                "First Post",
                "Content of the first post",
                null
        );

        String generatedPostId = UUID.randomUUID().toString();
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(postFacade.createPost(requestDTO, currentUser)).thenReturn(generatedPostId);

        String response = postController.createPost(requestDTO);

        assertNotNull(response);
        assertEquals(generatedPostId, response);
        verify(postFacade, times(1)).createPost(requestDTO, currentUser);
    }

    @Test
    @DisplayName("Should update a post")
    void updatePost() {
        UUID postId = UUID.randomUUID();
        PutPostDTO updatedPost = new PutPostDTO(postId, "New Title", "New Content");

        PostResponseDTO mockUpdatedPost = PostResponseDTO.builder()
                .postId(postId)
                .title("New Title")
                .content("New Content")
                .author(null)
                .createdAt(LocalDate.now())
                .images(Map.of())
                .build();
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(postFacade.updatePost(updatedPost, currentUser)).thenReturn(mockUpdatedPost);

        PostResponseDTO result = postController.updatePost(updatedPost);

        assertNotNull(result);
        assertEquals("New Title", result.title());
        verify(postFacade, times(1)).updatePost(updatedPost, currentUser);
    }

    @Test
    @DisplayName("Should handle generic error when updating a post")
    void updatePost_ServiceException() {
        UUID postId = UUID.randomUUID();
        PutPostDTO updatedPost = new PutPostDTO(postId, "New Title", "New Content");

        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(postFacade.updatePost(updatedPost, currentUser)).thenThrow(new RuntimeException("Unexpected error updating post"));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> postController.updatePost(updatedPost));

        assertEquals("Unexpected error updating post", exception.getMessage());
        verify(postFacade, times(1)).updatePost(updatedPost, currentUser);
    }

    @Test
    @DisplayName("Should delete a post")
    void deletePost() {
        UUID postId = UUID.randomUUID();
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(postFacade).deletePost(postId, currentUser);

        assertDoesNotThrow(() -> postController.deletePost(postId));
        verify(postFacade, times(1)).deletePost(postId, currentUser);
    }

}
