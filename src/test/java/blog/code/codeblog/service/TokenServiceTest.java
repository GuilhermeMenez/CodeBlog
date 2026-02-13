//package blog.code.codeblog.service;
//
//import blog.code.codeblog.enums.UserRoles;
//import blog.code.codeblog.model.User;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import jakarta.servlet.http.HttpServletRequest;
//import java.util.UUID;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TokenServiceTest {
//    @InjectMocks
//    private TokenService tokenService;
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//    private static final String SECRET = "testSecret";
//    private User validUser;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
//        validUser = new User("testuser", "test@email.com", "password", UserRoles.COSTUMER);
//        validUser.setId(UUID.randomUUID());
//    }
//
//    @Test
//    @DisplayName("Should generate a valid token for user")
//    void generateValidToken() {
//        String token = tokenService.generateToken(validUser);
//        assertNotNull(token);
//        assertFalse(token.isEmpty());
//        String subject = tokenService.validateToken(token);
//        assertEquals(validUser.getLogin(), subject);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when generating token with invalid secret")
//    void generateTokenWithInvalidSecretShouldThrow() {
//        ReflectionTestUtils.setField(tokenService, "secret", null);
//        Exception exception = assertThrows(RuntimeException.class, () -> tokenService.generateToken(validUser));
//        assertTrue(exception.getMessage().contains("Error generating JWT token"));
//    }
//
//    @Test
//    @DisplayName("Should throw exception for token with invalid issuer")
//    void validateTokenWithInvalidIssuerShouldThrow() {
//        String token = com.auth0.jwt.JWT.create()
//                .withIssuer("OtherIssuer")
//                .withSubject(validUser.getLogin())
//                .withExpiresAt(java.time.LocalDateTime.now().plusHours(2)
//                        .atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())
//                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));
//        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(token));
//    }
//
//    @Test
//    @DisplayName("Should throw exception for altered token")
//    void validateAlteredTokenShouldThrow() {
//        String token = tokenService.generateToken(validUser);
//        String alteredToken = token + "abc";
//        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(alteredToken));
//    }
//
//    @Test
//    @DisplayName("Should throw exception for expired token")
//    void validateExpiredTokenShouldThrow() {
//        String expiredToken = com.auth0.jwt.JWT.create()
//                .withIssuer("CodeBlog")
//                .withSubject(validUser.getLogin())
//                .withExpiresAt(java.time.LocalDateTime.now().minusHours(1)
//                        .atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())
//                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));
//        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(expiredToken));
//    }
//
//    @Test
//    @DisplayName("Should throw exception for empty or null token")
//    void validateTokenWithEmptyOrNullValueShouldThrowJWTVerificationException() {
//        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(""));
//        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(null));
//    }
//
//    @Test
//    @DisplayName("Should extract userId from token")
//    void getSubjectIdFromTokenShouldReturnUserId() {
//        String token = tokenService.generateToken(validUser);
//        String userId = tokenService.getSubjectIdFromToken(token);
//        assertEquals(validUser.getId().toString(), userId);
//    }
//
//    @Test
//    @DisplayName("Should throw exception if token does not contain id claim")
//    void getSubjectIdFromTokenWithoutIdShouldThrow() {
//        String token = com.auth0.jwt.JWT.create()
//                .withIssuer("CodeBlog")
//                .withSubject(validUser.getLogin())
//                .withExpiresAt(java.time.LocalDateTime.now().plusHours(2)
//                        .atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())
//                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));
//        Exception exception = assertThrows(RuntimeException.class, () -> tokenService.getSubjectIdFromToken(token));
//
//        assertTrue(exception.getMessage().contains("Error extracting user id from JWT token"));
//    }
//
//    @Test
//    @DisplayName("Should blacklist token and check blacklist status")
//    void blackListTokenAndCheckStatus() {
//        String token = tokenService.generateToken(validUser);
//        com.auth0.jwt.interfaces.DecodedJWT decodedJWT = com.auth0.jwt.JWT.decode(token);
//        String jti = decodedJWT.getId();
//        @SuppressWarnings("unchecked")
//        ValueOperations<String, Object> valueOps = (ValueOperations<String, Object>) mock(ValueOperations.class);
//        when(redisTemplate.opsForValue()).thenReturn(valueOps);
//        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any());
//        when(redisTemplate.hasKey("blacklist:" + jti)).thenReturn(true);
//        tokenService.blackListToken(token);
//        boolean isBlacklisted = tokenService.isBlackListed(token);
//        assertTrue(isBlacklisted);
//        verify(redisTemplate).opsForValue();
//        verify(redisTemplate).hasKey("blacklist:" + jti);
//    }
//
//    @Test
//    @DisplayName("Should return false for token without JTI in blacklist check")
//    void isBlackListedWithoutJtiShouldReturnFalse() {
//        String token = com.auth0.jwt.JWT.create()
//                .withIssuer("CodeBlog")
//                .withSubject(validUser.getLogin())
//                .withExpiresAt(java.time.LocalDateTime.now().plusHours(2)
//                        .atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())
//                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));
//        boolean result = tokenService.isBlackListed(token);
//        assertFalse(result);
//    }
//
//    @Test
//    @DisplayName("Should recover token from HttpServletRequest")
//    void recoverTokenShouldReturnToken() {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        String token = "sometokenvalue";
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        String recovered = tokenService.recoverToken(request);
//        assertEquals(token, recovered);
//    }
//
//    @Test
//    @DisplayName("Should return null if Authorization header is missing or invalid")
//    void recoverTokenShouldReturnNullIfHeaderMissingOrInvalid() {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        when(request.getHeader("Authorization")).thenReturn(null);
//        assertNull(tokenService.recoverToken(request));
//        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
//        assertNull(tokenService.recoverToken(request));
//    }
//}
