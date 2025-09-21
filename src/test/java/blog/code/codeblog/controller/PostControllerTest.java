package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PostDTO;
import blog.code.codeblog.dto.PostRequestDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.service.interfaces.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    @DisplayName("Deve retornar todos os posts de um usuário específico (mock)")
    void getAllUserPosts() {
        UUID userId = UUID.randomUUID();
        when(postService.getAllUserPosts(userId)).thenReturn(mockPostList);

        List<Post> result = postController.getAllUserPosts(userId).getBody();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Primeiro Post", result.get(0).getTitle());
        assertEquals("Segundo Post", result.get(1).getTitle());

        verify(postService, times(1)).getAllUserPosts(userId);
    }

    @Test
    @DisplayName("Deve retornar um post pelo ID (mock)")
    void getPostsbyId() {
        UUID postId = UUID.randomUUID();
        when(postService.findById(postId)).thenReturn(Optional.of(mockPost1));

        Post result = postController.getPostsbyId(postId);

        assertNotNull(result);
        assertEquals("Primeiro Post", result.getTitle());
        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("Deve criar um novo post (mock)")
    void createPost() {
        PostRequestDTO requestDTO = new PostRequestDTO(
                "Primeiro Post",
                "Conteúdo do primeiro post",
                UUID.randomUUID()
        );

        String generatedPostId = UUID.randomUUID().toString();
        when(postService.save(any(PostRequestDTO.class))).thenReturn(generatedPostId);

        ResponseEntity<String> response = postController.createPost(requestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(generatedPostId, response.getBody());

        verify(postService, times(1)).save(any(PostRequestDTO.class));
    }


    @Test
    @DisplayName("Deve atualizar um post (mock)")
    void updatePost() {
        UUID postId = UUID.randomUUID();
        PostDTO novoPost = new PostDTO("Novo Título", "Novo Texto");

        Post atualizadoMock = new Post();
        atualizadoMock.setId(postId);
        atualizadoMock.setTitle("Novo Título");
        atualizadoMock.setContent("Novo Texto");

        doNothing().when(postService).updatePost(postId, novoPost);

        ResponseEntity<?> response = postController.updatePost(postId, novoPost);

        assertEquals(200, response.getStatusCodeValue());
        verify(postService, times(1)).updatePost(postId, novoPost);
    }



    @Test
    @DisplayName("Deve tratar erro genérico ao atualizar um post")
    void updatePost_ServiceException() {
        UUID postId = UUID.randomUUID();
        PostDTO novoPost = new PostDTO("Novo Título", "Novo Texto");

        doThrow(new RuntimeException("Erro inesperado ao atualizar o post"))
                .when(postService).updatePost(postId, novoPost);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> postController.updatePost(postId, novoPost));

        assertEquals("Erro inesperado ao atualizar o post", exception.getMessage());
        verify(postService, times(1)).updatePost(postId, novoPost);
    }

    @Test
    @DisplayName("Deve deletar um post (mock)")
    void deletePost() {
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).deletePost(postId);

        ResponseEntity<?> response = postController.deletePost(postId);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(postService, times(1)).deletePost(postId);
        verify(postService, never()).findById(any());
    }

    @Test
    @DisplayName("Deve retornar feed balanceado para um usuário específico (mock)")
    void getBalancedFeed_Controller() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 3;

        Post p1 = new Post(); p1.setTitle("Post Recente 1");
        Post p2 = new Post(); p2.setTitle("Post Recente 2");
        Post p3 = new Post(); p3.setTitle("Post Aleatório 1");

        List<Post> mockFeed = List.of(p1, p2, p3);

        when(postService.getBalancedFeed(userId, page, size)).thenReturn(mockFeed);

        // Act
        ResponseEntity<List<Post>> response = postController.getBalancedFeed(userId, page, size);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("Post Recente 1", response.getBody().get(0).getTitle());
        assertEquals("Post Recente 2", response.getBody().get(1).getTitle());
        assertEquals("Post Aleatório 1", response.getBody().get(2).getTitle());

        verify(postService, times(1)).getBalancedFeed(userId, page, size);
    }

}