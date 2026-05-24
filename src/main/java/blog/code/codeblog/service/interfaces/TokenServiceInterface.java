package blog.code.codeblog.service.interfaces;

import blog.code.codeblog.model.User;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;

public interface TokenServiceInterface {
    String generateToken(User user);
    String getSubjectIdFromToken(String token);
    String validateToken(String token) throws JWTVerificationException;
    void blackListToken(String token);
    boolean isBlackListed(String token);
    String recoverToken(HttpServletRequest request);
}

