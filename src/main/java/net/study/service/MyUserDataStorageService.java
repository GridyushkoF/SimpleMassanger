package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.model.user.User;
import net.study.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MyUserDataStorageService {
    private final UserRepository userRepository;
    public User getMyUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String myUsername = authentication.getName();
        Optional<User> myUserOptional = userRepository.findByUsername(myUsername);
        if(myUserOptional.isPresent()) {
            return myUserOptional.get();
        }
        throw new IllegalArgumentException("Your account does not exists");
    }
    public Set<User> getMyUserContacts() {
        User myUser = getMyUser();
        return myUser.getContactUsernameSet()
                .stream()
                .map(userRepository::findByUsername)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
    public String getMyUsername () {
            return getMyUser().getUsername();
    }
}
