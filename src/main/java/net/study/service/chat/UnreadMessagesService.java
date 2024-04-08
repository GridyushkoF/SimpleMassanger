package net.study.service.chat;

import lombok.RequiredArgsConstructor;
import net.study.model.UnreadMessage;
import net.study.model.user.User;
import net.study.repository.UnreadMessageRepository;
import net.study.repository.UserRepository;
import net.study.service.MyUserDataStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnreadMessagesService {
    private final UserRepository userRepository;
    private final MyUserDataStorageService myUserData;
    private final UnreadMessageRepository unreadMessageRepository;
    @Transactional
    public Integer getUnreadMessagesAmount(String contactName) {
        Optional<User> contactOptional = userRepository.findByUsername(contactName);

        if (contactOptional.isPresent()) {
            User user = myUserData.getMyUser();
            User contact = contactOptional.get();

            Optional<UnreadMessage> unreadMessageOptional = unreadMessageRepository
                    .findByUserAndContact(user, contact);
            if (unreadMessageOptional.isPresent()) {
                UnreadMessage unreadMessage = unreadMessageOptional.get();
                return unreadMessage.getUnreadMessagesAmount();
            }
        }
        return 0;
    }
    @Transactional
    public void clearUnreadMessages(String contactName) {
        Optional<User> contactOptional = userRepository.findByUsername(contactName);

        if (contactOptional.isPresent()) {
            User user = myUserData.getMyUser();
            User contact = contactOptional.get();
            Optional<UnreadMessage> unreadMessageOptional = unreadMessageRepository
                    .findByUserAndContact(user, contact);
            unreadMessageOptional.ifPresent(unreadMessageRepository::delete);
        }
    }
    @Transactional
    public void addValueToUnreadMessagesAmount(User user, User contact, int value) {
        Optional<UnreadMessage> unreadMessageOptional = unreadMessageRepository.findByUserAndContact(user, contact);
        UnreadMessage unreadMessage;
        if(unreadMessageOptional.isPresent()) {
            unreadMessage = unreadMessageOptional.get();
            unreadMessage.setUnreadMessagesAmount(unreadMessage.getUnreadMessagesAmount() + value);
        } else {
            unreadMessage = new UnreadMessage(user, contact, 1);
        }
        unreadMessageRepository.save(unreadMessage);
    }
}
