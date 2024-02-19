package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageDto;
import net.study.dto.TargetedMessageDto;
import net.study.model.user.User;
import net.study.service.ChatService;
import net.study.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final UserService userService;
    private final ChatService chatService;

    @PostMapping("/send-private-message/{username}")
    public void sendPrivateMessage(
            @PathVariable String username,
           @RequestBody MessageDto messageDto) {
        chatService.sendPrivateMessage(username,messageDto.getMessageText());
    }
    @PostMapping("/delete-private-message/{username}")
    public void deletePrivateMessage(@PathVariable String username, @RequestBody MessageDto messageDto) {
        chatService.deletePrivateMessage(messageDto.getMessageId(), username);
    }
    @PostMapping("/edit-private-message/{username}")
    public void editPrivateMessage(@PathVariable String username, @RequestBody MessageDto messageDto) {
        chatService.editPrivateMessage(messageDto.getMessageId(),messageDto.getMessageText(),username);
    }
    @GetMapping ("/get-message-history/{username}")
    public ResponseEntity<Map<String,List<TargetedMessageDto>>> getMessageHistory(@PathVariable String username) {
        Optional<User> senderOptional = userService.getMyUserOptional();
        Optional<User> receiverOptional = chatService.findUserByUsername(username);
        Optional<User> myUserOptional = userService.getMyUserOptional();
        if(senderOptional.isEmpty()
                || receiverOptional.isEmpty()
                || myUserOptional.isEmpty()
                || !myUserOptional.get().getContactUsernameSet().contains(username)){
            return ResponseEntity.badRequest().body(null);
        }
        User sender = senderOptional.get();
        User receiver = receiverOptional.get();
        List<TargetedMessageDto> targetedMessageDtoList = chatService.getMessageHistory(sender,receiver);
        return ResponseEntity.ok(Map.of("message_history",targetedMessageDtoList));
    }
    @GetMapping("/delete-chat-locally/{contact_name}")
    public void deleteContactLocally(@PathVariable(name = "contact_name") String contactName) {
        chatService.deleteChatLocally(contactName);
    }
    @GetMapping("/delete-chat-request/{contact_name}")
    public void sendChatDeletionRequest(@PathVariable(name = "contact_name") String contactName) {
        chatService.sendChatDeletionRequest(contactName);
    }
    @GetMapping  ("/chat-deletion-voting/{username}/{vote}")
    public void sendChatDeletionResponse (@PathVariable String username, @PathVariable String vote) {
        chatService.voteToChatDeletion(username,vote);
    }
    @PostMapping("send-message-with-image")
    public void sendMessageWithImage(
            @RequestParam String caption,
            MultipartFile image,
            String receiverName)
    {
        chatService.sendMessageWithImage(caption,receiverName,image);
    }
}
