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
import java.util.Optional;

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

            // Токен email арқылы жасалады
            String token = jwtService.generateToken(savedUser.getEmail());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String verify(Users user) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPasswordHash()));

        if(authentication.isAuthenticated()){
            return jwtService.generateToken(user.getEmail());
        }
        return "fail";
    }

    public Users findByEmail(String email) {
        return repo.findByEmail(email)
                .map(user -> {
                    user.setPasswordHash(null); // парольді жасыру
                    return user;
                })
                .orElse(null); // егер табылмаса null қайтарады
    }

    public boolean deleteByEmail(String email) {
        Optional<Users> optionalUser = repo.findByEmail(email);
        if(optionalUser.isPresent()){
            repo.delete(optionalUser.get());
            return true;
        }
        return false;
    }


    public JWTService getJwtService() {
        return jwtService;
    }
}
