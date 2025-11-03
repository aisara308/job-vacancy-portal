package com.jobportal.service;

import com.jobportal.model.Users;
import com.jobportal.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String register(Users user) {
        try {
            System.out.println(">>> Saving user: " + user.getEmail());
            user.setPasswordHash(encoder.encode(user.getPasswordHash()));
            Users savedUser = repo.save(user);

            String token = jwtService.generateToken(savedUser.getFullName());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String verify(Users user) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getFullName(), user.getPasswordHash()));

        if(authentication.isAuthenticated()){
            return jwtService.generateToken(user.getFullName());
        }
        return "fail";
    }


    public JWTService getJwtService() {
        return jwtService;
    }
}
