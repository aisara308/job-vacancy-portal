package com.jobportal.controller;

import com.jobportal.model.Message;
import com.jobportal.model.Users;
import com.jobportal.repository.UserRepo;
import com.jobportal.service.ChatService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.format.DateTimeFormatter;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserRepo userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService,
                                   UserRepo userRepo,
                                   SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO dto,
                            Authentication authentication) {

        String email = authentication.getName();
        System.out.println("Отправка сообщения от: " + email);

        Users sender = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Message saved = chatService.saveMessage(
                dto.getChatId(),
                sender.getUserId(),
                dto.getContent()
        );

        // ВАЖНО: преобразуем в DTO перед отправкой
        MessageDTO messageDTO = new MessageDTO(saved);

        // Добавляем имя отправителя если нужно
        if (messageDTO.getSenderName() == null) {
            messageDTO.setSenderName(sender.getFullName());
        }

        System.out.println("Отправка DTO: " + messageDTO.getContent());

        messagingTemplate.convertAndSend(
                "/topic/chat." + dto.getChatId(),
                messageDTO  // Отправляем DTO, а не saved
        );
    }
}

class ChatMessageDTO {
    private Long chatId;
    private String content;

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

class MessageDTO {
    private Long messageId;
    private String content;
    private String sentAt;
    private Long senderId;
    private String senderEmail;
    private String senderName;
    private Long chatId;

    // Пустой конструктор для Jackson
    public MessageDTO() {}

    // Конструктор из сущности Message
    public MessageDTO(Message message) {
        this.messageId = message.getId();
        this.content = message.getContent();

        // Форматируем дату
        if (message.getCreatedAt() != null) {
            this.sentAt = message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // Извлекаем информацию об отправителе
        Users sender = message.getSender();
        if (sender != null) {
            this.senderId = sender.getUserId();
            this.senderEmail = sender.getEmail();
            this.senderName = sender.getFullName();
        }

        // ID чата
        if (message.getChat() != null) {
            this.chatId = message.getChat().getId();
        }
    }

    // Геттеры и сеттеры
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}