package net.study.mapping;

import net.study.dto.MessageDto;
import net.study.dto.MessageTarget;
import net.study.dto.TargetedMessageDto;
import net.study.model.Message;

public class MessageMapper implements DtoConverter<MessageDto, Message> {
    @Override
    public MessageDto convertToDto(Message message) {
        return new MessageDto(message.getMessageText(),message.getId());
    }
    public TargetedMessageDto convertToTargetedMessageDto(
            Message message, MessageTarget target) {
        return new TargetedMessageDto(
                target,
                message.getId(),
                message.getMessageText(),
                message.getSender(),
                message.getReceiver(),
                message.getDateTime(),
                message.getPinnedImageFilename()
        );
    }
    public TargetedMessageDto convertToTargetedMessageDto(
            Message message) {
        return new TargetedMessageDto(
                message.getTarget(),
                message.getId(),
                message.getMessageText(),
                message.getSender(),
                message.getReceiver(),
                message.getDateTime(),
                message.getPinnedImageFilename()
        );
    }
}