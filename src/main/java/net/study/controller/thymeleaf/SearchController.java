package net.study.controller.thymeleaf;

import net.study.model.user.User;
import net.study.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class SearchController {
    private final UserRepository userRepository;
    @Autowired
    public SearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users/{username}")
    public String findContactsByUsername(@PathVariable String username, Model model) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("found_user",user);
            return "found-users-page";
        }
        return "error-404-page";
    }
}
