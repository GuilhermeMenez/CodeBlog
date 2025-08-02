package blog.code.codeblog.service;

import blog.code.codeblog.dto.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    @DisplayName("Configuração inicial do UserServiceTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve encontrar um usuário pelo ID com sucesso")
    void findById() {
        User user = new User();
        user.setId("123");
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById("123");

        assertTrue(result.isPresent());
        assertEquals("123", result.get().getId());
        verify(userRepository).findById("123");
    }

    @Test
    @DisplayName("Deve encontrar um usuário pelo login com sucesso")
    void findByLogin() {
        User user = new User();
        user.setLogin("email@email.com");
        when(userRepository.findByLogin("email@email.com")).thenReturn(user);

        User result = userService.findByLogin("email@email.com");

        assertNotNull(result);
        assertEquals("email@email.com", result.getLogin());
        verify(userRepository).findByLogin("email@email.com");
    }

    @Test
    @DisplayName("Deve salvar um novo usuário com sucesso")
    void saveUser() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        userService.saveUser(user);

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve atualizar um usuário existente com sucesso")
    void updateUser() {
        String id = "456";
        User existingUser = new User();
        existingUser.setId(id);

        UserDTO dto = new UserDTO("Novo Nome", "novo@email.com", "novaSenha", null);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        Optional<User> updatedUser = userService.updateUser(id, dto);

        assertTrue(updatedUser.isPresent());
        assertEquals("Novo Nome", updatedUser.get().getName());
        assertEquals("novo@email.com", updatedUser.get().getLogin());
        assertTrue(new BCryptPasswordEncoder().matches("novaSenha", updatedUser.get().getPassword()));
        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve deletar um usuário pelo ID com sucesso")
    void deleteUser() {
        String id = "789";
        when(userRepository.existsById(id)).thenReturn(true);

        boolean result = userService.deleteUser(id);

        assertTrue(result);
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lidar corretamente com as ações de seguir e deixar de seguir usuários")
    void handleFollowUnfollow() {
        String followerId = "1";
        String followedId = "2";

        FollowUnfollowRequestDTO dto = mock(FollowUnfollowRequestDTO.class);
        when(dto.followerId()).thenReturn(followerId);
        when(dto.followedId()).thenReturn(followedId);

        User follower = mock(User.class);
        User followed = mock(User.class);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));

        boolean followResult = userService.handleFollowUnfollow(dto, true);
        verify(followed).addFollower(follower);
        verify(userRepository).save(followed);
        assertFalse(followResult);

        reset(followed);
        when(dto.followerId()).thenReturn(followerId);
        boolean unfollowResult = userService.handleFollowUnfollow(dto, false);
        verify(followed).removeFollower(follower);
        verify(userRepository, times(2)).save(followed);
        assertFalse(unfollowResult);

        when(dto.followerId()).thenReturn("x");
        when(dto.followedId()).thenReturn("x");
        boolean sameIdResult = userService.handleFollowUnfollow(dto, true);
        assertFalse(sameIdResult);
    }
}