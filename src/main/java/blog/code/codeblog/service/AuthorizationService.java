package blog.code.codeblog.service;


import blog.code.codeblog.model.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    private UserService userService;

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



}