package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.model.redis.ActivityType;
import net.study.model.redis.UserActivity;
import net.study.service.UserActivityRedisService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserActivityController {
    private final UserActivityRedisService userActivityRedisService;

    @RequestMapping("/mark-online-status")
    public void markMyUserAsOnline() {
        userActivityRedisService.setMyUserActivityType(ActivityType.ONLINE);
    }

    @RequestMapping("/mark-offline-status")
    public void markMyUserAsOffline() {
        userActivityRedisService.setMyUserActivityType(ActivityType.OFFLINE);
    }

    @RequestMapping("/get-my-user-activity")
    public Map<String, String> getMyUserActivity() {
        Optional<UserActivity> myUserActivityOptional = userActivityRedisService.getMyUserActivity();
        return Map.of("my_user_activity", (myUserActivityOptional.isPresent()) ? myUserActivityOptional.get().toString() : "не удалось получить активность");
    }
}
