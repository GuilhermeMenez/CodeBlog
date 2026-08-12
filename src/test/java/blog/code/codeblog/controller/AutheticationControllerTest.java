package blog.code.codeblog.controller;


import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.LoginResponseDTO;
import blog.code.codeblog.dto.authentication.RegisterResponseDTO;
import blog.code.codeblog.dto.authentication.SendOTPRequestDTO;
import blog.code.codeblog.dto.authentication.VerifyOTPrequestDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.enums.AuthFlow;
import blog.code.codeblog.facade.AuthenticationFacade;
import blog.code.codeblog.service.AuthenticationService;
import blog.code.codeblog.service.integration.Auth0ServiceIntergration;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutheticationControllerTest {

    @Mock
    private AuthenticationService authorizationService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private Auth0ServiceIntergration auth0ServiceIntergration;

    @InjectMocks
    private AutheticationController authenticationController;

    @BeforeEach
    @DisplayName("Initial setup for AutheticationControllerTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should register a user successfully via facade")
    void testRegisterSuccess() {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "Test User", "test@example.com", "plainPassword", AuthFlow.PASSWORD, null);

        when(authenticationFacade.createUser(createUserDTO))
                .thenReturn(new RegisterResponseDTO("mocked-token"));

        RegisterResponseDTO tokenResponse = authenticationController.register(createUserDTO);

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertEquals("mocked-token", tokenResponse.token());
        verify(authenticationFacade, times(1)).createUser(createUserDTO);
    }

    @Test
    @DisplayName("Should login a user successfully via facade")
    void testLoginSuccess() {
        AuthenticationDTO dto = new AuthenticationDTO("test@example.com", "plainPassword", AuthFlow.PASSWORD);

        when(authenticationFacade.authenticate(dto))
                .thenReturn(new LoginResponseDTO("mocked-token"));

        LoginResponseDTO response = authenticationController.login(dto);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("mocked-token", response.token());
        verify(authenticationFacade, times(1)).authenticate(dto);
    }

    @Test
    @DisplayName("Should logout a user successfully")
    void testLogoutSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Principal principal = mock(Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("test@example.com");

        authenticationController.logout(request);

        verify(authorizationService, times(1)).logout(request);
    }

    @Test
    @DisplayName("Should trigger passwordless start (send OTP)")
    void testPasswordlessStart() {
        SendOTPRequestDTO dto = new SendOTPRequestDTO("test@example.com");

        authenticationController.passwordlessStart(dto);

        verify(auth0ServiceIntergration, times(1)).sendOTP(dto);
    }

    @Test
    @DisplayName("Should verify passwordless OTP")
    void testPasswordlessVerify() {
        VerifyOTPrequestDTO dto = new VerifyOTPrequestDTO("test@example.com", "123456");
        when(auth0ServiceIntergration.verifyOTP(dto)).thenReturn(true);

        boolean result = authenticationController.passwordlessVerify(dto);

        Assertions.assertTrue(result);
        verify(auth0ServiceIntergration, times(1)).verifyOTP(dto);
    }
}