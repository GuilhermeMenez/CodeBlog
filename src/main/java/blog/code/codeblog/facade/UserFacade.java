package blog.code.codeblog.facade;

import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.mapper.UserMapper;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserFacade {
    @Autowired
    UserService userService;

    public UserResponseDTO findUserById(UUID id) {
        return userService.findUserById(id);
    }

    public UpdateUserResponseDTO updateUser(UpdateUserRequestDTO user, User currentUser) {
        return userService.updateUser(UserMapper.toUpdateUserCommand(user, currentUser));
    }

    public void deleteUser(User currentUser) {
        userService.deleteUser(UserMapper.toDeleteUserCommand(currentUser));
    }

    public void follow(FollowUnfollowRequestDTO request, User currentUser) {
        userService.follow(UserMapper.toFollowCommand(request, currentUser));
    }

    public void unfollow(FollowUnfollowRequestDTO request, User currentUser) {
        userService.unfollow(UserMapper.toUnfollowCommand(request, currentUser));
    }

    public PageResponseDTO<UserFollowDTO> getFollowers(UUID userId, int page, int size) {
        return userService.getFollowers(UserMapper.toGetFollowersCommand(userId, page, size));
    }

    public PageResponseDTO<UserFollowDTO> getFollowing(UUID userId, int page, int size) {
        return userService.getFollowing(UserMapper.toGetFollowingCommand(userId, page, size));
    }

    public UserResponseDTO getUserInformation(User currentUser) {
        return userService.findUserById(currentUser.getId());
    }

}
