package net.study.service.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatClearingNotificator {
    private final SimpMessagingTemplate messagingTemplate;
    public void notifyUserAboutClearing(String username) {
        messagingTemplate.convertAndSendToUser(username,"/topic/chat-clearing","");
    }
}
