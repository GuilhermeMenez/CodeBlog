package blog.code.codeblog.service;

import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    @DisplayName("Configuração inicial para PostServiceImpl")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve retornar todos os posts corretamente")
    void findAll() {
        List<Post> posts = Arrays.asList(new Post(), new Post());
        when(postRepository.findAll()).thenReturn(posts);

        List<Post> result = postService.findAll();

        assertEquals(2, result.size());
        verify(postRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar e retornar um post pelo ID")
    void findById() {
        Post post = new Post();
        post.setId("1");
        when(postRepository.findById(Long.valueOf("1"))).thenReturn(Optional.of(post));

        Optional<Post> result = postService.findById("1");

        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve salvar um novo post com sucesso")
    void save() {
        Post post = new Post();
        when(postRepository.save(post)).thenReturn(post);

        Post saved = postService.save(post);

        assertNotNull(saved);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Deve deletar um post pelo ID")
    void delete() {
        String id = "1";

        postService.delete(id);
        verify(postRepository).deleteById(Long.valueOf(id));
    }
    @Test
    @DisplayName("Deve retornar todos os posts do usuário quando ele existir")
    void getAllUserPostsUserFound() {
        String userId = "123";
        Post post1 = new Post();
        post1.setTitulo("Primeiro Post");
        Post post2 = new Post();
        post2.setTitulo("Segundo Post");

        User mockUser = new User();
        mockUser.setPosts(List.of(post1, post2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        List<Post> result = postService.getAllUserPosts(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Primeiro Post", result.get(0).getTitulo());
        assertEquals("Segundo Post", result.get(1).getTitulo());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void getAllUserPostsUserNotFound() {
        // Arrange
        String userId = "999";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.getAllUserPosts(userId);
        });

        assertEquals("usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar feed balanceado (70% recentes e 30% aleatórios)")
    void getBalancedFeed_UserFound() {
        // Arrange
        String userId = "123";
        User mockUser = new User();
        mockUser.setId(userId);

        // Simula os usuários seguidos
        User followed1 = new User();
        followed1.setId("u1");
        User followed2 = new User();
        followed2.setId("u2");
        mockUser.setFollowing(Set.of(followed1, followed2));

        // Simula posts recentes
        Post recent1 = new Post();
        recent1.setTitulo("Post Recente 1");
        Post recent2 = new Post();
        recent2.setTitulo("Post Recente 2");

        // Simula posts aleatórios
        Post random1 = new Post();
        random1.setTitulo("Post Aleatório 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(postRepository.findRecentPosts(eq(mockUser.getFollowing()), any(Pageable.class)))
                .thenReturn(List.of(recent1, recent2));
        when(postRepository.findRandomPosts(eq(mockUser.getFollowing()), any(Pageable.class)))
                .thenReturn(List.of(random1));

        // Act
        List<Post> feed = postService.getBalancedFeed(userId, 0, 3);

        // Assert
        assertNotNull(feed);
        assertEquals(3, feed.size());
        assertTrue(feed.stream().anyMatch(p -> p.getTitulo().contains("Recente")));
        assertTrue(feed.stream().anyMatch(p -> p.getTitulo().contains("Aleatório")));

        verify(userRepository, times(1)).findById(userId);
        verify(postRepository, times(1)).findRecentPosts(eq(mockUser.getFollowing()), any(Pageable.class));
        verify(postRepository, times(1)).findRandomPosts(eq(mockUser.getFollowing()), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado no feed")
    void getBalancedFeed_UserNotFound() {
        // Arrange
        String userId = "999";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.getBalancedFeed(userId, 0, 3);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(postRepository, never()).findRecentPosts(anySet(), any(Pageable.class));
        verify(postRepository, never()).findRandomPosts(anySet(), any(Pageable.class));
    }



}