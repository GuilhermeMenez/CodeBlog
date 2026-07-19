package blog.code.codeblog.service.provider;

import blog.code.codeblog.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserProvider {
    public User getCurrentUser()  {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user) || !auth.isAuthenticated() ) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }
        log.info("[getCurrentUser] user extracted from context with: {}", user.getId());
        return user;
    }
}
