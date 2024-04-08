package net.study.service.chat;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageTarget;
import net.study.dto.TargetedMessageDto;
import net.study.mapping.MessageDtoMapper;
import net.study.model.Message;
import net.study.model.user.User;
import net.study.repository.MessageRepository;
import net.study.repository.UserRepository;
import net.study.service.MyUserDataStorageService;
import net.study.util.MessagesPaginationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageHistoryService {
    private final UserRepository userRepository;
    private final MyUserDataStorageService myUserData;
    private final MessageRepository messageRepository;
    private final MessageDtoMapper messageDtoMapper;
    @Value("${pagination.messages.page-size}")
    private int messagesPageSize;
    @Transactional
    public String getLastMessage(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        User myUser = myUserData.getMyUser();
        User user = userOptional.orElse(null);
        Message lastMessage = messageRepository.findLastMessage(user,myUser);
        return lastMessage != null ? lastMessage.getMessageText() : "";
    }
    @Transactional
    public List<TargetedMessageDto> getMessageHistory(User sender, User receiver, Long messageIdBeforeWhichLoad, boolean shouldResetPageNumber) {
        if(shouldResetPageNumber) {
            MessagesPaginationUtil.reset();
        } else {
            MessagesPaginationUtil.incrementOnValue(1);
        }

        List<Message> messageHistoryList;
        if(messageIdBeforeWhichLoad != null) {
            messageHistoryList = getMessageHistoryByIdBeforeWhichLoad(messageIdBeforeWhichLoad,sender,receiver,MessagesPaginationUtil.getCurrentPage(),new ArrayList<>());
            if (messageHistoryList.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            Pageable pageable = PageRequest.of(MessagesPaginationUtil.getCurrentPage(), messagesPageSize);
            messageHistoryList = messageRepository.findAllByMembers(sender, receiver,pageable);
        }
        return getTargetedMessageDtoList(messageHistoryList);
    }
    private List<TargetedMessageDto> getTargetedMessageDtoList(List<Message> messageHistoryList) {
        Collections.reverse(messageHistoryList);
        List<TargetedMessageDto> targetedMessageDtoList = new ArrayList<>();
        for (Message message : messageHistoryList) {
            boolean shouldChangeTarget = message.getTarget() != MessageTarget.CREATE && message.getTarget() != MessageTarget.CHAT_DELETE;
            targetedMessageDtoList.add(
                    shouldChangeTarget
                            ?
                            messageDtoMapper.convertToTargetedDto(message, MessageTarget.CREATE)
                            :
                            messageDtoMapper.convertToTargetedDto(message)
            );
        }
        return targetedMessageDtoList;
    }
    @Transactional
    public List<Message> getMessageHistoryByIdBeforeWhichLoad(Long messageId, User member1, User member2, int pageNumber, List<Message> fullMessageHistoryList) {
        int currentPageNumber = pageNumber;

        Pageable pageable = PageRequest.of(pageNumber, messagesPageSize);
        List<Message> messageHistoryList = messageRepository.findAllByMembers(member1, member2, pageable);
        if (!messageHistoryList.isEmpty()) {
            fullMessageHistoryList.addAll(messageHistoryList);
            for (Message message : messageHistoryList) {
                if (message.getId().equals(messageId)) {
                    MessagesPaginationUtil.setPage(currentPageNumber);
                    return fullMessageHistoryList; // Возвращаем список, если нашли сообщение
                }
            }
            currentPageNumber++;
            // Используем результат рекурсивного вызова
            return getMessageHistoryByIdBeforeWhichLoad(messageId, member1, member2, currentPageNumber, fullMessageHistoryList);
        }
        // Если список пустой, возвращаем текущий полный список
        return new ArrayList<>();
    }
}
