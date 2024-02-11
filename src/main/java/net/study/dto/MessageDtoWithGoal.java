package net.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public @Data class MessageDtoWithGoal {
    private String messageText;
    private MessageGoal goal;
    private Long goalId;
    private String senderName;
    private String dateTime;
}
