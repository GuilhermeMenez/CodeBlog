package blog.code.codeblog.controller;

import blog.code.codeblog.dto.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
public class UserController {

    @Autowired
    UserService userService;

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") UUID userId) {
      if (userService.deleteUser(userId)) {
          return ResponseEntity.ok().build();
      }
      return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> findUserById(@PathVariable("id") UUID id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/user/edit/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserDTO user) {
        return userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        return processFollowAction(dto, true, "Usuários não podem seguir a si mesmos");
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        return processFollowAction(dto, false, "Usuários não podem deixar de seguir a si mesmos");
    }

    private ResponseEntity<?> processFollowAction(FollowUnfollowRequestDTO dto, boolean isFollow, String selfActionMessage) {
        if (dto.followedId().equals(dto.followerId())) {
            return ResponseEntity.badRequest().body(selfActionMessage);
        }
        if (userService.handleFollowUnfollow(dto, isFollow))
            return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("Usuário não encontrado");
    }
}