package net.study.service.websocket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.study.model.redis.ActivityType;
import net.study.service.UserActivityRedisService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler implements WebSocketHandler {

    private final UserActivityRedisService userActivityService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        userActivityService.setMyUserActivityType(ActivityType.ONLINE);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus)  {
        userActivityService.setMyUserActivityType(ActivityType.OFFLINE);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {

    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {

    }
}