package blog.code.codeblog.service;

import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.LoginResponseDTO;
import blog.code.codeblog.dto.authentication.RegisterRespondeDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AuthenticationService {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;


    public RegisterRespondeDTO register(CreateUserDTO user) throws IllegalArgumentException {
        log.info("[register] Attempting to register user: {}", user.email());

        if (userService.findByLogin(user.email()) != null){
            log.warn("[register] Email already registered: {}", user.email());
            throw new IllegalArgumentException("Email already registered");
        }

        userService.saveUser(user);

        AuthenticationDTO userAuthenticate = new AuthenticationDTO(user.email(), user.password());
        var loginResponse = login(userAuthenticate);

        log.info("[register] User logged in successfully after registration: {}", userAuthenticate.login());

        return new RegisterRespondeDTO(loginResponse.token());
    }

    public LoginResponseDTO login(AuthenticationDTO authenticationDTO){
        log.info("[login] Attempting login for user: {}", authenticationDTO.login());

        var usernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.login(), authenticationDTO.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        log.info("[login] User logged in successfully: {}", user.getLogin());

        return new LoginResponseDTO(token);
    }

    public void logout(HttpServletRequest request){
        String recoveredToken = tokenService.recoverToken(request);
        tokenService.blackListToken(recoveredToken);

        log.info("[logout] User logged out successfully. Remote user: {}", request.getRemoteUser());
    }
}
