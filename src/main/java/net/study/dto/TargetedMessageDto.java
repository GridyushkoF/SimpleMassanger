package net.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.study.model.Message;
import net.study.model.OriginalMessageStatus;
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
    private User forwarder;
    private Message originalMessageWeReplied;
    private OriginalMessageStatus originalMessageStatus;
}

