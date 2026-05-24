package blog.code.codeblog.controller;


import blog.code.codeblog.dto.authentication.RegisterRespondeDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.service.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutheticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AutheticationController authenticationController;

    @BeforeEach
    @DisplayName("Initial setup for AutheticationControllerTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        CreateUserDTO createUserDTO = new CreateUserDTO("Test User", "test@example.com", "plainPassword", UserRoles.COSTUMER, null);

        when(authenticationService.register(createUserDTO)).thenReturn(new RegisterRespondeDTO("mocked-token"));

        RegisterRespondeDTO tokenResponse = authenticationController.register(createUserDTO);

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertEquals("mocked-token", tokenResponse.token());
        verify(authenticationService, times(1)).register(createUserDTO);
    }

}
