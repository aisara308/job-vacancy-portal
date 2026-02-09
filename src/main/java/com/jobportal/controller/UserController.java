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

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Users user) {
        System.out.println(">>> REGISTER CALLED: " + user);
        String token = service.register(user);

        Map<String, String> response = new HashMap<>();
        if (token == null) {
            response.put("token", "Registration failed");
        } else {
            response.put("token", token);
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Users user) {
        String token = service.verify(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
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
