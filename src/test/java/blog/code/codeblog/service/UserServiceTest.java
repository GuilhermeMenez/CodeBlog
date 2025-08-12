package blog.code.codeblog.service;

import blog.code.codeblog.dto.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
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

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;


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
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        User existingUser = new User();
        existingUser.setId("1");
        existingUser.setName("Old Name");
        existingUser.setLogin("old@email.com");
        existingUser.setPassword("oldPassword");

        UserDTO userDTO = new UserDTO("New Name", "new@email.com", "newPassword", UserRoles.COSTUMER);

        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.updateUser("1", userDTO);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@email.com", updatedUser.getLogin());
        assertEquals("encodedNewPassword", updatedUser.getPassword());

        verify(userRepository, times(1)).save(existingUser);
        verify(bCryptPasswordEncoder, times(1)).encode("newPassword");
    }

    @Test
    @DisplayName("Deve deletar um usuário pelo ID com sucesso")
    void deleteUserShouldDeleteUserWhenExists() {
        String id = "789";
        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(userRepository).deleteById(id);

        boolean result = userService.deleteUser(id);

        assertTrue(result);
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve retornar falso ao tentar deletar usuário inexistente")
    void deleteUserShouldReturnFalseWhenUserDoesNotExist() {
        String id = "999";
        when(userRepository.existsById(id)).thenReturn(false);

        boolean result = userService.deleteUser(id);

        assertFalse(result);
        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(any());
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