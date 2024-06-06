package net.study.model.redis;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {
    private Long userId;
    private ActivityType currentActivityType;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastOnlineDateTime;

}
