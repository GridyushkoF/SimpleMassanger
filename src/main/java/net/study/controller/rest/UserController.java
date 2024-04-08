package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.model.user.User;
import net.study.service.MyUserDataStorageService;
import net.study.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MyUserDataStorageService myUserData;

    @GetMapping("/add-user-to-my-contacts/{username}")
    public ResponseEntity<Map<String, String>> addUserToContactList(@PathVariable String username) {
        return userService.addContactToMyContactsList(username);
    }
    @GetMapping("/get-my-contacts")
    public ResponseEntity<Map<String, Set<User>>> getMyUserContactList() {
        return ResponseEntity.ok(Map.of("my_user_contacts",myUserData.getMyUserContacts()));
    }
    @GetMapping("/get-my-user")
    public ResponseEntity<Map<String,User>> getMyUser() {
        return ResponseEntity.ok(Map.of("my_user",userService.findByUsername(myUserData.getMyUsername()).orElseGet(User::new)));
    }
}
