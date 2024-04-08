package net.study.service.websocket;

import lombok.RequiredArgsConstructor;
import net.study.dto.TargetedMessageDto;
import net.study.service.MyUserDataStorageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageNotificatorService {
    private final SimpMessagingTemplate messagingTemplate;
    private final MyUserDataStorageService myUserData;

    public void notifyUser(String username, TargetedMessageDto message) {
        String myUserName = myUserData.getMyUsername();
        if(!myUserName.equals(username)) {
            messagingTemplate.convertAndSendToUser(myUserName,"/topic/private-messages",message);
        }
        messagingTemplate.convertAndSendToUser(username,"/topic/private-messages",message);
    }

    public void notifyUserByBatch(String username, List<TargetedMessageDto> operationsBatch) {
        String myUserName = myUserData.getMyUsername();
        if(!myUserName.equals(username)) {
            messagingTemplate.convertAndSendToUser(myUserName,"/topic/private-messages",operationsBatch);
        }
        messagingTemplate.convertAndSendToUser(username,"/topic/private-messages", Map.of("operations_batch",operationsBatch));
    }
}
