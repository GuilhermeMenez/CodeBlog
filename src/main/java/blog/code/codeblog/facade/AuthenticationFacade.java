package blog.code.codeblog.facade;

import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.LoginResponseDTO;
import blog.code.codeblog.dto.authentication.RegisterResponseDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.mapper.AuthenticationMapper;
import blog.code.codeblog.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFacade {

    private final AuthenticationService authenticationService;

    public RegisterResponseDTO createUser(CreateUserDTO dto) {
        return authenticationService.register(AuthenticationMapper.toCreateUserCommand(dto));
    }

    public LoginResponseDTO authenticate(AuthenticationDTO dto) {
        return authenticationService.login(AuthenticationMapper.toLoginCommand(dto));
    }
}