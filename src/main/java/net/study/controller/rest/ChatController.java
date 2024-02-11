package net.study.controller.rest;

import net.study.dto.MessageDto;
import net.study.dto.MessageDtoWithGoal;
import net.study.dto.MessageGoal;
import net.study.mapping.MessageMapper;
import net.study.model.Message;
import net.study.model.VotingToDeleteChat;
import net.study.model.user.User;
import net.study.repository.MessageRepository;
import net.study.repository.UserRepository;
import net.study.service.UserService;
import net.study.service.websocket.ContactNotificator;
import net.study.service.websocket.MessageNotificator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ChatController {
    private final MessageNotificator messageNotificator;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final ContactNotificator contactNotificator;

    @Autowired
    public ChatController(MessageNotificator messageNotificator, MessageRepository messageRepository, UserService userService, UserRepository userRepository, ContactNotificator contactNotificator) {
        this.messageNotificator = messageNotificator;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.contactNotificator = contactNotificator;
        this.messageMapper = new MessageMapper();
    }

    @PostMapping("/send-private-message/{username}")
    public void sendPrivateMessage(
            @PathVariable String username,
           @RequestBody MessageDto messageDto) {
        Optional<User> senderOptional = userService.getMyUserOptional();
        Optional<User> receiverOptional = userRepository.findByUsername(username);
        if(senderOptional.isPresent() && receiverOptional.isPresent()) {
            User sender = senderOptional.get();
            User receiver = receiverOptional.get();
            Message message = new Message(
                    messageDto.getMessageText(),
                    sender,
                    receiver
            );

            messageRepository.save(message);
            messageNotificator.notifyUser(username,messageMapper.convertToMessageDtoWithGoal(message,MessageGoal.CREATE));
        }
    }
    @PostMapping("/delete-private-message/{username}")
    public void deletePrivateMessage(@PathVariable String username, @RequestBody MessageDto messageDto) {
        messageRepository.findById(messageDto.getMessageId()).ifPresent(message -> {
            try {
                if (message.getSender().getUsername().equals(userService.getMyUsername())) {
                    messageRepository.deleteById(messageDto.getMessageId());
                    messageNotificator.notifyUser(
                            username,
                            messageMapper.convertToMessageDtoWithGoal(message,MessageGoal.DELETE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
    @PostMapping("/edit-private-message/{username}")
    public void editPrivateMessage(@PathVariable String username, @RequestBody MessageDto messageDto) {
        messageRepository.findById(messageDto.getMessageId()).ifPresent(message -> {
            try {
                if (message.getSender().getUsername().equals(userService.getMyUsername())) {
                    message.setMessageText(messageDto.getMessageText());
                    message.setCurrentDateTime(true);
                    messageRepository.save(message);
                    messageNotificator.notifyUser(username,messageMapper.convertToMessageDtoWithGoal(message,MessageGoal.UPDATE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
    @GetMapping ("/get-message-history/{username}")
    public ResponseEntity<Map<String,List<MessageDtoWithGoal>>> getMessageHistory(@PathVariable String username) {
        Optional<User> senderOptional = userService.getMyUserOptional();
        Optional<User> receiverOptional = userRepository.findByUsername(username);
        Optional<User> myUserOptional = userService.getMyUserOptional();
        if(senderOptional.isEmpty()
                || receiverOptional.isEmpty()
                || myUserOptional.isEmpty()
                || !myUserOptional.get().getContactUsernameSet().contains(username)){
            return ResponseEntity.badRequest().body(null);
        }
        User sender = senderOptional.get();
        User receiver = receiverOptional.get();
        List<Message> messageHistoryList = messageRepository.findAllBySenderAndReceiver(sender,receiver);
        List<MessageDtoWithGoal> goalMessageHistoryList = messageHistoryList.stream().map(message -> messageMapper.convertToMessageDtoWithGoal(message,MessageGoal.CREATE)).toList();
        return ResponseEntity.ok(Map.of("message_history",goalMessageHistoryList));
    }
    @GetMapping("/delete-chat-locally/{contact_name}")
    public void deleteContactLocally(@PathVariable(name = "contact_name") String contactName) {
        userService.getMyUserOptional().ifPresent(myUser -> {
            myUser.getContactUsernameSet().remove(contactName);
            userRepository.save(myUser);
            contactNotificator.updateContacts(myUser.getUsername());
        });
    }
    @GetMapping("/delete-chat-request/{contact_name}")
    public void sendChatDeletionRequest(@PathVariable(name = "contact_name") String contactName) {
        userService.getMyUserOptional().ifPresent(myUser -> {
            userRepository.findByUsername(contactName).ifPresent(contactUser -> {
                String messageText = "@" + myUser.getUsername() + " отправил запрос на полное удаление чата, вы подтверждаете удаление?";
                Message message = new Message(messageText,myUser,contactUser);
                messageRepository.save(message);
                messageNotificator.notifyUser(
                        contactName,
                        messageMapper.convertToMessageDtoWithGoal(message,MessageGoal.CHAT_DELETE));
            });
        });
    }
    @PostMapping ("/confirm-chat-deletion")
    public void sendChatDeletionResponse (@RequestBody Map<String, String> answer) {
        String userAnswer = answer.get("deletion-confirm");
        if(userAnswer.equals("true")) {
            VotingToDeleteChat voting;

        }
    }
}
