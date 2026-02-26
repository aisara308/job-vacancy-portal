package com.jobportal.service;

import com.jobportal.model.Chat;
import com.jobportal.model.Message;
import com.jobportal.model.Users;
import com.jobportal.repository.ChatRepo;
import com.jobportal.repository.MessageRepo;
import com.jobportal.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ChatRepo chatRepo;
    private final MessageRepo messageRepo;
    private final UserRepo userRepo;

    public ChatService(ChatRepo chatRepo, MessageRepo messageRepo, UserRepo userRepo) {
        this.chatRepo = chatRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    public Chat getOrCreateChat(Long id1, Long id2) {
        Long first = Math.min(id1, id2);
        Long second = Math.max(id1, id2);

        return chatRepo.findExistingChat(first, second)
                .orElseGet(() -> {
                    Users user1 = userRepo.findById(first).orElseThrow();
                    Users user2 = userRepo.findById(second).orElseThrow();

                    Chat chat = new Chat();
                    chat.setUser1(user1);
                    chat.setUser2(user2);

                    return chatRepo.save(chat);
                });
    }

    public Optional<Chat> getChatById(Long chatId) {
        return chatRepo.findById(chatId);
    }

    public Message saveMessage(Long chatId,
                               Long senderId,
                               String content) {

        // 1️⃣ Получаем чат
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // 2️⃣ 🔐 ПРОВЕРКА ДОСТУПА (ВОТ СЮДА!)
        if (!chat.getUser1().getUserId().equals(senderId) &&
                !chat.getUser2().getUserId().equals(senderId)) {

            throw new RuntimeException("Access denied");
        }

        // 3️⃣ Получаем отправителя
        Users sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4️⃣ Создаём сообщение
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        // 5️⃣ Сохраняем
        return messageRepo.save(message);
    }

    // Получение всех сообщений чата
    public List<Message> getMessages(Long chatId) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id: " + chatId));
        return messageRepo.findByChatOrderByCreatedAtAsc(chat);
    }
}