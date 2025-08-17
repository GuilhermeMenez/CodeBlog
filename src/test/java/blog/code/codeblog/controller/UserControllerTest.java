package blog.code.codeblog.controller;

import blog.code.codeblog.dto.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDTO testUserDTO;
    private FollowUnfollowRequestDTO testFollowDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        UUID userId = UUID.randomUUID();
        testUser.setId(userId);
        testUser.setName("testuser");

        testUserDTO = new UserDTO("updateduser", "test@example.com", "newpassword", UserRoles.COSTUMER);

        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        testFollowDTO = new FollowUnfollowRequestDTO(followerId, followedId, true);
    }

    @Test
    @DisplayName("Testa se o usuário existente foi deletado")
    void deleteUserShouldReturnOkWhenUserExists() {
        UUID id = testUser.getId();
        when(userService.deleteUser(id)).thenReturn(true);

        ResponseEntity<?> response = userController.deleteUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteUser(id);
    }

    @Test
    @DisplayName("Testa se e possivel deletar um usuario inexistente")
    void deleteUserShouldReturnNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(false);

        ResponseEntity<?> response = userController.deleteUser(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).deleteUser(id);
    }

    @Test
    @DisplayName("Testa buscar por id, um usuário existente")
    void findUserByIdShouldReturnUserWhenUserExists() {
        UUID id = testUser.getId();
        when(userService.findById(id)).thenReturn(Optional.of(testUser));

        ResponseEntity<User> response = userController.findUserById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getName());
        verify(userService, times(1)).findById(id);
    }

    @Test
    @DisplayName("Testa buscar por id, um usuário inexistente ")
    void findUserByIdShouldReturnNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.findUserById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).findById(id);
    }

    @Test
    @DisplayName("Testa buscar por id, um usuário existente alterado")
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        UUID id = testUser.getId();
        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setName("updateduser");

        when(userService.updateUser(id, testUserDTO)).thenReturn(Optional.of(updatedUser));

        ResponseEntity<User> response = userController.updateUser(id, testUserDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updateduser", response.getBody().getName());
        verify(userService, times(1)).updateUser(id, testUserDTO);
    }

    @Test
    @DisplayName("Tenta atualizar um usuario inexistente")
    void updateUserShouldReturnNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(id, testUserDTO)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.updateUser(id, testUserDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).updateUser(id, testUserDTO);
    }

    @Test
    @DisplayName("Testa seguir um usurio")
    void followShouldReturnOkWhenValidRequest() {
        when(userService.handleFollowUnfollow(testFollowDTO, true)).thenReturn(true);

        ResponseEntity<?> response = userController.follow(testFollowDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).handleFollowUnfollow(testFollowDTO, true);
    }

    @Test
    @DisplayName("Testa impedir o autofollow")
    void followShouldReturnBadRequestWhenSelfFollow() {
        UUID sameId = UUID.randomUUID();
        FollowUnfollowRequestDTO selfFollowDTO = new FollowUnfollowRequestDTO(sameId, sameId, true);

        ResponseEntity<?> response = userController.follow(selfFollowDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Usuários não podem seguir a si mesmos", response.getBody());
        verify(userService, never()).handleFollowUnfollow(any(), anyBoolean());
    }

    @Test
    @DisplayName("Testa seguir um usuario inexistente")
    void followShouldReturnBadRequestWhenUserNotFound() {
        when(userService.handleFollowUnfollow(testFollowDTO, true)).thenReturn(false);

        ResponseEntity<?> response = userController.follow(testFollowDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Usuário não encontrado", response.getBody());
        verify(userService, times(1)).handleFollowUnfollow(testFollowDTO, true);
    }

    @Test
    @DisplayName("Testa o unfollow")
    void unfollowShouldReturnOkWhenValidRequest() {
        when(userService.handleFollowUnfollow(testFollowDTO, false)).thenReturn(true);

        ResponseEntity<?> response = userController.unfollow(testFollowDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).handleFollowUnfollow(testFollowDTO, false);
    }

    @Test
    @DisplayName("Testa impedir o autounfollow")
    void unfollowShouldReturnBadRequestWhenSelfUnfollow() {
        UUID sameId = UUID.randomUUID();
        FollowUnfollowRequestDTO selfUnfollowDTO = new FollowUnfollowRequestDTO(sameId, sameId, false);

        ResponseEntity<?> response = userController.unfollow(selfUnfollowDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Usuários não podem deixar de seguir a si mesmos", response.getBody());
        verify(userService, never()).handleFollowUnfollow(any(), anyBoolean());
    }

    @Test
    @DisplayName("Testa o autofollow em um usuário inexistente ")
    void unfollowShouldReturnBadRequestWhenUserNotFound() {
        when(userService.handleFollowUnfollow(testFollowDTO, false)).thenReturn(false);

        ResponseEntity<?> response = userController.unfollow(testFollowDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Usuário não encontrado", response.getBody());
        verify(userService, times(1)).handleFollowUnfollow(testFollowDTO, false);
    }

}