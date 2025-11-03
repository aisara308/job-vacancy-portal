package com.jobportal.controller;

import com.jobportal.model.Users;
import com.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
