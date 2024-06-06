package net.study.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.study.model.redis.ActivityType;
import net.study.model.redis.UserActivity;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class UserActivityRedisService {
    private final MyUserDataStorageService myUserData;
    private final ObjectMapper objectMapper;

    private static final int ONLINE_TIMEOUT = 6_000;

    public Optional<UserActivity> initMyUserActivityAndGet() {
        try (JedisPool pool = new JedisPool("localhost", 6379)) {
            Jedis jedis = pool.getResource();
            Long myUserId = myUserData.getMyUser().getId();
            UserActivity userActivity = new UserActivity(myUserId, ActivityType.ONLINE, LocalDateTime.now());
            String userActivityJson = objectMapper.writeValueAsString(userActivity);
            jedis.hset("userActivity:" + myUserId, "userActivity", userActivityJson);
            return Optional.of(userActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<UserActivity> getMyUserActivity() {
        try (JedisPool pool = new JedisPool("localhost", 6379)) {
            Jedis jedis = pool.getResource();
            Long myUserId = myUserData.getMyUser().getId();
            String userActivityJson = jedis.hget("userActivity:" + myUserId, "userActivity");
            UserActivity activity;
            if (userActivityJson == null) {
                Optional<UserActivity> initedMyUserActivity = initMyUserActivityAndGet();
                if (initedMyUserActivity.isEmpty()) {
                    return Optional.empty();
                }
                activity = initedMyUserActivity.get();
            } else {
                activity = objectMapper.readValue(userActivityJson, UserActivity.class);
            }
            return Optional.of(activity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    public void setMyUserActivityType(ActivityType activityType) {
        UserActivity myUserActivity = getMyUserActivityOrCreate();
        myUserActivity.setCurrentActivityType(activityType);
        if (!activityType.equals(ActivityType.OFFLINE)) {
            myUserActivity.setLastOnlineDateTime(LocalDateTime.now());
        }
        try (JedisPool pool = new JedisPool("localhost", 6379)) {
            Jedis jedis = pool.getResource();
            String userActivityJson = objectMapper.writeValueAsString(myUserActivity);
            jedis.hset("userActivity:" + myUserData.getMyUser().getId(), "userActivity", userActivityJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserActivity getMyUserActivityOrCreate() {
        UserActivity userActivity;
        Long myUserId = myUserData.getMyUser().getId();
        Optional<UserActivity> existsActivityOptional = getMyUserActivity();
        userActivity = existsActivityOptional.orElseGet(() -> new UserActivity(myUserId, ActivityType.ONLINE, LocalDateTime.now()));
        return userActivity;
    }

}
