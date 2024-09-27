package net.study.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.study.model.user.User;

import java.util.Set;

@Entity
@NoArgsConstructor
public @Data class VotingToDeleteChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private Long confirmVotesAmount;
    private Long declineVotesAmount;
    @ElementCollection
    private Set<String> votedUsernames;
    @ManyToOne(cascade = CascadeType.DETACH)
    private User member1;
    @ManyToOne(cascade = CascadeType.DETACH)
    private User member2;
    public VotingToDeleteChat(User member1, User member2) {
        confirmVotesAmount = 0L;
        declineVotesAmount = 0L;
        this.member1 = member1;
        this.member2 = member2;
    }
}
