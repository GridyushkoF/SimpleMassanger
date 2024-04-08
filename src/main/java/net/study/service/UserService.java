package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.model.user.User;
import net.study.repository.UserRepository;
import net.study.service.websocket.ContactNotificatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ContactNotificatorService contactNotificator;
    private final MyUserDataStorageService myUserData;
    public void encryptPasswordAndSaveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public ResponseEntity<Map<String,String>> addContactToMyContactsList(String contactName) {

        Optional<User> userToAddOptional = userRepository.findByUsername(contactName);

        if (userToAddOptional.isPresent()) {
            User myUser = myUserData.getMyUser();
            User userToAdd = userToAddOptional.get();
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
        return ResponseEntity.ok(Map.of("is_successfully_added", "false"));
    }

}
