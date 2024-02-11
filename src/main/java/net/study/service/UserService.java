package net.study.service;

import net.study.model.user.User;
import net.study.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Random RANDOM = new Random();
    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }
    public void encryptPasswordAndSaveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    public Optional<User> getMyUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String myUsername = authentication.getName();
        return userRepository.findByUsername(myUsername);
    }
    public String getMyUsername () throws Exception {
        Optional<User> myUserOptional = getMyUserOptional();
        if(myUserOptional.isPresent()) {
            return  myUserOptional.get().getUsername();
        }
        throw new IllegalArgumentException("Your account does not exists");
    }
    public Long generateChatId () {
        return RANDOM.nextLong(100_000,999_999);
    }

}
