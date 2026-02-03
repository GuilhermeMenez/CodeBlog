package blog.code.codeblog.service;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    public Optional<User> findById(UUID id) {
        log.info("[findById] Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    public UserResponseDTO findUserById(UUID id) {
        log.info("[findByIdAsDTO] Finding user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[findByIdAsDTO] User not found. id: {}", id);
                    return new EntityNotFoundException("User not found with id: " + id);
                });
        return convertToUserResponseDTO(user);
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .urlProfilePic(user.getUrlProfilePic())
                .followersCount(user.getFollowers() != null ? user.getFollowers().size() : 0)
                .followingCount(user.getFollowing() != null ? user.getFollowing().size() : 0)
                .build();
    }

    public User findByLogin(String login){
        log.info("[findByLogin] Attempting to find user by login: {}", login);
        return userRepository.findByLogin(login);
    }

    public void saveUser(User user){
        log.info("[saveUser] Saving user with login: {}", user.getLogin());
        userRepository.save(user);
        log.info("[saveUser] User saved successfully. login: {}", user.getLogin());
    }

    public UpdateUserResponseDTO updateUser(UUID id, UpdateUserRequestDTO updatedUser) {
        log.info("[updateUser] Attempting to update user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[updateUser] User not found. id: {}", id);
                    return new EntityNotFoundException("User not found");
                });

        existingUser.setName(updatedUser.name());
        existingUser.setLogin(updatedUser.email());
        existingUser.setPassword(bCryptPasswordEncoder.encode(updatedUser.password()));

        userRepository.save(existingUser);
        log.info("[updateUser] User updated successfully. id: {}", id);
        return UpdateUserResponseDTO.builder()
                .name(existingUser.getName())
                .email(existingUser.getLogin())
                .build();
    }

    public void deleteUser(UUID userId){
        log.info("[deleteUser] Attempting to delete user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("[deleteUser] User not found for deletion. id: {}", userId);
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("[deleteUser] User deleted successfully. id: {}", userId);
    }

    public ImageUploadResponseDTO saveUploadProfilePic(UUID userId, String profilePicUrl, String profilePicId) throws EntityNotFoundException {
        log.info("[updateProfilePic] Attempting to update profile pic for user with id: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[updateProfilePic] User not found. id: {}", userId);
                    return new EntityNotFoundException("User not found");
                });
        existingUser.setUrlProfilePic(profilePicUrl);
        existingUser.setProfilePicId(profilePicId);
        userRepository.save(existingUser);
        log.info("[updateProfilePic] Profile pic updated successfully. id for user: {}", existingUser.getLogin());
        return ImageUploadResponseDTO.builder()
                .message("Profile pic updated successfully")
                .imageUrl(profilePicUrl)
                .publicId(profilePicId)
                .build();
    }



        public boolean handleFollowUnfollow(FollowUnfollowRequestDTO followUnfollowRequestDTO, boolean isFollow){
        log.info("[handleFollowUnfollow] Attempting to {} user. followerId: {}, followedId: {}", isFollow ? "follow" : "unfollow", followUnfollowRequestDTO.followerId(), followUnfollowRequestDTO.followedId());
        if (followUnfollowRequestDTO.followedId().equals(followUnfollowRequestDTO.followerId())) {
            log.warn("[handleFollowUnfollow] Follower and followed are the same user. id: {}", followUnfollowRequestDTO.followerId());
            return false;
        }

        boolean result = userRepository.findById(followUnfollowRequestDTO.followerId())
                .flatMap(follower -> userRepository.findById(followUnfollowRequestDTO.followedId())
                        .map(followed -> {
                            if (isFollow) {
                                followed.addFollower(follower);
                            } else {
                                followed.removeFollower(follower);
                            }
                            userRepository.save(followed);
                            log.info("[handleFollowUnfollow] {} operation successful. followerId: {}, followedId: {}", isFollow ? "Follow" : "Unfollow", followUnfollowRequestDTO.followerId(), followUnfollowRequestDTO.followedId());
                            return true;
                        }))
                .orElse(false);
        if (!result) {
            log.warn("[handleFollowUnfollow] Could not {} user. followerId: {}, followedId: {}", isFollow ? "follow" : "unfollow", followUnfollowRequestDTO.followerId(), followUnfollowRequestDTO.followedId());
        }
        return result;
    }

    public User getReference(UUID id){
        log.info("[getReference] Getting reference for user id: {}", id);
        return userRepository.getReferenceById(id);
    }

    public boolean deleteProfilePic(String publicId) {
        log.info("[deleteProfilePic] Attempting to delete profile pic with publicId: {}", publicId);
        Optional<User> userOpt = userRepository.findByProfilePicId(publicId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUrlProfilePic(null);
            user.setProfilePicId(null);
            userRepository.save(user);
            log.info("[deleteProfilePic] Profile pic removed from user: {}", user.getId());
            return true;
        }
        return false;
    }

    public PageResponseDTO<UserFollowDTO> getFollowers(UUID userId, Pageable pageable) {
        log.info("[getFollowers] Getting followers for user id: {} (page: {}, size: {})", userId, pageable.getPageNumber(), pageable.getPageSize());

        if (!userRepository.existsById(userId)) {
            log.warn("[getFollowers] User not found. id: {}", userId);
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        Page<User> followersPage = userRepository.findFollowersByUserId(userId, pageable);

        return PageResponseDTO.<UserFollowDTO>builder()
                .content(followersPage.getContent().stream().map(this::convertToUserFollowDTO).toList())
                .currentPage(followersPage.getNumber())
                .totalPages(followersPage.getTotalPages())
                .totalElements(followersPage.getTotalElements())
                .size(followersPage.getSize())
                .first(followersPage.isFirst())
                .last(followersPage.isLast())
                .empty(followersPage.isEmpty())
                .build();
    }

    public PageResponseDTO<UserFollowDTO> getFollowing(UUID userId, Pageable pageable) {
        log.info("[getFollowing] Getting following for user id: {} (page: {}, size: {})", userId, pageable.getPageNumber(), pageable.getPageSize());

        if (!userRepository.existsById(userId)) {
            log.warn("[getFollowing] User not found. id: {}", userId);
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        Page<User> followingPage = userRepository.findFollowingByUserId(userId, pageable);

        return PageResponseDTO.<UserFollowDTO>builder()
                .content(followingPage.getContent().stream().map(this::convertToUserFollowDTO).toList())
                .currentPage(followingPage.getNumber())
                .totalPages(followingPage.getTotalPages())
                .totalElements(followingPage.getTotalElements())
                .size(followingPage.getSize())
                .first(followingPage.isFirst())
                .last(followingPage.isLast())
                .empty(followingPage.isEmpty())
                .build();
    }

    private UserFollowDTO convertToUserFollowDTO(User user) {
        return UserFollowDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .urlProfilePic(user.getUrlProfilePic())
                .build();
    }

}
