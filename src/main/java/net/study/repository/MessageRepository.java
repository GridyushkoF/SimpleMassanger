package net.study.repository;

import net.study.model.Message;
import net.study.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query("SELECT m FROM Message m WHERE (m.receiver = :receiver AND m.sender = :sender) OR (m.receiver = :sender AND m.sender = :receiver) ORDER BY m.id ASC")
    List<Message> findAllBySenderAndReceiver(@Param("sender") User sender, @Param("receiver") User receiver);
}
