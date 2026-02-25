package com.jobportal.controller;

import com.jobportal.model.Users;
import com.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService service;
    @PostMapping("/send-reset-code")
    public ResponseEntity<?> sendResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Құпиясөзді қалпына келтіру коды жіберілуде: email={}", email);
        try {
            boolean sent = service.generateAndSendResetCode(email);

            if (!sent) {
                logger.warn("Құпиясөзді қалпына келтіру коды жіберілмеді: email={}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Құпиясөзді қалпына келтіру коды сәтті жіберілді: email={}", email);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            logger.error("Құпиясөзді қалпына келтіру кезінде қате шықты: email={}", email, e);
            return ResponseEntity.status(500).body("Қате пайда болды");
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        logger.info("Қалпына келтіру кодын тексеру: email={}", email);

        try {
            boolean valid = service.verifyResetCode(email, code);

            if (!valid) {
                logger.warn("Қалпына келтіру коды жарамсыз: email={}, code={}", email, code);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            logger.info("Қалпына келтіру коды сәтті тексерілді: email={}", email);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            logger.error("Қалпына келтіру кодын тексеру кезінде қате шықты: email={}", email, e);
            return ResponseEntity.status(500).body("Қате пайда болды");
        }
    }

    @PostMapping("/reset-password-final")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        logger.info("Құпиясөзді қалпына келтіру орындалуда: email={}", email);

        try {
            service.updatePassword(email, newPassword);
            logger.info("Құпиясөз сәтті жаңартылды: email={}", email);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            logger.error("Құпиясөзді жаңарту кезінде қате шықты: email={}", email, e);
            return ResponseEntity.status(500).body("Қате пайда болды");
        }
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Users user) {
        logger.info("Пайдаланушы тіркелуге тырысуда: email={}", user.getEmail());
        logger.debug("Тіркеу деректері: {}", user);

        Map<String, String> response = new HashMap<>();

        try {
            String token = service.register(user);
            Users dbUser = service.findByEmail(user.getEmail());

            if (token == null) {
                logger.warn("Тіркеу сәтсіз аяқталды: email={}", user.getEmail());
                response.put("token", "Тіркеу сәтсіз аяқталды");
            } else {
                logger.info("Пайдаланушы сәтті тіркелді: email={}", user.getEmail());
                response.put("token", token);
                response.put("userType", dbUser.getUserType());
            }
        }catch (Exception e) {
            logger.error("Тіркеу кезінде қате шықты: email={}", user.getEmail(), e);
            response.put("token", "Қате пайда болды");
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Users user) {
        logger.info("Пайдаланушы кіруге тырысуда: email={}", user.getEmail());

        Map<String, String> response = new HashMap<>();
        try {
            String result = service.verify(user);

            switch(result) {
                case "wrong_password":
                    logger.warn("Қате құпиясөз: email={}", user.getEmail());
                    response.put("message", "Қате құпиясөз");
                    break;
                case "user_not_found":
                    logger.warn("Пайдаланушы табылмады: email={}", user.getEmail());
                    response.put("message", "Пайдаланушы табылмады");
                    break;
                default:
                    Users dbUser = service.findByEmail(user.getEmail());
                    logger.info("Пайдаланушы сәтті кірді: email={}", user.getEmail());
                    response.put("token", result);
                    response.put("userType", dbUser.getUserType());
                    break;
            }
        }catch (Exception e) {
            logger.error("Кіру кезінде қате шықты: email={}", user.getEmail(), e);
            response.put("message", "Қате пайда болды");
        }
        return response;
    }


    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Рұқсатсыз кіру әрекеті байқалды");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        String email = authentication.getName();
        logger.info("Пайдаланушы деректері сұралуда: email={}", email);

        try {
            Users user = service.findByEmail(email);

            if (user == null) {
                logger.warn("Пайдаланушы табылмады: email={}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            logger.info("Пайдаланушы деректері қайтарылды: email={}", email);
            return ResponseEntity.ok(user);
        }catch (Exception e) {
            logger.error("Пайдаланушы деректерін алу кезінде қате шықты: email={}", email, e);
            return ResponseEntity.status(500).body("Қате пайда болды");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Рұқсатсыз аккаунт жою әрекеті байқалды");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        String email = authentication.getName();
        logger.info("Пайдаланушы аккаунтын жою: email={}", email);
        try{
            boolean deleted = service.deleteByEmail(email);

            if (deleted) {
                logger.info("Аккаунт сәтті жойылды: email={}", email);
                return ResponseEntity.ok(Map.of("message", "Аккаунт жойылды"));
            } else {
                logger.warn("Аккаунт табылмады, жою мүмкін болмады: email={}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Пайдаланушы табылмады"));
            }
        }catch (Exception e) {
            logger.error("Аккаунтты жою кезінде қате шықты: email={}", email, e);
            return ResponseEntity.status(500).body("Қате пайда болды");
        }
    }
}
