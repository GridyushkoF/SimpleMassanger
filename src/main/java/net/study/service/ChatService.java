package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageTarget;
import net.study.dto.TargetedMessageDto;
import net.study.mapping.MessageMapper;
import net.study.model.Message;
import net.study.model.VotingToDeleteChat;
import net.study.model.user.User;
import net.study.repository.MessageRepository;
import net.study.repository.UserRepository;
import net.study.repository.VotingRepository;
import net.study.service.websocket.ContactNotificator;
import net.study.service.websocket.MessageNotificator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageNotificator messageNotificator;
    private final ImageStorageService imageStorageService;
    private final MessageMapper messageMapper = new MessageMapper();
    private final ContactNotificator contactNotificator;
    private final VotingRepository votingRepository;
    @Value("${pinned-images-path}")
    public String pinnedImagesFolderPath;

    public void sendPrivateMessage(String username, String messageText) {
        Optional<User> senderOptional = userService.getMyUserOptional();
        Optional<User> receiverOptional = userRepository.findByUsername(username);
        if (senderOptional.isPresent() && receiverOptional.isPresent()) {
            User sender = senderOptional.get();
            User receiver = receiverOptional.get();
            Message message = new Message(
                    messageText,
                    sender,
                    receiver
            );
            messageRepository.save(message);
            messageNotificator.notifyUser(
                    username,
                    messageMapper.convertToTargetedMessageDto(message));
        }
    }

    public void deletePrivateMessage(Long messageId, String username) {
        messageRepository.findById(messageId).ifPresent(message -> {
            try {
                if (message.getSender().getUsername().equals(userService.getMyUsername())) {
                    String pinnedImageFilename = message.getPinnedImageFilename();
                    if(pinnedImageFilename != null) {
                        imageStorageService.deleteImage(pinnedImagesFolderPath + "/" + pinnedImageFilename);
                    }
                    messageRepository.deleteById(messageId);
                    messageNotificator.notifyUser(
                            username,
                            messageMapper.convertToTargetedMessageDto(message, MessageTarget.DELETE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void editPrivateMessage(Long messageId, String messageText, String username) {
        messageRepository.findById(messageId).ifPresent(message -> {
            try {
                if (message.getSender().getUsername().equals(userService.getMyUsername())) {
                    message.setMessageText(messageText);
                    message.setCurrentDateTime(true);
                    message.setTarget(MessageTarget.UPDATE);
                    message.setTargetId(messageId);
                    messageRepository.save(message);
                    messageNotificator.notifyUser(username, messageMapper.convertToTargetedMessageDto(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public List<TargetedMessageDto> getMessageHistory(User sender, User receiver) {

        List<Message> messageHistoryList = messageRepository.findAllBySenderAndReceiver(sender, receiver);
        List<TargetedMessageDto> targetedMessageDtoList = new ArrayList<>();
        for (Message message : messageHistoryList) {
            boolean shouldChangeTarget = message.getTarget() != MessageTarget.CREATE && message.getTarget() != MessageTarget.CHAT_DELETE;
            targetedMessageDtoList.add(
                    shouldChangeTarget
                            ?
                            messageMapper.convertToTargetedMessageDto(
                                    message, MessageTarget.CREATE)
                            :
                            messageMapper.convertToTargetedMessageDto(message)
            );
        }
        return targetedMessageDtoList;
    }

    public void deleteChatLocally(String contactToDelete) {
        userService.getMyUserOptional().ifPresent(myUser -> {
            myUser.getContactUsernameSet().remove(contactToDelete);
            userRepository.save(myUser);
            contactNotificator.updateContacts(myUser.getUsername());
            userRepository.findByUsername(contactToDelete).ifPresent(userToDelete -> {
                messageRepository.findAllBySenderAndReceiver(myUser,userToDelete)
                        .stream()
                        .map(message -> messageMapper.convertToTargetedMessageDto(message,MessageTarget.DELETE))
                        .toList()
                        .forEach(targetedMessageDto -> {
                            messageNotificator.notifyUser(contactToDelete,targetedMessageDto);
                        });
            });


        });
    }

    public void sendChatDeletionRequest(String contact) {
        userService.getMyUserOptional().
                ifPresent(myUser -> userRepository
                        .findByUsername(contact)
                        .ifPresent(contactUser -> {
                            if (votingRepository.findByMembers(myUser, contactUser).isEmpty()) {
                                votingRepository.save(new VotingToDeleteChat(myUser, contactUser));
                                String messageText = "@" + myUser.getUsername() + " отправил запрос на полное удаление чата, вы подтверждаете удаление?";
                                Message message = new Message(messageText, myUser, contactUser);
                                message.setTarget(MessageTarget.CHAT_DELETE);
                                messageRepository.save(message);
                                messageNotificator.notifyUser(
                                        contact,
                                        messageMapper.convertToTargetedMessageDto(message, MessageTarget.CHAT_DELETE));
                            }
                        }
                        )
                );
    }
    public void voteToChatDeletion(String username, String vote) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        Optional<User> myUserOptional = userService.getMyUserOptional();
        if(myUserOptional.isEmpty() || userOptional.isEmpty()) {
            return;
        }
        User myUser = myUserOptional.get();
        User user = userOptional.get();
        if(!myUser.getContactUsernameSet().contains(username) || !user.getContactUsernameSet().contains(myUser.getUsername())) {
            return;
        }
        Optional<VotingToDeleteChat> votingToDeleteChat = votingRepository.findByMembers(myUser,user);
        if(votingToDeleteChat.isEmpty()) {
            return;
        }
        VotingToDeleteChat voting = votingToDeleteChat.get();
        System.out.println("myUsername: " + myUser.getUsername() + " user: " + user.getUsername());
        if(voting.getVotedUsernames().contains(myUser.getUsername())) {
            return;
        }
        if(vote.equals("true")) {
            voting.setConfirmVotesAmount(voting.getConfirmVotesAmount() + 1);
        } else {
            voting.setDeclineVotesAmount(voting.getDeclineVotesAmount() + 1);
        }
        if (myUser.getUsername().equals(user.getUsername()) ? voting.getConfirmVotesAmount().equals(1L) : voting.getConfirmVotesAmount().equals(2L)) {
            votingRepository.delete(voting);
            List<Message> messageHistoryList = messageRepository.findAllBySenderAndReceiver(myUser,user);
            List<TargetedMessageDto> targetedMessageDtoHistoryList = messageHistoryList.stream().map(
                    message -> messageMapper.convertToTargetedMessageDto(
                            message, MessageTarget.DELETE)).toList();
            targetedMessageDtoHistoryList.forEach(targetedMessageDto -> {
                System.out.println(targetedMessageDto);
                messageNotificator.notifyUser(user.getUsername(),targetedMessageDto);
            });

            deleteChat(myUser,user);
            contactNotificator.updateContacts(user.getUsername());

            return;
        }
        voting.getVotedUsernames().add(myUser.getUsername());
        votingRepository.save(voting);
    }
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public void sendMessageWithImage(String caption,String receiverName,MultipartFile image) {
        findUserByUsername(receiverName).ifPresent(receiver -> {
            userService.getMyUserOptional().ifPresent(myUser -> {
                Message message = new Message(caption,myUser,receiver);
                String imageFilename = imageStorageService.uploadImage(image,pinnedImagesFolderPath);
                message.setPinnedImageFilename(imageFilename);
                messageRepository.save(message);
                TargetedMessageDto messageDto = messageMapper.convertToTargetedMessageDto(message);
                messageNotificator.notifyUser(receiverName,messageDto);
            });
        });
    }
    public void deleteChat(User member1, User member2) {
        messageRepository.findAllBySenderAndReceiver(member1, member2).forEach(message -> {
            if (message.getPinnedImageFilename() != null) {
                imageStorageService.deleteImage(pinnedImagesFolderPath + "/" + message.getPinnedImageFilename());
            }
        });
        member1.getContactUsernameSet().remove(member2.getUsername());
        member2.getContactUsernameSet().remove(member1.getUsername());
        userRepository.save(member1);
        userRepository.save(member2);
    }
}

