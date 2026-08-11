package blog.code.codeblog.mapper;

import blog.code.codeblog.command.user.CreateUserCommand;
import blog.code.codeblog.command.user.LoginCommand;
import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthenticationMapper {

    public CreateUserCommand toCreateUserCommand(CreateUserDTO dto) {
        return CreateUserCommand.builder()
                .name(dto.name())
                .email(dto.email())
                .credential(dto.credential())
                .profileImage(dto.profileImage())
                .flow(dto.flow())
                .build();
    }

    public LoginCommand toLoginCommand(AuthenticationDTO dto) {
        return LoginCommand.builder()
                .login(dto.login())
                .credential(dto.credential())
                .flow(dto.flow())
                .build();
    }
}