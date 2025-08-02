package blog.code.codeblog.controller;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AutheticationControllerTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AutheticationController authenticationController;

    @BeforeEach
    @DisplayName("Configuração inicial do AutheticationControllerTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve retornar sucesso ao realizar login com credenciais válidas")
    void testLoginSuccess() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("user", "password");
        LoginResponseDTO loginResponse = new LoginResponseDTO("token123");

        when(authorizationService.login(authenticationDTO))
                .thenReturn(ResponseEntity.ok(loginResponse));

        ResponseEntity<LoginResponseDTO> response = authenticationController.login(authenticationDTO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("token123", response.getBody().token());
    }

    @Test
    @DisplayName("Deve retornar erro 401 ao realizar login com credenciais inválidas")
    void testLoginInvalidCredentials() {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO("user", "wrongpassword");

        when(authorizationService.login(authenticationDTO))
                .thenReturn(ResponseEntity.status(401).build());

        ResponseEntity<LoginResponseDTO> response = authenticationController.login(authenticationDTO);

        assertEquals(401, response.getStatusCode().value());
    }
}