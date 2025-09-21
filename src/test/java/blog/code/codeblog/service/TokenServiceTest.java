package blog.code.codeblog.service;

import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private static final String SECRET = "meuSecretDeTeste";
    private UserDTO validUser;

    @BeforeEach
    @DisplayName("Configuração inicial para TokenService")
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        validUser = new UserDTO("guilherme", "guilherme@email", "123", UserRoles.COSTUMER);
    }

    @Test
    @DisplayName("Deve gerar um token válido para o usuário")
    void generateValidToken() {
        User validUserModel = new User(validUser.name(), validUser.email(), validUser.password(), validUser.role());
        validUserModel.setId(UUID.randomUUID());

        String token = tokenService.generateToken(validUserModel);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String result = tokenService.validateToken(token);
        assertEquals(validUser.email(), result);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ao tentar gerar token com secret inválido")
    void validateTokenWithInvalidSecret() {
        TokenService ts = new TokenService();
        User userModel = new User("nome", "email@email.com", "senha", UserRoles.ADMIN); // Conversão explícita
        ReflectionTestUtils.setField(ts, "secret", null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ts.generateToken(userModel); // Passa User
        });
        assertNotNull(exception.getCause());
        assertTrue(exception.getMessage().contains("Erro ao gerar token JWT"));
    }

    @Test
    @DisplayName("Deve validar o subject do token gerado")
    void ValidateTokenSubject() {

        User validUserModel = new User(
                validUser.name(),
                validUser.email(),
                validUser.password(),
                validUser.role()
        );
        validUserModel.setId(UUID.randomUUID());

        String token = tokenService.generateToken(validUserModel);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String result = tokenService.validateToken(token);
        assertEquals(validUser.email(), result);

    }


    @Test
    @DisplayName("Deve retornar vazio para token com issuer inválido")
    void validateInvalidIssuerToken() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);

        User validUserModel = new User(validUser.name(), validUser.email(), validUser.password(), validUser.role());
        String tokenOutroIssuer = com.auth0.jwt.JWT.create()
                .withIssuer("OutroIssuer")
                .withSubject(validUserModel.getLogin()) // Usa o modelo User
                .withExpiresAt(java.time.LocalDateTime.now().plusHours(2)
                        .atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                        .toInstant())
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));

        String result = tokenService.validateToken(tokenOutroIssuer);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar vazio para token alterado")
    void validateAlteredToken() {

        User validUserModel = new User(validUser.name(), validUser.email(), validUser.password(), validUser.role());
        validUserModel.setId(UUID.randomUUID());

        String token = tokenService.generateToken(validUserModel);
        String alteredToken = token + "abc";

        String result = tokenService.validateToken(alteredToken);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar vazio para token expirado")
    void validateExpiredToken() {
        User validUserModel = new User(validUser.name(), validUser.email(), validUser.password(), validUser.role());
        String expiredToken = com.auth0.jwt.JWT.create()
                .withIssuer("CodeBlog")
                .withSubject(validUserModel.getLogin()) // Usa o modelo User
                .withExpiresAt(java.time.LocalDateTime.now().minusHours(1)
                        .atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                        .toInstant())
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));

        String result = tokenService.validateToken(expiredToken);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar vazio para token vazio ou nulo")
    void validateEmptyToken() {
        assertEquals("", tokenService.validateToken(""));
        assertEquals("", tokenService.validateToken(null));
    }

    @Test
    @DisplayName("Deve gerar tokens idênticos para o mesmo usuário")
    void generateEqualTokens() {
        User validUserModel = new User(validUser.name(), validUser.email(), validUser.password(), validUser.role());
        validUserModel.setId(UUID.randomUUID());
        String token1 = tokenService.generateToken(validUserModel);
        String token2 = tokenService.generateToken(validUserModel);

        assertNotNull(token1);
        assertNotNull(token2);
        assertFalse(token1.isEmpty());
        assertFalse(token2.isEmpty());

        assertEquals(token1, token2);

        String subject1 = tokenService.validateToken(token1);
        String subject2 = tokenService.validateToken(token2);

        assertEquals(validUser.email(), subject1);
        assertEquals(validUser.email(), subject2);
    }
}