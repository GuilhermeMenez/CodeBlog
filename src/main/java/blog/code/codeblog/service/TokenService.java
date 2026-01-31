package blog.code.codeblog.service;


import blog.code.codeblog.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    public String generateToken(User user){
        log.info("[generateToken] Generating token for user: {}", user.getLogin());
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String jti = java.util.UUID.randomUUID().toString();
            String token = JWT.create()
                    .withIssuer("CodeBlog")
                    .withSubject(user.getLogin())
                    .withJWTId(jti)
                    .withClaim("id", user.getId().toString())
                    .withClaim("name", user.getName())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
            log.info("[generateToken] Token generated successfully for user: {}", user.getLogin());
            return token;
        }catch (Exception e){
            log.error("[generateToken] Error generating JWT token for user: {}", user.getLogin(), e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public String getSubjectIdFromToken(String token) {
        log.info("[getSubjectIdFromToken] Extracting subject id from token");
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            DecodedJWT decodedJWT = JWT.decode(token);
            String userId = decodedJWT.getClaim("id").asString();
            if (userId == null) {
                log.warn("[getSubjectIdFromToken] Token does not contain 'id' claim");
                throw new RuntimeException("Token does not contain 'id' claim");
            }
            log.info("[getSubjectIdFromToken] Extracted userId: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("[getSubjectIdFromToken] Error extracting user id from token", e);
            throw new RuntimeException("Error extracting user id from JWT token", e);
        }
    }

    public String validateToken(String token) throws JWTVerificationException {
        log.info("[validateToken] Validating token");
        if (isBlackListed(token)) {
            log.warn("[validateToken] Token is blacklisted");
            throw new JWTVerificationException("Blacklisted token");
        }
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer("CodeBlog")
                .build()
                .verify(token);
        log.info("[validateToken] Token validated successfully for subject: {}", decodedJWT.getSubject());
        return decodedJWT.getSubject();
    }


    private Instant generateExpirationDate() {
        return LocalDateTime.now()
                .plusHours(2)
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .toInstant();
    }

    public void blackListToken(String token) {
        log.info("[blackListToken] Blacklisting token");
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String jti = decodedJWT.getId();
            if (jti == null) {
                log.warn("[blackListToken] Token does not have JTI");
                throw new RuntimeException("Token does not have JTI");
            }
            Instant expiresAt = decodedJWT.getExpiresAt().toInstant();
            long ttlMillis = expiresAt.toEpochMilli() - System.currentTimeMillis();
            long ttlSeconds = Math.max(ttlMillis / 1000, 0);
            redisTemplate.opsForValue().set("blacklist:" + jti, true, ttlSeconds, TimeUnit.SECONDS);
            log.info("[blackListToken] Token blacklisted successfully. jti: {}", jti);
        } catch (Exception e) {
            log.error("[blackListToken] Error blacklisting token", e);
            throw new RuntimeException("Error blacklisting token: " + e.getMessage(), e);
        }
    }

    public boolean isBlackListed(String token) {
        log.info("[isBlackListed] Checking if token is blacklisted");
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String jti = decodedJWT.getId();
            if (jti == null) {
                log.warn("[isBlackListed] Token does not have JTI");
                return false;
            }
            boolean result = redisTemplate.hasKey("blacklist:" + jti);
            log.info("[isBlackListed] Token blacklist status for jti {}: {}", jti, result);
            return result;
        } catch (Exception e) {
            log.error("[isBlackListed] Error checking if token is blacklisted", e);
            return false;
        }
    }

    public String recoverToken(HttpServletRequest request) {
        log.info("[recoverToken] Recovering token from request");
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[recoverToken] Authorization header missing or does not start with 'Bearer '");
            return null;
        }
        String token = authHeader.replace("Bearer ", "").trim();
        log.info("[recoverToken] Token recovered successfully");
        return token;
    }

}
