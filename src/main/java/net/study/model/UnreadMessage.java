package net.study.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.study.model.user.User;
@Entity
@Table(name = "unread_messages")
@NoArgsConstructor
public @Data class UnreadMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private User contact;
    private Integer unreadMessagesAmount;
    public UnreadMessage(User user, User contact, Integer unreadMessagesAmount) {
        this.user = user;
        this.contact = contact;
        this.unreadMessagesAmount = unreadMessagesAmount;
    }
}
