package net.study.service.chat;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageTarget;
import net.study.dto.TargetedMessageDto;
import net.study.mapping.MessageDtoMapper;
import net.study.model.Message;
import net.study.model.OriginalMessageStatus;
import net.study.model.VotingToDeleteChat;
import net.study.model.user.User;
import net.study.repository.MessageRepository;
import net.study.repository.UserRepository;
import net.study.repository.VotingRepository;
import net.study.service.ImageStorageService;
import net.study.service.MyUserDataStorageService;
import net.study.service.websocket.MessageNotificatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatCrudService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final MessageNotificatorService messageNotificator;
    private final ImageStorageService imageStorageService;
    private final MessageDtoMapper messageDtoMapper;
    private final VotingRepository votingRepository;
    private final MyUserDataStorageService myUserData;
    private final UnreadMessagesService unreadMessagesService;
    private String currentSelectedContactName = "";
    public void setCurrentSelectedContactName(String currentSelectedContactName) {
        this.currentSelectedContactName = currentSelectedContactName;
    }
    @Value("${pinned-images-path}")
    public String pinnedImagesFolderPath;

    public boolean notUsernameEqualsSelectedContactName(String username) {
        return !username.equals(currentSelectedContactName);
    }
    @Transactional
    public Message createMessage(String receiverUsername, String messageText, Long idOfMessageWeReplying, MultipartFile pinnedImage) {
        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);
        User sender = myUserData.getMyUser();
        User receiver = receiverOptional.orElseThrow();
        Message message = new Message(messageText, sender, receiver);
        if(idOfMessageWeReplying != null) {
            messageRepository.findById(idOfMessageWeReplying)
                    .ifPresent(message::setOriginalMessageWeReplied);
            message.setOriginalMessageStatus(OriginalMessageStatus.EXISTS);
        }
        if(pinnedImage != null) {
            String imageFileName  = imageStorageService.uploadImageAndReturnFileName(pinnedImage,pinnedImagesFolderPath);
            message.setPinnedImageFilename(imageFileName);
        }
        if(notUsernameEqualsSelectedContactName(sender.getUsername())) {
            unreadMessagesService.addValueToUnreadMessagesAmount(receiver, sender,1);
        }
        messageRepository.save(message);
        return message;
    }
    public void sendMessageAndNotify(Message message) {
        messageNotificator.notifyUser(message.getReceiver().getUsername(),
                messageDtoMapper.convertToTargetedDto(message));
    }
    @Transactional
    public void deleteMessagesList(List<Integer> messageIdList, boolean ignoreSender, boolean shouldNotify) {
        System.out.println("MESSAGE_ID_LIST: " + messageIdList.size());
        List<Message> messageList = messageDtoMapper.convertIdListToEntityList(messageIdList);
        List<TargetedMessageDto> operations = new ArrayList<>();
        sortMessageListByIdDesc(messageList).forEach(message -> {
            User senderOrReceiver = myUserData.getMyUsername().equals(message.getSender().getUsername()) ? message.getReceiver() : message.getSender();
            if (message.getSender().getUsername().equals(myUserData.getMyUsername()) || ignoreSender) {
                unreadMessagesService.addValueToUnreadMessagesAmount(senderOrReceiver,myUserData.getMyUser(),-1);
                clearMessageOfAttachedInfo(message);
                operations.add(messageDtoMapper.convertToTargetedDto(message, MessageTarget.DELETE));
            }
        });
        System.out.println("OPERATIONS: " + operations.size());
        messageRepository.deleteBatchByIds(messageIdList);
        if(!operations.isEmpty() && shouldNotify) {
            messageNotificator.notifyUserByBatch(operations.get(0).getReceiver().getUsername(),operations);
        }
    }
    public static List<Message> sortMessageListByIdDesc(List<Message> messageList) {
        List<Message> copy = new ArrayList<>(messageList);
        Collections.sort(copy);
        return copy;
    }
    @Transactional
    public void deleteMessage(Message message, boolean ignoreSender) {
        deleteMessagesList(List.of(Math.toIntExact(message.getId())),ignoreSender,true);
    }
    @Transactional
    public void clearMessageOfAttachedInfo(Message message) {
        String pinnedImageFilename = message.getPinnedImageFilename();
        if(pinnedImageFilename != null) {
            imageStorageService.deleteImage(pinnedImagesFolderPath + "/" + pinnedImageFilename);
        }
        if(message.getTarget().equals(MessageTarget.CHAT_DELETE)) {
            Optional<VotingToDeleteChat> attachedVotingOptional = votingRepository.findByMembers(
                    message.getSender(),
                    message.getReceiver()
            );
            attachedVotingOptional.ifPresent(votingRepository::delete);
        }
        message.setOriginalMessageWeReplied(null);
        findAllReplyMessagesAndDetach(message);
        messageRepository.save(message);
    }
    @Transactional
    public void findAllReplyMessagesAndDetach(Message message) {
        messageRepository.detachReplyMessagesOfOriginalAndUpdateStatus(message.getId(),OriginalMessageStatus.DELETED);
    }
    @Transactional
    public void editMessage(Long messageId, String messageText, String contactName) {
        messageRepository.findById(messageId).ifPresent(message -> {
            try {
                if (message.getSender().getUsername().equals(myUserData.getMyUsername())) {
                    message.setMessageText(messageText);
                    message.setCurrentDateTime(true);
                    message.setTarget(MessageTarget.UPDATE);
                    message.setTargetId(messageId);
                    messageRepository.save(message);
                    messageNotificator.notifyUser(contactName, messageDtoMapper.convertToTargetedDto(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
    @Transactional
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Transactional
    public void forwardMessagesToContacts(List<String> contactIdList,List<Long> messageIdList) {
        System.out.println(contactIdList);
        User sender = myUserData.getMyUser();
        contactIdList.stream().map(userRepository::findByUsername)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(receiver -> messageIdList.stream()
                .map(messageRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(message -> {
                    Message newMessage = new Message(
                            message.getMessageText(),
                            sender,
                            receiver
                    );
                    System.out.println("Creating new forwarding message");
                    newMessage.setCurrentDateTime(false);
                    newMessage.setForwarder(message.getSender());
                    newMessage.setPinnedImageFilename(message.getPinnedImageFilename());
                    messageRepository.save(newMessage);
                    messageNotificator.notifyUser(
                            receiver.getUsername(),
                            messageDtoMapper.convertToTargetedDto(newMessage));

                }));
    }
}


