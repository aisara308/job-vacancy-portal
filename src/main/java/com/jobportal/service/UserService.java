package com.jobportal.service;

import com.jobportal.model.Users;
import com.jobportal.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private JavaMailSender mailSender;

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
            throw e;
        }
    }


    public String verify(Users user) {
        Users dbUser = repo.findByEmail(user.getEmail()).orElse(null);
        if (dbUser == null) {
            return "user_not_found";
        }

        try {
            Authentication authentication =
                    authManager.authenticate(
                            new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPasswordHash()));

            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(user.getEmail());
            } else {
                return "fail";
            }
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return "wrong_password";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
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

    public boolean generateAndSendResetCode(String email) {

        Optional<Users> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return false;
        }

        Users user = optionalUser.get();

        // Генерация 6-значного кода
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(10));

        repo.save(user);
        // Отправка email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("mc.dindon1898@gmail.com");
            message.setTo(email);
            message.setSubject("Password Reset Code");
            message.setText("Сіздің құпиясөзді қалпына келтіру кодыңыз: " + code +
                    "\nКод 10 минут ішінде жарамсыз болады.");

            mailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyResetCode(String email, String code) {
        Optional<Users> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) return false;

        Users user = optionalUser.get();
        return user.isResetCodeValid(code);
    }

    public void updatePassword(String email, String newPassword) {

        Optional<Users> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return;
        }

        Users user = optionalUser.get();

        user.setPasswordHash(encoder.encode(newPassword));

        // очищаем код после использования
        user.setResetCode(null);
        user.setResetCodeExpiry(null);

        repo.save(user);
    }
}
