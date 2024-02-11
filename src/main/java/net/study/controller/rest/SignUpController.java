package net.study.controller.rest;

import net.study.model.user.User;
import net.study.repository.UserRepository;
import net.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SignUpController {
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public SignUpController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String,String>> signUpNewUser(@RequestBody User user) {
        if(userRepository.existsByUsername(user.getUsername()))
        {
            return ResponseEntity.ok(Map.of("is_signup_success","false","error_message","Имя пользователя занято"));
        }
        if(user.getPassword().equals("") || user.getUsername().equals("")) {
            return ResponseEntity.ok(Map.of("is_signup_success","false","error_message","Пустой логин или пароль"));
        }
        userService.encryptPasswordAndSaveNewUser(user);
        return ResponseEntity.ok(Map.of("is_signup_success","true","error_message","no errors"));
    }
}
