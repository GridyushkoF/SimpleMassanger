package net.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.study.model.user.User;


@NoArgsConstructor
@AllArgsConstructor
public @Data class TargetedMessageDto {
    private MessageTarget target;
    private Long targetId;
    private String messageText;
    private User sender;
    private User receiver;
    private String dateTime;
    private String pinnedImageFilename;
}

