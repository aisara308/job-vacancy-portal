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

@RestController
public class UserController {

    @Autowired
    private UserService service;
    @PostMapping("/send-reset-code")
    public ResponseEntity<?> sendResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean sent = service.generateAndSendResetCode(email);

        if (!sent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean valid = service.verifyResetCode(email, code);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password-final")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        service.updatePassword(email, newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Users user) {
        System.out.println(">>> REGISTER CALLED: " + user);
        String token = service.register(user);
        Users dbUser = service.findByEmail(user.getEmail());

        Map<String, String> response = new HashMap<>();
        if (token == null) {
            response.put("token", "Registration failed");
        } else {
            response.put("token", token);
            response.put("userType", dbUser.getUserType());
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Users user) {
        String result = service.verify(user);
        Map<String, String> response = new HashMap<>();

        switch(result) {
            case "wrong_password":
                response.put("message", "Қате құпиясөз");
                break;
            case "user_not_found":
                response.put("message", "Пайдаланушы табылмады");
                break;
            default:
                Users dbUser = service.findByEmail(user.getEmail());
                response.put("token", result);
                response.put("userType", dbUser.getUserType());
                break;
        }

        return response;
    }


    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        String Email = authentication.getName();
        Users user = service.findByEmail(Email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        String Email = authentication.getName();
        boolean deleted = service.deleteByEmail(Email);

        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Аккаунт жойылды"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Пайдаланушы табылмады"));
        }
    }


}
