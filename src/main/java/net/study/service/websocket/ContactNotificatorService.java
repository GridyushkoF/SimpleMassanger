package net.study.service.websocket;

import lombok.RequiredArgsConstructor;
import net.study.service.MyUserDataStorageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactNotificatorService {
    private final SimpMessagingTemplate messagingTemplate;
    private final MyUserDataStorageService myUserData;
    public void updateContacts (String username) {
        String myUsername = myUserData.getMyUsername();
        if(!myUsername.equals(username)) {
            messagingTemplate.convertAndSendToUser(myUsername,"/topic/private-messages",Map.of("need_contacts_update","true"));
        }
        messagingTemplate.convertAndSendToUser(username,"/topic/contacts", Map.of("need_contacts_update","true"));
    }

}
