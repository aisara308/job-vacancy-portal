package com.jobportal.controller;

import com.jobportal.model.Chat;
import com.jobportal.model.Message;
import com.jobportal.model.Users;
import com.jobportal.service.ChatService;
import com.jobportal.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepo userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, UserRepo userRepo, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/get-or-create")
    @ResponseBody
    public ResponseEntity<ChatDTO> getOrCreateChat(
            @RequestParam Long user2Id,
            Authentication authentication) {

        String email = authentication.getName();

        Users currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long user1Id = currentUser.getUserId();

        if (!userRepo.existsById(user2Id)) {
            return ResponseEntity.badRequest().build();
        }

        Chat chat = chatService.getOrCreateChat(user1Id, user2Id);
        chat.getUser1().getFullName();
        chat.getUser2().getFullName();
        ChatDTO chatDTO = new ChatDTO(chat);
        return ResponseEntity.ok(chatDTO);
    }

    @GetMapping("/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable Long chatId, Authentication authentication) {
        String email = authentication.getName();
        Users currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, что пользователь участвует в чате
        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getUser1().getUserId().equals(currentUser.getUserId()) &&
                !chat.getUser2().getUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        // Получаем сообщения и преобразуем в DTO
        List<Message> messages = chatService.getMessages(chatId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    // Отправка сообщения через REST (необязательно, WebSocket лучше)
    @PostMapping("/{chatId}/send")
    @ResponseBody
    public ResponseEntity<Message> sendMessage(@PathVariable Long chatId,
                                               @RequestParam String content,
                                               Authentication authentication) {
        String email = authentication.getName();
        Users currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = chatService.saveMessage(chatId, currentUser.getUserId(), content);

        // Отправляем через WebSocket подписчикам
        messagingTemplate.convertAndSend("/topic/chat." + chatId, message);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/my-chats")
    @ResponseBody
    public ResponseEntity<List<UserChatDTO>> getMyChats(Authentication authentication) {
        String email = authentication.getName();
        Users currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Получаем все чаты пользователя
        List<Chat> userChats = chatService.getChatsByUserId(currentUser.getUserId());

        // Преобразуем в DTO с дополнительной информацией
        List<UserChatDTO> chatDTOs = userChats.stream()
                .map(chat -> {
                    // Определяем другого участника чата
                    Users otherUser = chat.getUser1().getUserId().equals(currentUser.getUserId())
                            ? chat.getUser2()
                            : chat.getUser1();

                    // Получаем последнее сообщение в чате
                    Message lastMessage = chatService.getLastMessage(chat.getId());

                    // Проверяем, есть ли непрочитанные сообщения от другого пользователя
                    boolean hasUnread = chatService.hasUnreadMessages(chat.getId(), currentUser.getUserId());

                    return new UserChatDTO(
                            chat.getId(),
                            otherUser.getUserId(),
                            otherUser.getFullName(),
                            lastMessage != null ? lastMessage.getContent() : "Нет сообщений",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            lastMessage != null ? lastMessage.getSender().getUserId().equals(currentUser.getUserId()) : false,
                            hasUnread // <-- новое поле
                    );
                })
                // Сортируем по времени последнего сообщения (сначала новые)
                .sorted((dto1, dto2) -> {
                    if (dto1.getLastMessageTime() == null && dto2.getLastMessageTime() == null) return 0;
                    if (dto1.getLastMessageTime() == null) return 1;
                    if (dto2.getLastMessageTime() == null) return -1;
                    return dto2.getLastMessageTime().compareTo(dto1.getLastMessageTime());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatDTOs);
    }

    @PutMapping("/{chatId}/read")
    @ResponseBody
    public ResponseEntity<?> markChatAsRead(
            @PathVariable Long chatId,
            Authentication authentication
    ) {

        // Получаем текущего пользователя ТАК ЖЕ как у тебя в my-chats
        String email = authentication.getName();

        Users currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Помечаем сообщения как прочитанные
        chatService.markAsRead(chatId, currentUser.getUserId());

        return ResponseEntity.ok().build();
    }
}

class UserChatDTO {
    private Long chatId;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean lastMessageFromMe;
    private boolean hasUnreadMessages;

    public UserChatDTO(Long chatId, Long otherUserId, String otherUserName,
                       String lastMessage, LocalDateTime lastMessageTime,
                       boolean lastMessageFromMe, boolean hasUnreadMessages) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageFromMe = lastMessageFromMe;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    // Геттеры
    public Long getChatId() { return chatId; }
    public Long getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public boolean isLastMessageFromMe() { return lastMessageFromMe; }
    public boolean isHasUnreadMessages() {
        return hasUnreadMessages;
    }
}

class ChatDTO {
    private Long chatId;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;

    public ChatDTO(Chat chat) {
        this.chatId = chat.getId();
        this.user1Id = chat.getUser1().getUserId();
        this.user1Name = chat.getUser1().getFullName();
        this.user2Id = chat.getUser2().getUserId();
        this.user2Name = chat.getUser2().getFullName();
    }

    public Long getChatId() { return chatId; }
    public Long getUser1Id() { return user1Id; }
    public String getUser1Name() { return user1Name; }
    public Long getUser2Id() { return user2Id; }
    public String getUser2Name() { return user2Name; }
}
