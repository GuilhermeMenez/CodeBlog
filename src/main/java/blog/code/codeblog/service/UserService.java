package blog.code.codeblog.service;

import blog.code.codeblog.dto.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    public Optional<User> findById(String userId){
        return userRepository.findById(userId);
    }

    public User findByLogin(String login){
        return userRepository.findByLogin(login);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public Optional<User> updateUser(String id, UserDTO user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(user.name());
                    existingUser.setLogin(user.email());
                    existingUser.setPassword(bCryptPasswordEncoder.encode(user.password()));
                    return userRepository.save(existingUser);
                });
    }

    public boolean deleteUser(String userId){
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public boolean handleFollowUnfollow(FollowUnfollowRequestDTO followUnfollowRequestDTO, boolean isFollow){
        if (followUnfollowRequestDTO.followedId().equals(followUnfollowRequestDTO.followerId())) {
            return false;
        }

        userRepository.findById(followUnfollowRequestDTO.followerId())
                .flatMap(follower -> userRepository.findById(followUnfollowRequestDTO.followedId())
                        .map(followed -> {
                            if (isFollow) {
                                followed.addFollower(follower);
                            } else {
                                followed.removeFollower(follower);
                            }
                            userRepository.save(followed);
                            return true;

                        }));
        return false;
    }

    public User getReference(String id){
        return userRepository.getReferenceById(id);
    }

}
