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
        testUser.setId("1");
        testUser.setName("testuser");

        testUserDTO = new UserDTO("updateduser", "test@example.com", "newpassword", UserRoles.COSTUMER);

        testFollowDTO = new FollowUnfollowRequestDTO("follower1", "followed1", true);
    }

    @Test
    @DisplayName("Testa se o usuário existente foi deletado")
    void deleteUserShouldReturnOkWhenUserExists() {
        when(userService.deleteUser("1")).thenReturn(true);

        ResponseEntity<?> response = userController.deleteUser("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteUser("1");
    }

    @Test
    @DisplayName("Testa se e possivel deletar um usuario inexistente")
    void deleteUserShouldReturnNotFoundWhenUserDoesNotExist() {
        when(userService.deleteUser("999")).thenReturn(false);

        ResponseEntity<?> response = userController.deleteUser("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).deleteUser("999");
    }

    @Test
    @DisplayName("Testa buscar por id, um usuário existente")
    void findUserByIdShouldReturnUserWhenUserExists() {
        when(userService.findById("1")).thenReturn(Optional.of(testUser));

        ResponseEntity<User> response = userController.findUserById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getName()); // corrigido aqui
        verify(userService, times(1)).findById("1");
    }


    @Test
    @DisplayName("Testa buscar por id, um usuário inexistente ")
    void findUserByIdShouldReturnNotFoundWhenUserDoesNotExist() {
        when(userService.findById("999")).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.findUserById("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).findById("999");
    }

    @Test
    @DisplayName("Testa buscar por id, um usuário existente alterado")
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setName("updateduser");

        when(userService.updateUser("1", testUserDTO)).thenReturn(Optional.of(updatedUser));

        ResponseEntity<User> response = userController.updateUser("1", testUserDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updateduser", response.getBody().getName());
        verify(userService, times(1)).updateUser("1", testUserDTO);
    }

    @Test
    @DisplayName("Tenta atualizar um usuario inexistente")
    void updateUserShouldReturnNotFoundWhenUserDoesNotExist() {
        when(userService.updateUser("999", testUserDTO)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.updateUser("999", testUserDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).updateUser("999", testUserDTO);
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
        FollowUnfollowRequestDTO selfFollowDTO = new FollowUnfollowRequestDTO("same", "same", true);

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
        FollowUnfollowRequestDTO selfUnfollowDTO = new FollowUnfollowRequestDTO("same", "same",false);

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