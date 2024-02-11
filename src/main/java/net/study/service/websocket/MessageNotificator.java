package net.study.service.websocket;

import net.study.dto.MessageDtoWithGoal;
import net.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageNotificator {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Autowired
    public MessageNotificator(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }
    public void notifyUser(String username, MessageDtoWithGoal messageDtoWithGoal) {
        try {
            String myUserName = userService.getMyUsername();
            if(!myUserName.equals(username)) {
                messagingTemplate.convertAndSendToUser(myUserName,"/topic/private-messages",messageDtoWithGoal);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        messagingTemplate.convertAndSendToUser(username,"/topic/private-messages",messageDtoWithGoal);
    }

}
