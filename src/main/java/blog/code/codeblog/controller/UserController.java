package blog.code.codeblog.controller;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;

import blog.code.codeblog.facade.UserFacade;
import blog.code.codeblog.service.provider.UserProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserFacade userFacade;
    @Autowired
    UserProvider userProvider;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser() {
        log.info("Delete user request received for current user");
        userFacade.deleteUser(userProvider.getCurrentUser());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO findUserById(@PathVariable UUID id) {
        log.info("Get user by id request received for user {}", id);
        return userFacade.findUserById(id);
    }

    @PutMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public UpdateUserResponseDTO updateUser(@RequestBody @Valid UpdateUserRequestDTO user) {
        log.info("Update user request received for current user");
        return userFacade.updateUser(user, userProvider.getCurrentUser());
    }

    @PostMapping("/follow")
    @ResponseStatus(HttpStatus.OK)
    public void follow(@RequestBody @Valid FollowUnfollowRequestDTO followUnfollowRequestDTO) {
        log.info("Follow request received. followedId: {}", followUnfollowRequestDTO.followedId());
        userFacade.follow(followUnfollowRequestDTO, userProvider.getCurrentUser());
    }

    @PostMapping("/unfollow")
    @ResponseStatus(HttpStatus.OK)
    public void unfollow(@RequestBody @Valid FollowUnfollowRequestDTO followUnfollowRequestDTO) {
        log.info("Unfollow request received. followedId: {}", followUnfollowRequestDTO.followedId());
        userFacade.unfollow(followUnfollowRequestDTO, userProvider.getCurrentUser());
    }

    @GetMapping("/{id}/followers")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<UserFollowDTO> getFollowers(
            @PathVariable("id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get followers request received for user {} (page: {}, size: {})", userId, page, size);
        return userFacade.getFollowers(userId, page, size);
    }

    @GetMapping("/{id}/following")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<UserFollowDTO> getFollowing(
            @PathVariable("id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get following request received for user {} (page: {}, size: {})", userId, page, size);
        return userFacade.getFollowing(userId, page, size);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getMe() {
        log.info("Get me request received");
        return userFacade.getUserInformation(userProvider.getCurrentUser());
    }
}
