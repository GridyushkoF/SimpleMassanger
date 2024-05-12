package net.study.repository;

import net.study.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String name);
    boolean existsByUsername(String name);

    @Query("SELECT u FROM User u JOIN u.contactUsernameSet c WHERE c = :username")
    List<User> findAllByContactUsernameSetContains(@Param("username") String username);

}