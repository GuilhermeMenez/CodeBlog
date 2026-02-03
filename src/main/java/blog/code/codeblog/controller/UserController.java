package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO findUserById(@PathVariable("id") UUID id) {
        log.info("Get user by id request received for user {}", id);
        return userService.findUserById(id);
    }

    @PutMapping("/user/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UpdateUserResponseDTO updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UpdateUserRequestDTO user) {
        log.info("Update user request received for user {}", id);
        return userService.updateUser(id, user);
    }

    @PostMapping("/follow")
    @ResponseStatus(HttpStatus.OK)
    public void follow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        log.info("Follow request received. followerId: {}, followedId: {}", dto.followerId(), dto.followedId());
        processFollowAction(dto, true, "Users cannot follow themselves");
    }

    @PostMapping("/unfollow")
    @ResponseStatus(HttpStatus.OK)
    public void unfollow(@RequestBody @Valid FollowUnfollowRequestDTO dto) {
        log.info("Unfollow request received. followerId: {}, followedId: {}", dto.followerId(), dto.followedId());
        processFollowAction(dto, false, "Users cannot unfollow themselves");
    }

    private void processFollowAction(FollowUnfollowRequestDTO dto, boolean isFollow, String selfActionMessage) {
        if (dto.followedId().equals(dto.followerId())) {
            throw new IllegalArgumentException(selfActionMessage);
        }
        if (!userService.handleFollowUnfollow(dto, isFollow)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    @GetMapping("/user/{id}/followers")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<UserFollowDTO> getFollowers(
            @PathVariable("id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get followers request received for user {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userService.getFollowers(userId, pageable);
    }

    @GetMapping("/user/{id}/following")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<UserFollowDTO> getFollowing(
            @PathVariable("id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get following request received for user {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userService.getFollowing(userId, pageable);
    }

}