package blog.code.codeblog.controller;

import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.LoginResponseDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AutheticationController {

    @Autowired
    AuthorizationService authorizationService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@ModelAttribute @Valid CreateUserDTO user) {
        log.info("Register request received for user {}", user.email());
        return authorizationService.register(user);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@RequestBody @Valid AuthenticationDTO authenticationDTO) {
        log.info("Login request received for user {}", authenticationDTO.login());
        return authorizationService.login(authenticationDTO);

    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        log.info("Logout request received for user {}", request.getUserPrincipal().getName());
        authorizationService.logout(request);
    }

}