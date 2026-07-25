package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.command.user.DeleteUserCommand;
import blog.code.codeblog.command.user.FollowCommand;
import blog.code.codeblog.command.user.GetFollowersCommand;
import blog.code.codeblog.command.user.GetFollowingCommand;
import blog.code.codeblog.command.user.UnfollowCommand;
import blog.code.codeblog.command.user.UpdateUserCommand;
import blog.code.codeblog.dto.PageResponseDTO;
import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.dto.user.UpdateUserResponseDTO;
import blog.code.codeblog.dto.user.UserFollowDTO;
import blog.code.codeblog.dto.user.UserResponseDTO;
import blog.code.codeblog.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserServiceInterface {
    Optional<User> findById(UUID id);
    UserResponseDTO findUserById(UUID id);
    User findByLogin(String login);
    void saveUser(CreateUserDTO user);
    UpdateUserResponseDTO updateUser(UpdateUserCommand command);
    void deleteUser(DeleteUserCommand command);
    ImageUploadResponseDTO saveUploadProfilePic(UUID userId, String profilePicUrl, String profilePicId);
    void follow(FollowCommand command);
    void unfollow(UnfollowCommand command);
    User getReference(UUID id);
    boolean deleteProfilePic(String publicId);
    PageResponseDTO<UserFollowDTO> getFollowers(GetFollowersCommand command);
    PageResponseDTO<UserFollowDTO> getFollowing(GetFollowingCommand command);
}
