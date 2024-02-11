package net.study.repository;

import net.study.model.PrivateChat;
import net.study.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivateChatRepository extends JpaRepository<PrivateChat,Long> {
    @Query("SELECT c FROM PrivateChat c " +
            "WHERE (c.member1 = :member1 AND c.member2 = :member2) " +
            "OR (c.member1 = :member2 AND c.member2 = :member1) " +
            "ORDER BY c.id ASC")
    Optional<PrivateChat> findByMembers(User member1, User member2);
}
