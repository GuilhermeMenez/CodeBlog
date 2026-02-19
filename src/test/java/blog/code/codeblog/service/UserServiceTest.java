package blog.code.codeblog.service;

import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.model.UserFollow;
import blog.code.codeblog.repository.UserFollowRepository;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
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

        UpdateUserRequestDTO updateUserDTO =
                new UpdateUserRequestDTO("New Name", "new@email.com", "newPassword", null);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        UpdateUserResponseDTO response = userService.updateUser(id, updateUserDTO);

        assertEquals("New Name", response.name());
        assertEquals("new@email.com", response.email());

        verify(userRepository).findById(id);
        verify(bCryptPasswordEncoder).encode("newPassword");

        // NÃO verificar save()
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
    void followUserSuccess() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        User follower = new User();
        follower.setId(followerId);
        User followed = new User();
        followed.setId(followedId);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
        when(userFollowRepository.save(any(UserFollow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> userService.follow(followerId, followedId));

        verify(userFollowRepository).save(any(UserFollow.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when already following")
    void followUserAlreadyFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        User follower = new User();
        follower.setId(followerId);
        User followed = new User();
        followed.setId(followedId);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
        when(userFollowRepository.save(any(UserFollow.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate entry"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.follow(followerId, followedId));
        assertEquals("User already follows this user", exception.getMessage());
    }

    @Test
    @DisplayName("Should unfollow user successfully")
    void unfollowUserSuccess() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(userFollowRepository
                .deleteByFollower_IdAndFollowed_Id(followerId, followedId))
                .thenReturn(1);

        assertDoesNotThrow(() -> userService.unfollow(followerId, followedId));

        verify(userFollowRepository)
                .deleteByFollower_IdAndFollowed_Id(followerId, followedId);
    }




    @Test
    @DisplayName("Should throw IllegalStateException when not following")
    void unfollowUserNotFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(userFollowRepository.existsByFollower_IdAndFollowed_Id(followerId, followedId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.unfollow(followerId, followedId));
        assertEquals("User does not follow this user", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when follower and followed are the same user")
    void followSameUserThrowsException() {
        UUID sameId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.follow(sameId, sameId));
        assertEquals("Cannot follow yourself", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when follower not found")
    void followFollowerNotFound() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.follow(followerId, followedId));
        assertEquals(followerId + " not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when followed not found")
    void followFollowedNotFound() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        User follower = new User();
        follower.setId(followerId);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.follow(followerId, followedId));
        assertEquals(followedId + " not found", exception.getMessage());
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

    @Test
    @DisplayName("Should get followers successfully with pagination")
    void getFollowersSuccess() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        User follower1 = new User();
        follower1.setId(UUID.randomUUID());
        follower1.setName("Follower 1");
        follower1.setLogin("follower1@email.com");
        follower1.setUrlProfilePic("https://cloudinary.com/pic1.jpg");

        User follower2 = new User();
        follower2.setId(UUID.randomUUID());
        follower2.setName("Follower 2");
        follower2.setLogin("follower2@email.com");

        Page<User> followersPage = new PageImpl<>(List.of(follower1, follower2), pageable, 2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowersByUserId(userId, pageable)).thenReturn(followersPage);

        var result = userService.getFollowers(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.totalElements());
        verify(userRepository).existsById(userId);
        verify(userFollowRepository).findFollowersByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getting followers for non-existent user")
    void getFollowersUserNotFound() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> userService.getFollowers(userId, pageable));

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("Should get following successfully with pagination")
    void getFollowingSuccess() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        User following1 = new User();
        following1.setId(UUID.randomUUID());
        following1.setName("Following 1");
        following1.setLogin("following1@email.com");
        following1.setUrlProfilePic("https://cloudinary.com/pic1.jpg");

        User following2 = new User();
        following2.setId(UUID.randomUUID());
        following2.setName("Following 2");
        following2.setLogin("following2@email.com");

        Page<User> followingPage = new PageImpl<>(List.of(following1, following2), pageable, 2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userFollowRepository.findFollowingByUserId(userId, pageable)).thenReturn(followingPage);

        var result = userService.getFollowing(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.totalElements());
        verify(userRepository).existsById(userId);
        verify(userFollowRepository).findFollowingByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getting following for non-existent user")
    void getFollowingUserNotFound() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> userService.getFollowing(userId, pageable));

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).existsById(userId);
    }
}