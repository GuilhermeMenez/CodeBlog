package blog.code.codeblog.mapper;

import blog.code.codeblog.command.user.*;
import blog.code.codeblog.dto.follow.FollowUnfollowRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserRequestDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.model.User;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UserMapper {

    public UpdateUserCommand toUpdateUserCommand(UpdateUserRequestDTO request, User currentUser) {
        return UpdateUserCommand.builder()
                .userId(currentUser.getId())
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .build();
    }

    public DeleteUserCommand toDeleteUserCommand(User currentUser) {
        return DeleteUserCommand.builder()
                .userId(currentUser.getId())
                .build();
    }

    public FollowCommand toFollowCommand(FollowUnfollowRequestDTO request, User currentUser) {
        return FollowCommand.builder()
                .followerId(currentUser.getId())
                .followedId(request.followedId())
                .build();
    }

    public UnfollowCommand toUnfollowCommand(FollowUnfollowRequestDTO request, User currentUser) {
        return UnfollowCommand.builder()
                .followerId(currentUser.getId())
                .followedId(request.followedId())
                .build();
    }

    public GetFollowersCommand toGetFollowersCommand(UUID userId, int page, int size) {
        return GetFollowersCommand.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .build();
    }

    public GetFollowingCommand toGetFollowingCommand(UUID userId, int page, int size) {
        return GetFollowingCommand.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .build();
    }

    public UserResponseDTO toUserResponseDTO(User user, long followersCount, long followingCount) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getLogin(),
                user.getUrlProfilePic(),
                followersCount,
                followingCount
        );
    }

    public UpdateUserResponseDTO toUpdateUserResponseDTO(User user) {
        return UpdateUserResponseDTO.builder()
                .name(user.getName())
                .email(user.getLogin())
                .build();
    }

    public UserFollowDTO toUserFollowDTO(User user) {
        return UserFollowDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .urlProfilePic(user.getUrlProfilePic())
                .build();
    }
    public User toUserEntity(CreateUserCommand command) {
        return new User(command.getName(), command.getEmail(), command.getCredential());
    }
}
