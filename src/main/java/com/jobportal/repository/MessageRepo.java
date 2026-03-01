package com.jobportal.repository;

import com.jobportal.model.Message;
import com.jobportal.model.Chat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
    Message findTopByChatIdOrderByCreatedAtDesc(Long chatId);
    @Modifying
    @Transactional
    @Query("""
        UPDATE Message m 
        SET m.isRead = true 
        WHERE m.chat.id = :chatId 
        AND m.sender.id <> :userId 
        AND m.isRead = false
    """)
    int markMessagesAsRead(Long chatId, Long userId);
    @Query("""
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM Message m
    WHERE m.chat.id = :chatId
    AND m.sender.id <> :userId
    AND m.isRead = false
""")
    boolean hasUnreadMessages(Long chatId, Long userId);
}