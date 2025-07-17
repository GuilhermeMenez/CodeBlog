package blog.code.codeblog.controller;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutheticationController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody @Valid UserDTO user){
        if (userRepository.findByLogin(user.email()) != null){
            return ResponseEntity.badRequest().build();
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(user.password());
        User newUser = new User(user.name(),user.email(), encryptedPassword, user.role());
        this.userRepository.save(newUser);

        return ResponseEntity.ok(user);
    }


    @RequestMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO authenticationDTO){
        var UsernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.login(), authenticationDTO.password());
        var auth =  authenticationManager.authenticate(UsernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }



    @GetMapping("byano")
    public ResponseEntity<String> getByAno() {
        return ResponseEntity.ok("byano");
    }

}