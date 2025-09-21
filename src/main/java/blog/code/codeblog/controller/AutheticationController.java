package blog.code.codeblog.controller;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutheticationController {

    @Autowired
    AuthorizationService authorizationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserDTO user){
     return ResponseEntity.ok(authorizationService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO authenticationDTO){
      return authorizationService.login(authenticationDTO);
    }


}