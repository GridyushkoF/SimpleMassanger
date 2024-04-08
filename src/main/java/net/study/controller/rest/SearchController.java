package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.model.user.User;
import net.study.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/users/{username}")
    public ResponseEntity<Map<String,User>> findContactsByUsername(@PathVariable String username) {
        Optional<User> userOptional = searchService.findByUsername(username);
        if (userOptional.isPresent() && username != null) {
            User user = userOptional.get();
            return ResponseEntity.ok(Map.of("found_user",user));
        }
        return new ResponseEntity<>(HttpStatus.valueOf(404));
    }
}
