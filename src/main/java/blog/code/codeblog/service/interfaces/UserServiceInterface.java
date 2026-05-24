package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserServiceInterface {
    Optional<User> findById(UUID id);
    UserResponseDTO findUserById(UUID id);
    User findByLogin(String login);
    void saveUser(User user);
    UpdateUserResponseDTO updateUser(UUID id, UpdateUserRequestDTO updatedUser);
    void deleteUser(UUID userId);
    ImageUploadResponseDTO saveUploadProfilePic(UUID userId, String profilePicUrl, String profilePicId) throws EntityNotFoundException;
    void follow(UUID followerId, UUID followedId);
    void unfollow(UUID followerId, UUID followedId);
    User getReference(UUID id);
    boolean deleteProfilePic(String publicId);
    PageResponseDTO<UserFollowDTO> getFollowers(UUID userId, Pageable pageable);
    PageResponseDTO<UserFollowDTO> getFollowing(UUID userId, Pageable pageable);
    UserResponseDTO getUserInformation(String token);
}

