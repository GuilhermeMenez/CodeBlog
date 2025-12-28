package blog.code.codeblog.service;

import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<User> findById(UUID userId){
        log.info("[findById] Attempting to find user with id: {}", userId);
        return userRepository.findById(userId);
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
        return new UpdateUserResponseDTO(existingUser.getName(), existingUser.getLogin());
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

}
