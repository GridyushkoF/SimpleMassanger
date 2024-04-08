package net.study.mapping;

import lombok.RequiredArgsConstructor;
import net.study.dto.MessageDto;
import net.study.dto.MessageTarget;
import net.study.dto.TargetedMessageDto;
import net.study.model.Message;
import net.study.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class MessageDtoMapper {
    private final MessageRepository messageRepository;
    public TargetedMessageDto convertToTargetedDto(
            Message message, MessageTarget target) {
        return new TargetedMessageDto(
                target,
                message.getId(),
                message.getMessageText(),
                message.getSender(),
                message.getReceiver(),
                message.getDateTime(),
                message.getPinnedImageFilename(),
                message.getForwarder(),
                message.getOriginalMessageWeReplied(),
                message.getOriginalMessageStatus()
        );
    }
    public TargetedMessageDto convertToTargetedDto(
            Message message) {
        return new TargetedMessageDto(
                message.getTarget(),
                message.getId(),
                message.getMessageText(),
                message.getSender(),
                message.getReceiver(),
                message.getDateTime(),
                message.getPinnedImageFilename(),
                message.getForwarder(),
                message.getOriginalMessageWeReplied(),
                message.getOriginalMessageStatus()
        );
    }
    public MessageDto convertToDto(Message message) {
        return new MessageDto(message.getMessageText(),message.getId());
    }
    public List<Message> convertDtoListToEntityList (List<MessageDto> messageEntityDtoList) {
        return messageEntityDtoList.stream().map(messageDto -> messageRepository.findById(messageDto.getMessageId()))
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }
    public  List<Message> convertIdListToEntityList(List<Integer> ids) {
        return ids.stream().map(id -> messageRepository.findById(Long.valueOf(id)))
                .filter(Optional::isPresent).map(Optional::get).toList();
    }
    public List<Integer> convertEntityListToIdList(List<Message> messages) {
        return messages.stream().map(Message::getId).map(longId -> Integer.parseInt(longId.toString())).toList();
    }
}