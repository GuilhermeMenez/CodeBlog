package blog.code.codeblog.service;

import blog.code.codeblog.model.Post;
import blog.code.codeblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

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
        post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Optional<Post> result = postService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
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
        Long id = 1L;

        postService.delete(id);
        verify(postRepository).deleteById(id);
    }
}