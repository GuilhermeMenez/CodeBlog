package blog.code.codeblog.service;

import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.LoginResponseDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import blog.code.codeblog.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private CloudinaryService cloudinaryService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[loadUserByUsername] Attempting to load user by username: {}", username);
        User user = userService.findByLogin(username);
        if (user == null) {
            log.warn("[loadUserByUsername] User not found for username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public String register(CreateUserDTO user) throws IllegalArgumentException {
        log.info("[register] Attempting to register user: {}", user.email());

        if (userService.findByLogin(user.email()) != null){
            log.warn("[register] Email already registered: {}", user.email());
            throw new IllegalArgumentException("Email already registered");
        }

        String encryptedPassword = passwordEncoder.encode(user.password());
        User newUser = new User(user.name(), user.email(), encryptedPassword);
        userService.saveUser(newUser);

        log.info("[register] User registered successfully: {}", newUser.getLogin());

        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            log.info("[register] Processing profile image for user: {}", newUser.getId());
            try {
                cloudinaryService.uploadFile(user.profileImage(), FlowImageFlag.PROFILE, newUser.getId().toString(), null);
            } catch (IOException e) {
                log.error("[register] Failed to upload profile image for user: {}. Error: {}", newUser.getId(), e.getMessage());
            }
        }

        AuthenticationDTO userAuthenticate = new AuthenticationDTO(user.email(), user.password());
        var loginResponse = login(userAuthenticate);

        log.info("[register] User logged in successfully after registration: {}", newUser.getLogin());
        return loginResponse.token();
    }

    public LoginResponseDTO login(AuthenticationDTO authenticationDTO){
        log.info("[login] Attempting login for user: {}", authenticationDTO.login());

        var usernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.login(), authenticationDTO.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        log.info("[login] User logged in successfully: {}", user.getLogin());

        return new LoginResponseDTO(token);
    }

    public void logout(HttpServletRequest request){
        String recoveredToken = tokenService.recoverToken(request);
        tokenService.blackListToken(recoveredToken);

        log.info("[logout] User logged out successfully. Remote user: {}", request.getRemoteUser());
    }


}