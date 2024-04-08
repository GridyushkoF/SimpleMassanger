package net.study.service.chat;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageTarget;
import net.study.mapping.MessageDtoMapper;
import net.study.model.Message;
import net.study.model.VotingToDeleteChat;
import net.study.model.user.User;
import net.study.repository.MessageRepository;
import net.study.repository.UserRepository;
import net.study.repository.VotingRepository;
import net.study.service.MyUserDataStorageService;
import net.study.service.websocket.ChatClearingNotificator;
import net.study.service.websocket.ContactNotificatorService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatDeletionService {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ContactNotificatorService contactNotificator;
    private final VotingRepository votingRepository;
    private final MyUserDataStorageService myUserData;
    private final ChatClearingNotificator chatClearingNotificator;
    private final ChatCrudService chatCrudService;
    private final MessageDtoMapper messageDtoMapper;
    public void deleteChatLocally(String contactToDelete) {
        User myUser = myUserData.getMyUser();
        myUser.getContactUsernameSet().remove(contactToDelete);
        userRepository.save(myUser);
        contactNotificator.updateContacts(myUser.getUsername());
        chatClearingNotificator.notifyUserAboutClearing(contactToDelete);
    }
    public void sendChatDeletionRequest(String contact) {
        System.out.println(userRepository.findByUsername(contact).isPresent());
        User myUser = myUserData.getMyUser();
        userRepository.findByUsername(contact).ifPresent(contactUser -> {
            if (votingRepository.findByMembers(myUser, contactUser).isEmpty()) {
                votingRepository.save(new VotingToDeleteChat(myUser, contactUser));
                String messageText = "@" + myUser.getUsername() + " отправил запрос на полное удаление чата, вы подтверждаете удаление?";
                Message message = new Message(messageText, myUser, contactUser);
                message.setTarget(MessageTarget.CHAT_DELETE);
                chatCrudService.sendMessageAndNotify(message);
            }
        });
    }

    public void voteToChatDeletion(String username, String vote) {
        User myUser = myUserData.getMyUser();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            return;
        }
        User user = userOptional.get();
        Optional<VotingToDeleteChat> votingToDeleteChatOptional = votingRepository.findByMembers(myUser,user);
        if(votingToDeleteChatOptional.isEmpty()) {
            return;
        }
        VotingToDeleteChat voting = votingToDeleteChatOptional.orElseThrow();

        if(!isUsersInEachOthersContacts(myUser,user)
                ||
                voting.getVotedUsernames().contains(myUser.getUsername())) {
            return;
        }
        if(vote.equals("true")) {
            voting.setConfirmVotesAmount(voting.getConfirmVotesAmount() + 1);
        } else {
            sendDeclineDeletionNotification(myUser, user, voting);
            return;
        }
        if (myUser.getUsername().equals(user.getUsername()) ? voting.getConfirmVotesAmount().equals(1L) : voting.getConfirmVotesAmount().equals(2L)) {
            sendAcceptDeletionNotification(myUser, user, voting);
            return;
        }
        voting.getVotedUsernames().add(myUser.getUsername());
        votingRepository.save(voting);
    }

    public void sendAcceptDeletionNotification(User myUser, User user, VotingToDeleteChat voting) {
        votingRepository.delete(voting);
        deleteChat(myUser, user);
    }

    public void sendDeclineDeletionNotification(User myUser, User user, VotingToDeleteChat voting) {
        voting.setDeclineVotesAmount(voting.getDeclineVotesAmount() + 1);
        Message message = new Message(
                "Запрос успешно отклонен!",
                myUser,
                user
        );
        chatCrudService.sendMessageAndNotify(message);
        votingRepository.delete(voting);
    }
    public void deleteChat(User member1, User member2) {
        List<Message> allMessagesInThisChat = messageRepository.findAllByMembers(member1, member2, Pageable.unpaged());
        chatClearingNotificator.notifyUserAboutClearing(member2.getUsername());
        if(!member1.getUsername().equals(member2.getUsername())) {
            chatClearingNotificator.notifyUserAboutClearing(member1.getUsername());
        }
        member1.getContactUsernameSet().remove(member2.getUsername());
        userRepository.save(member1);
        member2.getContactUsernameSet().remove(member1.getUsername());
        userRepository.save(member2);
        contactNotificator.updateContacts(member2.getUsername());
        contactNotificator.updateContacts(member1.getUsername());

        chatCrudService.deleteMessagesList(messageDtoMapper.convertEntityListToIdList(allMessagesInThisChat),true,false);
    }
    public boolean isUsersInEachOthersContacts (User user1, User user2) {
        return user1.getContactUsernameSet().contains(user2.getUsername())
                && user2.getContactUsernameSet().contains(user1.getUsername());
    }
}
