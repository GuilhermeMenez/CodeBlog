package blog.code.codeblog.service;

import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder bCryptPasswordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void findByIdSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void findByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        Optional<User> result = userService.findById(id);
        assertFalse(result.isPresent());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Should find user by login successfully")
    void findByLoginSuccess() {
        User user = new User();
        user.setLogin("email@email.com");
        when(userRepository.findByLogin("email@email.com")).thenReturn(user);
        User result = userService.findByLogin("email@email.com");
        assertNotNull(result);
        assertEquals("email@email.com", result.getLogin());
        verify(userRepository).findByLogin("email@email.com");
    }

    @Test
    @DisplayName("Should return null when user not found by login")
    void findByLoginNotFound() {
        when(userRepository.findByLogin("notfound@email.com")).thenReturn(null);
        User result = userService.findByLogin("notfound@email.com");
        assertNull(result);
        verify(userRepository).findByLogin("notfound@email.com");
    }

    @Test
    @DisplayName("Should save user successfully")
    void saveUserSuccess() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);
        userService.saveUser(user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUserSuccess() {
        UUID id = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setName("Old Name");
        existingUser.setLogin("old@email.com");
        existingUser.setPassword("oldPassword");
        UpdateUserRequestDTO updateUserDTO = new UpdateUserRequestDTO("New Name", "new@email.com", "newPassword", null);
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateUserResponseDTO response = userService.updateUser(id, updateUserDTO);
        assertEquals("New Name", response.name());
        assertEquals("new@email.com", response.email());
        verify(userRepository).findById(id);
        verify(bCryptPasswordEncoder).encode("newPassword");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void updateUserNotFound() {
        UUID id = UUID.randomUUID();
        UpdateUserRequestDTO updateUserDTO = new UpdateUserRequestDTO("Name", "email@email.com", "password", null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(id, updateUserDTO));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUserSuccess() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(userRepository).deleteById(id);
        userService.deleteUser(id);
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void deleteUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(id));
        assertEquals("User not found with id: " + id, exception.getMessage());
        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should follow user successfully")
    void handleFollowUserSuccess() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        FollowUnfollowRequestDTO dto = new FollowUnfollowRequestDTO(followerId, followedId, true);
        User follower = mock(User.class);
        User followed = mock(User.class);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
        when(userRepository.save(followed)).thenReturn(followed);
        boolean result = userService.handleFollowUnfollow(dto, true);
        verify(followed).addFollower(follower);
        verify(userRepository).save(followed);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should unfollow user successfully")
    void handleUnfollowUserSuccess() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        FollowUnfollowRequestDTO dto = new FollowUnfollowRequestDTO(followerId, followedId, false);
        User follower = mock(User.class);
        User followed = mock(User.class);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
        when(userRepository.save(followed)).thenReturn(followed);
        boolean result = userService.handleFollowUnfollow(dto, false);
        verify(followed).removeFollower(follower);
        verify(userRepository).save(followed);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when follower and followed are the same user")
    void handleFollowUnfollowSameUser() {
        UUID sameId = UUID.randomUUID();
        FollowUnfollowRequestDTO dto = new FollowUnfollowRequestDTO(sameId, sameId, true);
        boolean result = userService.handleFollowUnfollow(dto, true);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when follower not found")
    void handleFollowUnfollowFollowerNotFound() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        FollowUnfollowRequestDTO dto = new FollowUnfollowRequestDTO(followerId, followedId, true);
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());
        boolean result = userService.handleFollowUnfollow(dto, true);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when followed not found")
    void handleFollowUnfollowFollowedNotFound() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        FollowUnfollowRequestDTO dto = new FollowUnfollowRequestDTO(followerId, followedId, true);
        User follower = mock(User.class);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.empty());
        boolean result = userService.handleFollowUnfollow(dto, true);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should get user reference by ID")
    void getReferenceSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.getReferenceById(id)).thenReturn(user);
        User result = userService.getReference(id);
        assertEquals(user, result);
        verify(userRepository).getReferenceById(id);
    }

    @Test
    @DisplayName("Should save upload profile pic successfully")
    void saveUploadProfilePicSuccess() {
        UUID userId = UUID.randomUUID();
        String profilePicUrl = "https://cloudinary.com/profile.jpg";
        String profilePicId = "profile_pics/test-image";
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setLogin("user@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.saveUploadProfilePic(userId, profilePicUrl, profilePicId);

        assertNotNull(result);
        assertEquals("Profile pic updated successfully", result.message());
        assertEquals(profilePicUrl, result.imageUrl());
        assertEquals(profilePicId, result.publicId());
        assertEquals(profilePicUrl, existingUser.getUrlProfilePic());
        assertEquals(profilePicId, existingUser.getProfilePicId());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when saving profile pic for non-existent user")
    void saveUploadProfilePicUserNotFound() {
        UUID userId = UUID.randomUUID();
        String profilePicUrl = "https://cloudinary.com/profile.jpg";
        String profilePicId = "profile_pics/test-image";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> userService.saveUploadProfilePic(userId, profilePicUrl, profilePicId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete profile pic successfully")
    void deleteProfilePicSuccess() {
        String publicId = "profile_pics/test-image";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUrlProfilePic("https://cloudinary.com/profile.jpg");
        user.setProfilePicId(publicId);

        when(userRepository.findByProfilePicId(publicId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.deleteProfilePic(publicId);

        assertTrue(result);
        assertNull(user.getUrlProfilePic());
        assertNull(user.getProfilePicId());
        verify(userRepository).findByProfilePicId(publicId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should return false when profile pic not found for deletion")
    void deleteProfilePicNotFound() {
        String publicId = "profile_pics/nonexistent-image";

        when(userRepository.findByProfilePicId(publicId)).thenReturn(Optional.empty());

        boolean result = userService.deleteProfilePic(publicId);

        assertFalse(result);
        verify(userRepository).findByProfilePicId(publicId);
        verify(userRepository, never()).save(any());
    }
}