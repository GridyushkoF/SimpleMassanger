package net.study.repository;

import net.study.model.UnreadMessage;
import net.study.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnreadMessageRepository extends JpaRepository<UnreadMessage,Long> {
    Optional<UnreadMessage> findByUserAndContact(User user, User contact);
}
