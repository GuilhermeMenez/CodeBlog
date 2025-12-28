package blog.code.codeblog.config.security;

import blog.code.codeblog.config.handlers.CustomAuthenticationEntryPoint;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = tokenService.recoverToken(request);
            if (token != null && !token.isEmpty()) {
                var subject = tokenService.validateToken(token);
                User user = userRepository.findByLogin(subject);
                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            customAuthenticationEntryPoint.commence(request, response, ex);
        } catch (JWTVerificationException ex) {
            customAuthenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid or expired token: " + ex.getMessage(), ex));
        }
    }


}
