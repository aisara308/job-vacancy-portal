package com.jobportal.repository;
import com.jobportal.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepo extends JpaRepository<Chat, Long> {
    @Query("""
    SELECT c FROM Chat c
    WHERE (c.user1.userId = :user1 AND c.user2.userId = :user2)
       OR (c.user1.userId = :user2 AND c.user2.userId = :user1)
""")
    Optional<Chat> findExistingChat(@Param("user1") Long user1,
                                    @Param("user2") Long user2);
}
