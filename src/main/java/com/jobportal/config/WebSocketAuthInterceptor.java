package com.jobportal.config;

import com.jobportal.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Перехватываем только CONNECT команду
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // Извлекаем email из токена
                    String email = jwtService.extractEmail(token);

                    if (email != null) {
                        // Загружаем пользователя
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        // Валидируем токен
                        if (jwtService.validateToken(token, userDetails)) {
                            // Создаем аутентификацию
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            // Устанавливаем пользователя в сессию WebSocket
                            accessor.setUser(authentication);

                            System.out.println("WebSocket аутентифицирован пользователь: " + email);
                        } else {
                            System.out.println("Токен невалидный для пользователя: " + email);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка аутентификации WebSocket: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("WebSocket подключение без токена авторизации");
            }
        }

        return message;
    }
}