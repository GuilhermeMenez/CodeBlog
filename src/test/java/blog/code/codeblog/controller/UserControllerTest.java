package blog.code.codeblog.controller;

import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        UUID userId = UUID.randomUUID();
        testUser.setId(userId);
        testUser.setName("testuser");
    }

    @Test
    @DisplayName("Should return OK when deleting an existing user")
    void deleteUserShouldReturnOkWhenUserExists() {
        UUID id = testUser.getId();
        doNothing().when(userService).deleteUser(id);

        assertDoesNotThrow(() -> userController.deleteUser(id));
        verify(userService, times(1)).deleteUser(id);
    }

    @Test
    @DisplayName("Should return user when user exists by id")
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
    @DisplayName("Should return NOT_FOUND when user does not exist by id")
    void findUserByIdShouldReturnNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.findUserById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should update user and return updated user")
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        UUID id = testUser.getId();
        UpdateUserRequestDTO updateDTO = new UpdateUserRequestDTO("updateduser", "test@example.com", "newpassword");
        UpdateUserResponseDTO updatedResponse = new UpdateUserResponseDTO("updateduser", "test@example.com");
        when(userService.updateUser(id, updateDTO)).thenReturn(updatedResponse);

        UpdateUserResponseDTO response = userController.updateUser(id, updateDTO);

        assertNotNull(response);
        assertEquals("updateduser", response.name());
        assertEquals("test@example.com", response.email());
        verify(userService, times(1)).updateUser(id, updateDTO);
    }

}
