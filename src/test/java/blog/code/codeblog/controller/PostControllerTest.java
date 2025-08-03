package blog.code.codeblog.controller;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        mockPost1.setId("1");
        mockPost1.setTitulo("Primeiro Post");
        mockPost1.setAutor("Autor 1");
        mockPost1.setTexto("Conteúdo do primeiro post");
        mockPost1.setData(LocalDate.of(2024, 7, 29));

        mockPost2 = new Post();
        mockPost2.setId("2");
        mockPost2.setTitulo("Segundo Post");
        mockPost2.setAutor("Autor 2");
        mockPost2.setTexto("Conteúdo do segundo post");
        mockPost2.setData(LocalDate.of(2024, 7, 28));

        mockPostList = Arrays.asList(mockPost1, mockPost2);
    }

    @Test
    @DisplayName("Deve retornar todos os posts de um usuário específico (mock)")
    void getAllUserPosts() {
        String userId = "123";
        when(postService.getAllUserPosts(userId)).thenReturn(mockPostList);

        List<Post> result = postController.getAllUserPosts(userId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Primeiro Post", result.get(0).getTitulo());
        assertEquals("Segundo Post", result.get(1).getTitulo());

        verify(postService, times(1)).getAllUserPosts(userId);
    }

    @Test
    @DisplayName("Deve retornar um post pelo ID (mock)")
    void getPostsbyId() {
        when(postService.findById("1")).thenReturn(Optional.of(mockPost1));

        Post result = postController.getPostsbyId("1");

        assertNotNull(result);
        assertEquals("Primeiro Post", result.getTitulo());
        verify(postService, times(1)).findById("1");
    }

    @Test
    @DisplayName("Deve criar um novo post (mock)")
    void createPost() {
        when(postService.save(any(Post.class))).thenReturn(mockPost1);

        ResponseEntity<Post> response = postController.createPost(mockPost1);

        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Primeiro Post", response.getBody().getTitulo());
        verify(postService, times(1)).save(mockPost1);
    }

    @Test
    @DisplayName("Deve atualizar um post (mock)")
    void updatePost() {
        Post novoPost = new Post();
        novoPost.setTitulo("Novo Título");
        novoPost.setAutor("Novo Autor");
        novoPost.setTexto("Novo Texto");
        novoPost.setData(LocalDate.of(2024, 7, 30));

        when(postService.findById("1")).thenReturn(Optional.of(mockPost1));
        when(postService.save(any(Post.class))).thenReturn(mockPost1);

        ResponseEntity<?> response = postController.updatePost("1", novoPost);

        assertEquals(200, response.getStatusCodeValue());
        Post atualizado = (Post) response.getBody();
        assertEquals("Novo Título", atualizado.getTitulo());
        assertEquals("Novo Autor", atualizado.getAutor());
        verify(postService, times(1)).save(mockPost1);
    }

    @Test
    @DisplayName("Deve deletar um post (mock)")
    void deletePost() {
        when(postService.findById("1")).thenReturn(Optional.of(mockPost1));
        doNothing().when(postService).delete("1");

        ResponseEntity<?> response = postController.deletePost("1");

        assertEquals(200, response.getStatusCodeValue());
        verify(postService, times(1)).findById("1");
        verify(postService, times(1)).delete("1");
    }

    @Test
    @DisplayName("Deve retornar feed balanceado para um usuário específico (mock)")
    void getBalancedFeed_Controller() {
        // Arrange
        String userId = "123";
        int page = 0;
        int size = 3;

        Post p1 = new Post(); p1.setTitulo("Post Recente 1");
        Post p2 = new Post(); p2.setTitulo("Post Recente 2");
        Post p3 = new Post(); p3.setTitulo("Post Aleatório 1");

        List<Post> mockFeed = List.of(p1, p2, p3);

        when(postService.getBalancedFeed(userId, page, size)).thenReturn(mockFeed);

        // Act
        ResponseEntity<List<Post>> response = postController.getBalancedFeed(userId, page, size);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("Post Recente 1", response.getBody().get(0).getTitulo());
        assertEquals("Post Recente 2", response.getBody().get(1).getTitulo());
        assertEquals("Post Aleatório 1", response.getBody().get(2).getTitulo());

        verify(postService, times(1)).getBalancedFeed(userId, page, size);
    }

}