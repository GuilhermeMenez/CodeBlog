package blog.code.codeblog.controller;

import blog.code.codeblog.dto.authentication.*;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.facade.AuthenticationFacade;
import blog.code.codeblog.service.AuthenticationService;
import blog.code.codeblog.service.integration.Auth0ServiceIntergration;
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
    AuthenticationService authorizationService;

    @Autowired
    Auth0ServiceIntergration auth0ServiceIntergration;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDTO register(@RequestBody @Valid CreateUserDTO user) {
        log.info("Register request received for user {}", user.email());
        return authenticationFacade.createUser(user);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@RequestBody @Valid AuthenticationDTO authenticationDTO) {
        log.info("Login request received for user {}", authenticationDTO.login());
        return authenticationFacade.authenticate(authenticationDTO);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        log.info("Logout request received for user {}", request.getUserPrincipal().getName());
        authorizationService.logout(request);
    }

    @PostMapping("/otp/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void passwordlessStart(@RequestBody @Valid SendOTPRequestDTO dto) {
        log.info("Passwordless start para {}", dto.email());
        auth0ServiceIntergration.sendOTP(dto);
    }

    @PostMapping("/otp/verify")
    @ResponseStatus(HttpStatus.OK)
    public boolean passwordlessVerify(@RequestBody @Valid VerifyOTPrequestDTO dto) {
        log.info("Passwordless verify para {}", dto.email());
        return auth0ServiceIntergration.verifyOTP(dto);
    }

}