//package net.study.service;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import net.study.model.Message;
//import net.study.model.user.User;
//import net.study.repository.MessageRepository;
//import net.study.repository.UnreadMessageRepository;
//import net.study.repository.UserRepository;
//import net.study.repository.VotingRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class EntityTestInitService {
//    private final UserRepository userRepository;
//    private final MessageRepository messageRepository;
//    private final PasswordEncoder bcrypt;
//    private final VotingRepository votingRepository;
//    private final UnreadMessageRepository unreadMessageRepository;
//    ;
//
//    @Transactional
//    public void initTestUserWithPassword1(String username) {
//        unreadMessageRepository.deleteAll();
//        messageRepository.deleteAll();
//        userRepository.deleteAll();
//        Optional<User> testUserOptional = userRepository.findByUsername(username);
//        if (testUserOptional.isEmpty()) {
//            User testUser = createDefaultUser(username);
//            userRepository.save(testUser);
//        }
//
//    }
//
//    @Transactional
//    public void cleanVotes() {
//        votingRepository.deleteAll();
//    }
//
//    @Transactional
//    public void initTestMessages(int messagesAmount, String username) {
//        messageRepository.deleteAll();
//        List<Message> messagesBatch = new ArrayList<>();
//        Optional<User> senderOptional = userRepository.findByUsername(username);
//        User senderAndReceiver = senderOptional.orElseGet(() -> createDefaultUser(username));
//        for (int i = 0; i < messagesAmount; i++) {
//            Message message = new Message(String.valueOf(i), senderAndReceiver, senderAndReceiver);
//            if (i < 600) {
//                setOldDateTimeToMessage(message);
//            }
//            messagesBatch.add(message);
//        }
//        messageRepository.saveAll(messagesBatch);
//    }
//
//    private void setOldDateTimeToMessage(Message message) {
//        LocalDateTime now = LocalDateTime.of(2024, 4, 20, 18, 45);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
//        String formattedDate = now.format(formatter);
//        message.setDateTime(formattedDate);
//    }
//
//    public User createDefaultUser(String username) {
//        return new User(username, bcrypt.encode("1"), null, null, Set.of("1"), "test description");
//    }
//
//    @Transactional
//    @PostConstruct
//    public void initTestData() {
//        System.out.println("INIT_TEST_DATA_STARTED!");
//        cleanVotes();
//        initTestUserWithPassword1("1");
//        initTestMessages(1000, "1");
//    }
//
//}
