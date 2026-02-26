package com.jobportal.repository;

import com.jobportal.model.Message;
import com.jobportal.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
}