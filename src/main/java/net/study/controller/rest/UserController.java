package net.study.controller.rest;

import net.study.model.user.User;
import net.study.repository.UserRepository;
import net.study.service.UserService;
import net.study.service.websocket.ContactNotificator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final ContactNotificator contactNotificator;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService, ContactNotificator contactNotificator) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.contactNotificator = contactNotificator;

    }

    @GetMapping("/add-user-to-my-contacts/{username}")
    public ResponseEntity<Map<String, String>> addUserToContactList(@PathVariable String username) {
        Optional<User> myUserOptional = userService.getMyUserOptional();
        Optional<User> userToAddOptional = userRepository.findByUsername(username);

        if (myUserOptional.isPresent() && userToAddOptional.isPresent()) {
            User myUser = myUserOptional.get();
            User userToAdd = userToAddOptional.get();

            // Check if myUser already has userToAdd in contacts
            if (
                    myUser.getContactUsernameSet().stream().anyMatch(contact -> contact.equals(userToAdd.getUsername()))
                                                ||
                    userToAdd.getContactUsernameSet().stream().anyMatch(contact -> contact.equals(myUser.getUsername()))
            ) {
                return ResponseEntity.badRequest().body(Map.of("is_successfully_added", "false"));
            }
            if(myUser.getId().equals(userToAdd.getId())) {
                myUser.getContactUsernameSet().add(userToAdd.getUsername());
                userRepository.save(myUser);
                return ResponseEntity.ok(Map.of("is_successfully_added","true"));
            }
            myUser.getContactUsernameSet().add(userToAdd.getUsername());
            userToAdd.getContactUsernameSet().add(myUser.getUsername());
            userRepository.save(myUser);
            userRepository.save(userToAdd);
            contactNotificator.updateContacts(userToAdd.getUsername());

            return ResponseEntity.ok(Map.of("is_successfully_added", "true"));
        }

        return ResponseEntity.badRequest().body(Map.of("is_successfully_added", "false"));
    }
    @GetMapping("/get-my-contacts")
    public ResponseEntity<Map<String, Set<String>>> getMyUserContactList() {
        Optional<User> myUserOptional = userService.getMyUserOptional();
        if(myUserOptional.isPresent()) {
            User myUser = myUserOptional.get();
            return ResponseEntity.ok(Map.of("my_user_contacts", myUser.getContactUsernameSet()));
        }
        return ResponseEntity.badRequest().body(null);
    }
    @GetMapping("/get-my-username")
    public ResponseEntity<Map<String,String>> getMyUsername() {
        try {
            return ResponseEntity.ok(Map.of("my_username",userService.getMyUsername()));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(null);
    }
}
