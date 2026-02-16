package blog.code.codeblog.service;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.model.UserFollow;
import blog.code.codeblog.repository.UserFollowRepository;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserFollowRepository userFollowRepository;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;


    public Optional<User> findById(UUID id) {
        log.info("[findById] Finding user by id: {}", id);
        return userRepository.findById(id);
    }
    @Cacheable("users")
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
                .followersCount(userFollowRepository.countFollowersByUserId(user.getId()))
                .followingCount(userFollowRepository.countFollowingByUserId(user.getId()))
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



    @Transactional
    public void follow(UUID followerId, UUID followedId) {
        log.info("[follow] Attempting to follow user. followerId: {}, followedId: {}", followerId, followedId);

        validateNotSameUser(followerId, followedId);

        User follower = findUserOrThrow(followerId);
        User followed = findUserOrThrow(followedId);

        UserFollow userFollow = UserFollow.builder()
                .follower(follower)
                .followed(followed)
                .build();

        try {
            userFollowRepository.save(userFollow);
        } catch (DataIntegrityViolationException e) {
            log.warn("[follow] User already follows this user. followerId: {}, followedId: {}", followerId, followedId);
            throw new IllegalStateException("User already follows this user");
        }

        log.info("[follow] Follow operation successful. followerId: {}, followedId: {}", followerId, followedId);
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followedId) {
        log.info("[unfollow] Attempting to unfollow user. followerId: {}, followedId: {}", followerId, followedId);

        validateNotSameUser(followerId, followedId);

        if (!userFollowRepository.existsByFollower_IdAndFollowed_Id(followerId, followedId)) {
            log.warn("[unfollow] User does not follow this user. followerId: {}, followedId: {}", followerId, followedId);
            throw new IllegalStateException("User does not follow this user");
        }

        userFollowRepository.deleteByFollower_IdAndFollowed_Id(followerId, followedId);
        log.info("[unfollow] Unfollow operation successful. followerId: {}, followedId: {}", followerId, followedId);
    }

    private void validateNotSameUser(UUID followerId, UUID followedId) {
        if (followedId.equals(followerId)) {
            log.warn("[validateNotSameUser] Follower and followed are the same user. id: {}", followerId);
            throw new IllegalArgumentException("Cannot follow yourself");
        }
    }

    private User findUserOrThrow(UUID userId ) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[findUserOrThrow] user not found. id: {}", userId);
                    return new EntityNotFoundException(userId + " not found");
                });
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

        Page<User> followersPage = userFollowRepository.findFollowersByUserId(userId, pageable);

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

        Page<User> followingPage = userFollowRepository.findFollowingByUserId(userId, pageable);

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
