package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.config.security.CustomUserDetails;
import net.study.model.user.User;
import net.study.repository.UserRepository;
import net.study.service.websocket.ContactNotificatorService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ContactNotificatorService contactNotificator;
    private final MyUserDataStorageService myUserData;
    private final ApplicationEventPublisher eventPublisher;

    public void encryptPasswordAndSaveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    public boolean existsByUsername (String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public ResponseEntity<Map<String, String>> addContactToMyContactsList(String contactName) {

        Optional<User> userToAddOptional = userRepository.findByUsername(contactName);

        if (userToAddOptional.isPresent()) {
            User myUser = myUserData.getMyUser();
            User userToAdd = userToAddOptional.get();
            if (myUser.getId().equals(userToAdd.getId())) {
                myUser.getContactUsernameSet().add(userToAdd.getUsername());
                userRepository.save(myUser);
                return ResponseEntity.ok(Map.of("is_successfully_added", "true"));
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean updateMyUsername(String newUsername) {
        if (!userRepository.existsByUsername(newUsername)) {
            User myUser = myUserData.getMyUser();
            String oldUsername = myUser.getUsername();

            myUser.setUsername(newUsername);
            userRepository.save(myUser);

            List<User> dependentContacts = setNewUsernameForDependentContacts(newUsername, oldUsername);

            CustomUserDetails customUserDetails = new CustomUserDetails(myUser);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(newUsername, myUser.getPassword(), customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            eventPublisher.publishEvent(new UsernameUpdatedEvent(dependentContacts));
            return true;
        }
        return false;
    }

    private List<User> setNewUsernameForDependentContacts(String newUsername, String oldUsername) {
        List<User> dependentContacts = userRepository.findAllByContactUsernameSetContains(oldUsername);
        dependentContacts.forEach(dependentContact -> {
            dependentContact.getContactUsernameSet().remove(oldUsername);
            dependentContact.getContactUsernameSet().add(newUsername);
            userRepository.save(dependentContact);
        });
        return dependentContacts;
    }


    public record UsernameUpdatedEvent(List<User> dependentContacts) {
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateDependentContactsAfterUsernameUpdated(UsernameUpdatedEvent event) {
        List<User> dependentContacts = event.dependentContacts();
        dependentContacts.forEach(contact -> contactNotificator.updateContacts(contact.getUsername()));
    }
}
