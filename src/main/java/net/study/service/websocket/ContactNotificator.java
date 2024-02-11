package net.study.service.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ContactNotificator {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    public ContactNotificator(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    public void updateContacts (String username) {
        messagingTemplate.convertAndSendToUser(username,"/topic/contacts", Map.of("need_contacts_update","true"));
    }
}
