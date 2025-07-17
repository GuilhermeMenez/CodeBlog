package blog.code.codeblog.controller;

import blog.code.codeblog.dto.FollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;



    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());

    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> findUserById(@PathVariable("id") String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



    @PutMapping("/user/edit/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody @Valid UserDTO user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(user.name());
                    existingUser.setLogin(user.email());
                    existingUser.setPassword(new BCryptPasswordEncoder().encode(user.password()));
                    userRepository.save(existingUser);
                    return ResponseEntity.ok(existingUser);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody @Valid FollowRequestDTO followRequestDTO) {
        if (followRequestDTO.followedId().equals(followRequestDTO.followerId())) {
            return ResponseEntity.badRequest().body("Users cannot follow themselves");
        }

        return userRepository.findById(followRequestDTO.followedId())
                .flatMap(follower -> userRepository.findById(followRequestDTO.followerId())
                        .map(followed -> {
                            followed.addFollower(follower);
                            userRepository.save(followed);
                            return ResponseEntity.ok().body("User followed successfully");
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestBody @Valid FollowRequestDTO followRequestDTO) {
        if (followRequestDTO.followedId().equals(followRequestDTO.followerId())) {
            return ResponseEntity.badRequest().body("Users cannot unfollow themselves");
        }
        return userRepository.findById(followRequestDTO.followedId())
                .flatMap(follower -> userRepository.findById(followRequestDTO.followerId())
                        .map(followed -> {
                            followed.removeFollower(follower);
                            userRepository.save(followed);
                            return ResponseEntity.ok().body("User unfollowed successfully");
                        }))
                .orElse(ResponseEntity.notFound().build());
    }
//todo implementar o unfollow
    
}