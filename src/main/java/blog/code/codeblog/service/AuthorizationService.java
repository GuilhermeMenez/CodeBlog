package blog.code.codeblog.service;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByLogin(username);
    }

    public String register(UserDTO user) {
        if (userService.findByLogin(user.email()) != null){
            throw new UsernameNotFoundException("Email já cadastrado");
        }
        String encryptedPassword = passwordEncoder.encode(user.password());
        User newUser = new User(user.name(),user.email(), encryptedPassword, user.role());
        userService.saveUser(newUser);

        AuthenticationDTO userAuthenticate = new AuthenticationDTO(user.email(), user.password());
        var loginResponse = login(userAuthenticate);
        return loginResponse.getBody().token();
    }

    public ResponseEntity<LoginResponseDTO> login(AuthenticationDTO authenticationDTO){
        var UsernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.login(), authenticationDTO.password());
        var auth = authenticationManager.authenticate(UsernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }



}