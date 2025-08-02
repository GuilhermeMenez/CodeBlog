package blog.code.codeblog.service;

import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        String token = tokenService.generateToken(validUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String result = tokenService.validateToken(token);
        assertEquals(validUser.email(), result);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ao tentar gerar token com secret inválido")
    void validateTokenWithInvalidSecret() {
        TokenService ts = new TokenService();
        UserDTO user = new UserDTO("nome", "email@email.com", "senha", UserRoles.ADMIN);
        ReflectionTestUtils.setField(ts, "secret", null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ts.generateToken(user);
        });
        assertNotNull(exception.getCause());
        assertTrue(exception.getMessage().contains("Erro ao gerar token JWT"));
    }

    @Test
    @DisplayName("Deve validar o subject do token gerado")
    void ValidateTokenSubject() {
        String token = tokenService.generateToken(validUser);
        String subject = tokenService.validateToken(token);
        assertEquals(validUser.email(), subject);
    }

    @Test
    @DisplayName("Deve retornar vazio para token com issuer inválido")
    void validateInvalidIssuerToken() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);

        String tokenOutroIssuer = com.auth0.jwt.JWT.create()
                .withIssuer("OutroIssuer")
                .withSubject(validUser.email())
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
        String token = tokenService.generateToken(validUser);
        String alteredToken = token + "abc";

        String result = tokenService.validateToken(alteredToken);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar vazio para token expirado")
    void validateExpiredToken() {
        String expiredToken = com.auth0.jwt.JWT.create()
                .withIssuer("CodeBlog")
                .withSubject(validUser.email())
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
        String token1 = tokenService.generateToken(validUser);
        String token2 = tokenService.generateToken(validUser);

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