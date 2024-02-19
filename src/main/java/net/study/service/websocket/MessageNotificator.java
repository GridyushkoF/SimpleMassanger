package net.study.service.websocket;

import lombok.RequiredArgsConstructor;
import net.study.dto.TargetedMessageDto;
import net.study.service.UserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageNotificator {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public void notifyUser(String username, TargetedMessageDto message) {
        try {
            String myUserName = userService.getMyUsername();
            if(!myUserName.equals(username)) {
                messagingTemplate.convertAndSendToUser(myUserName,"/topic/private-messages",message);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        messagingTemplate.convertAndSendToUser(username,"/topic/private-messages",message);
    }

}
