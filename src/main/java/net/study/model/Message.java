package net.study.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.study.dto.MessageTarget;
import net.study.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
@Table(name = "messages")
@NoArgsConstructor
public @Data class Message {
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @JsonIgnore
    private Long id;
    @Column(name = "message_text",columnDefinition = "MEDIUMTEXT")
    private String messageText;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User receiver;
    private String dateTime;
    @Enumerated(EnumType.STRING)
    private MessageTarget target;
    private Long targetId;
    private String pinnedImageFilename;
    public Message(String messageText, User sender, User receiver) {
        this.messageText = messageText;
        this.sender = sender;
        this.receiver = receiver;
        setCurrentDateTime (false);
        target = MessageTarget.CREATE;
        targetId = id;
    }
    public void setCurrentDateTime (boolean isChanged) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedDate = now.format(formatter);
        dateTime = isChanged ? "изм. " + formattedDate : formattedDate;
    }

}
