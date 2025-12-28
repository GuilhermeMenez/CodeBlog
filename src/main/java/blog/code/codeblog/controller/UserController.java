package blog.code.codeblog.controller;

import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @DeleteMapping("/deleteUser/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("id") UUID userId) {
        log.info("Delete user request received for user {}", userId);
      userService.deleteUser(userId);

    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> findUserById(@PathVariable("id") UUID id) {
        log.info("Get user by id request received for user {}", id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/user/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UpdateUserResponseDTO updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UpdateUserRequestDTO user) {
        log.info("Update user request received for user {}", id);
        return userService.updateUser(id, user);
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        return processFollowAction(dto, true, "Users cannot follow themselves");
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        return processFollowAction(dto, false, "Users cannot unfollow themselves");
    }

    private ResponseEntity<?> processFollowAction(FollowUnfollowRequestDTO dto, boolean isFollow, String selfActionMessage) {
        if (dto.followedId().equals(dto.followerId())) {
            return ResponseEntity.badRequest().body(selfActionMessage);
        }
        if (userService.handleFollowUnfollow(dto, isFollow))
            return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("User not found");
    }
}