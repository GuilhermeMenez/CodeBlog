package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID testUserId;
    private UserResponseDTO testUserResponseDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserResponseDTO = UserResponseDTO.builder()
                .id(testUserId)
                .name("testuser")
                .login("testuser@email.com")
                .urlProfilePic("https://pic.jpg")
                .followersCount(10L)
                .followingCount(5L)
                .build();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUserShouldSucceed() {
        doNothing().when(userService).deleteUser(testUserId);

        assertDoesNotThrow(() -> userController.deleteUser(testUserId));
        verify(userService, times(1)).deleteUser(testUserId);
    }

    @Test
    @DisplayName("Should return user when user exists by id")
    void findUserByIdShouldReturnUserWhenUserExists() {
        when(userService.findUserById(testUserId)).thenReturn(testUserResponseDTO);

        UserResponseDTO result = userController.findUserById(testUserId);

        assertNotNull(result);
        assertEquals("testuser", result.name());
        assertEquals("testuser@email.com", result.login());
        assertEquals(10L, result.followersCount());
        assertEquals(5L, result.followingCount());
        verify(userService, times(1)).findUserById(testUserId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user does not exist by id")
    void findUserByIdShouldThrowWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userService.findUserById(id))
                .thenThrow(new EntityNotFoundException("User not found with id: " + id));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userController.findUserById(id));

        assertEquals("User not found with id: " + id, exception.getMessage());
        verify(userService, times(1)).findUserById(id);
    }

    @Test
    @DisplayName("Should update user and return updated user")
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        UpdateUserRequestDTO updateDTO = new UpdateUserRequestDTO("updateduser", "test@example.com", "newpassword", null);
        UpdateUserResponseDTO updatedResponse = new UpdateUserResponseDTO("updateduser", "test@example.com");
        when(userService.updateUser(testUserId, updateDTO)).thenReturn(updatedResponse);

        UpdateUserResponseDTO response = userController.updateUser(testUserId, updateDTO);

        assertNotNull(response);
        assertEquals("updateduser", response.name());
        assertEquals("test@example.com", response.email());
        verify(userService, times(1)).updateUser(testUserId, updateDTO);
    }

    @Test
    @DisplayName("Should return followers page successfully")
    void getFollowersShouldReturnPageOfFollowers() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        UserFollowDTO follower1 = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Follower 1")
                .login("follower1@email.com")
                .urlProfilePic(null)
                .build();
        UserFollowDTO follower2 = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Follower 2")
                .login("follower2@email.com")
                .urlProfilePic("https://pic.jpg")
                .build();
        PageResponseDTO<UserFollowDTO> followersPage = PageResponseDTO.<UserFollowDTO>builder()
                .content(List.of(follower1, follower2))
                .currentPage(0)
                .totalPages(1)
                .totalElements(2)
                .size(10)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(userService.getFollowers(testUserId, pageable)).thenReturn(followersPage);

        PageResponseDTO<UserFollowDTO> result = userController.getFollowers(testUserId, page, size);

        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals("Follower 1", result.content().get(0).name());
        assertEquals("Follower 2", result.content().get(1).name());
        verify(userService, times(1)).getFollowers(testUserId, pageable);
    }

    @Test
    @DisplayName("Should return following page successfully")
    void getFollowingShouldReturnPageOfFollowing() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        UserFollowDTO following1 = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Following 1")
                .login("following1@email.com")
                .urlProfilePic(null)
                .build();
        UserFollowDTO following2 = UserFollowDTO.builder()
                .id(UUID.randomUUID())
                .name("Following 2")
                .login("following2@email.com")
                .urlProfilePic("https://pic.jpg")
                .build();
        PageResponseDTO<UserFollowDTO> followingPage = PageResponseDTO.<UserFollowDTO>builder()
                .content(List.of(following1, following2))
                .currentPage(0)
                .totalPages(1)
                .totalElements(2)
                .size(10)
                .first(true)
                .last(true)
                .empty(false)
                .build();

        when(userService.getFollowing(testUserId, pageable)).thenReturn(followingPage);

        PageResponseDTO<UserFollowDTO> result = userController.getFollowing(testUserId, page, size);

        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals("Following 1", result.content().get(0).name());
        assertEquals("Following 2", result.content().get(1).name());
        verify(userService, times(1)).getFollowing(testUserId, pageable);
    }

}
