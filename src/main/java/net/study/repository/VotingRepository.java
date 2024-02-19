package net.study.repository;

import net.study.model.VotingToDeleteChat;
import net.study.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VotingRepository extends JpaRepository<VotingToDeleteChat,Long> {
    @Query("SELECT v FROM VotingToDeleteChat v WHERE (v.member2 = :member1 AND v.member1 = :member2) OR (v.member2 = :member2 AND v.member1 = :member1) ORDER BY v.id ASC")
    Optional<VotingToDeleteChat> findByMembers(User member1, User member2);
}
