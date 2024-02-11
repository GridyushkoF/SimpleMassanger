package net.study.mapping;

import net.study.dto.MessageDto;
import net.study.dto.MessageDtoWithGoal;
import net.study.dto.MessageGoal;
import net.study.model.Message;

public class MessageMapper implements DtoConverter<MessageDto, Message> {
    @Override
    public MessageDto convertToDto(Message message) {
        return new MessageDto(message.getMessageText(),message.getId());
    }
    public MessageDtoWithGoal convertToMessageDtoWithGoal(
            Message message,
            MessageGoal goal) {
        return new MessageDtoWithGoal(
                message.getMessageText(),
                goal,
                message.getId(),
                message.getSender().getUsername(),
                message.getDateTime()
        );
    }
}