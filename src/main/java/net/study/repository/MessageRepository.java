package net.study.repository;

import net.study.model.Message;
import net.study.model.OriginalMessageStatus;
import net.study.model.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query("SELECT m FROM Message m WHERE (m.receiver = :receiver AND m.sender = :sender) OR (m.receiver = :sender AND m.sender = :receiver) ORDER BY m.id DESC ")
    List<Message> findAllByMembers(@Param("sender") User member1, @Param("receiver") User member2, Pageable pageable);
    @Query("SELECT m FROM Message m WHERE (m.receiver = :receiver AND m.sender = :sender) OR (m.receiver = :sender AND m.sender = :receiver) ORDER BY m.id DESC")
    List<Message> findLastMessageBySenderAndReceiver(@Param("sender") User sender, @Param("receiver") User receiver, Pageable pageable);
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.id IN :ids")
    void deleteBatchByIds(List<Integer> ids);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.originalMessageWeReplied = null, m.originalMessageStatus = :status WHERE m.originalMessageWeReplied.id = :messageId")
    void detachReplyMessagesOfOriginalAndUpdateStatus(@Param("messageId") Long messageId, @Param("status") OriginalMessageStatus status);

    default Message findLastMessage(User user1, User user2) {
        Pageable pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "id");
        List<Message> messages = findLastMessageBySenderAndReceiver(user1, user2, pageable);
        return messages.isEmpty() ? null : messages.get(0);
    }
}
