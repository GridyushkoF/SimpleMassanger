package net.study.model;

import jakarta.persistence.*;
import net.study.model.user.User;

@Entity
public class VotingToDeleteChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long votesAmountToDelete;
    private Long confirmVotesAmount;
    private Long declineVotesAmount;
    @ManyToOne
    private User originalVoterToDeletion;
}
