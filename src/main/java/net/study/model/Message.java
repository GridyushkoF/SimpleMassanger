package net.study.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "sender_id")
    @ManyToOne
    private User sender;
    @JoinColumn(name = "receiver_id")
    @ManyToOne
    private User receiver;
    private String dateTime;
    private Long chatId;
    public Message(String messageText, User sender, User receiver) {
        this.messageText = messageText;
        this.sender = sender;
        this.receiver = receiver;
        setCurrentDateTime (false);
    }
    public void setCurrentDateTime (boolean isChanged) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedDate = now.format(formatter);
        dateTime = isChanged ? "изм. " + formattedDate : formattedDate;
    }

}
