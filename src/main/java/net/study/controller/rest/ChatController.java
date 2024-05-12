package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageDto;
import net.study.dto.MessageIdListDto;
import net.study.dto.TargetedMessageDto;
import net.study.model.user.User;
import net.study.service.chat.ChatCrudService;
import net.study.service.MyUserDataStorageService;
import net.study.service.chat.ChatDeletionService;
import net.study.service.chat.MessageHistoryService;
import net.study.service.chat.UnreadMessagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatCrudService chatCrudService;
    private final ChatDeletionService chatDeletionService;
    private final MessageHistoryService messageHistoryService;
    private final UnreadMessagesService unreadMessagesService;
    private final MyUserDataStorageService myUserData;

    @PostMapping("/send-private-message")
    public ResponseEntity<String> sendMessage(
            @RequestParam(value = "messageText") String messageText,
            @RequestParam(value = "receiverName") String receiverName,
            @RequestParam(value = "messageIdToReply", required = false) String messageIdToReply,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        chatCrudService.sendMessageAndNotify(chatCrudService.createMessage(receiverName, messageText, Objects.equals(messageIdToReply, "null") ? null : Long.valueOf(messageIdToReply), image));

        return ResponseEntity.ok("Message sent successfully");
    }
    @PostMapping("/delete-messages-list")
    public void deleteMessagesList(@RequestBody() MessageIdListDto messageIdListDto) {
        System.out.println(messageIdListDto.getMessageIdList());
        chatCrudService.deleteMessagesList(messageIdListDto.getMessageIdList(),false,true);
    }

    @PostMapping("/edit-private-message/{username}")
    public void editMessage(@PathVariable String username, @RequestBody MessageDto messageDto) {
        chatCrudService.editMessage(messageDto.getMessageId(),messageDto.getMessageText(),username);
    }
    @GetMapping ("/get-message-history/{username}")
    public ResponseEntity<Map<String,List<TargetedMessageDto>>> getMessageHistory(
            @PathVariable String username,
            @RequestParam boolean shouldResetPageNumber,
            @RequestParam(required = false) String messageIdBeforeWhichLoad)
     {
        User sender = myUserData.getMyUser();
        Optional<User> receiverOptional = chatCrudService.findUserByUsername(username);
        if(receiverOptional.isEmpty() || !sender.getContactUsernameSet().contains(username)){
            return ResponseEntity.badRequest().body(null);
        }
        User receiver = receiverOptional.get();
        List<TargetedMessageDto> targetedMessageDtoList = messageHistoryService.getMessageHistory(sender,receiver, Objects.equals(messageIdBeforeWhichLoad, "null") ? null :  Long.valueOf(messageIdBeforeWhichLoad),shouldResetPageNumber);
        return ResponseEntity.ok(Map.of("message_history",targetedMessageDtoList));
    }

    @GetMapping("/get-first-message/{username}")
    public ResponseEntity<Map<String,Long>> getFirstMessage(@PathVariable String username) {
        try {
            return ResponseEntity.ok(Map.of("first-message-id",messageHistoryService.getFirstMessageId(username)));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/delete-chat-locally/{contact_name}")
    public void deleteContactLocally(@PathVariable(name = "contact_name") String contactName) {
        chatDeletionService.deleteChatLocally(contactName);
    }
    @GetMapping("/delete-chat-request/{contact_name}")
    public void sendChatDeletionRequest(@PathVariable(name = "contact_name") String contactName) {
        chatDeletionService.sendChatDeletionRequest(contactName);
    }
    @GetMapping  ("/chat-deletion-voting/{username}/{vote}")
    public void sendChatDeletionResponse (@PathVariable String username, @PathVariable String vote) {
        chatDeletionService.voteToChatDeletion(username,vote);
    }
    @PostMapping("forward-messages-to-contacts")
    public void forwardMessagesToContacts (
            @RequestParam List<String> contactNameList,
            @RequestParam List<Long> messageIdList)
    {
        chatCrudService.forwardMessagesToContacts(contactNameList,messageIdList);
    }
    @GetMapping("get-last-message/{username}")
    public ResponseEntity<Map<String, String>> getLastMessage(@PathVariable String username) {
        String lastMessage = messageHistoryService.getLastMessage(username);
        return ResponseEntity.ok(Map.of("last_message_text",lastMessage));
    }
    @GetMapping("/update-selected-contact-name/{contactName}")
    public void updateSelectedContactName(@PathVariable String contactName) {
        chatCrudService.setCurrentSelectedContactName(contactName);
    }
    @GetMapping("/get-unread-messages/{username}")
    public ResponseEntity<Map<String,Integer>> getUnreadMessages(@PathVariable String username) {
        Integer unreadMessagesAmount = unreadMessagesService.getUnreadMessagesAmount(username);
        return ResponseEntity.ok(Map.of("unread_messages_amount",unreadMessagesAmount));
    }
    @GetMapping("/clear-unread-messages-amount/{username}")
    public void clearUnreadMessagesAmount(@PathVariable String username) {
        unreadMessagesService.clearUnreadMessages(username);
    }
}
