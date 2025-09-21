package blog.code.codeblog.service;

import blog.code.codeblog.dto.PostDTO;
import blog.code.codeblog.dto.PostRequestDTO;
import blog.code.codeblog.model.Post;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.PostRepository;
import blog.code.codeblog.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

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
        UUID id = UUID.randomUUID();
        Post post = new Post();
        post.setId(id);
        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        Optional<Post> result = postService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(postRepository).findById(id);
    }

    @Test
    @DisplayName("Deve salvar um novo post com sucesso")
    void save() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        PostRequestDTO request = new PostRequestDTO("Título Teste", "Conteúdo Teste", userId);

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle(request.title());
        mockPost.setContent(request.content());
        mockPost.setDate(LocalDate.now());
        mockPost.setUser(mockUser);

        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);

        // Act
        String result = postService.save(request);

        // Assert
        assertEquals(mockPost.getId().toString(), result);
        verify(postRepository).save(any(Post.class));
        verify(userRepository).getReferenceById(userId);

    }



    @Test
    @DisplayName("Deve deletar um post pelo ID")
    void delete() {
        UUID id = UUID.randomUUID();

        postService.deletePost(id);
        verify(postRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve retornar todos os posts do usuário quando ele existir")
    void getAllUserPostsUserFound() {
        UUID userId = UUID.randomUUID();
        Post post1 = new Post();
        post1.setTitle("Primeiro Post");
        Post post2 = new Post();
        post2.setTitle("Segundo Post");

        User mockUser = new User();
        mockUser.setPosts(List.of(post1, post2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        List<Post> result = postService.getAllUserPosts(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Primeiro Post", result.get(0).getTitle());
        assertEquals("Segundo Post", result.get(1).getTitle());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void getAllUserPostsUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.getAllUserPosts(userId));

        assertEquals("usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar feed balanceado (70% recentes e 30% aleatórios)")
    void getBalancedFeed_UserFound() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        User followed1 = new User();
        followed1.setId(UUID.randomUUID());

        User followed2 = new User();
        followed2.setId(UUID.randomUUID());

        mockUser.setFollowing(Set.of(followed1, followed2));

        Post recent1 = new Post();
        recent1.setTitle("Post Recente 1");
        Post recent2 = new Post();
        recent2.setTitle("Post Recente 2");

        Post random1 = new Post();
        random1.setTitle("Post Aleatório 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(postRepository.findRecentPosts(eq(mockUser.getFollowing()), any(Pageable.class)))
                .thenReturn(List.of(recent1, recent2));
        when(postRepository.findRandomPosts(eq(mockUser.getFollowing()), any(Pageable.class)))
                .thenReturn(List.of(random1));

        List<Post> feed = postService.getBalancedFeed(userId, 0, 3);

        assertNotNull(feed);
        assertEquals(3, feed.size());
        assertTrue(feed.stream().anyMatch(p -> p.getTitle().contains("Recente")));
        assertTrue(feed.stream().anyMatch(p -> p.getTitle().contains("Aleatório")));

        verify(userRepository, times(1)).findById(userId);
        verify(postRepository, times(1)).findRecentPosts(eq(mockUser.getFollowing()), any(Pageable.class));
        verify(postRepository, times(1)).findRandomPosts(eq(mockUser.getFollowing()), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado no feed")
    void getBalancedFeed_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> postService.getBalancedFeed(userId, 0, 3));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(postRepository, never()).findRecentPosts(anySet(), any(Pageable.class));
        verify(postRepository, never()).findRandomPosts(anySet(), any(Pageable.class));
    }

}