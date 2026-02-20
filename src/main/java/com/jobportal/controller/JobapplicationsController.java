package com.jobportal.controller;

import com.jobportal.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import com.jobportal.repository.JobapplicationsRepo;
import com.jobportal.model.Jobapplications;
import com.jobportal.model.Users;

@Controller
@RequestMapping("/applications")
public class JobapplicationsController {

    @Autowired
    private JobapplicationsRepo repository;

    @Autowired
    private UserRepo userRepository;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> apply(@RequestBody Jobapplications application,
                                   Authentication authentication) {

        String username = authentication.getName(); // email или логин

        // Находим пользователя
        Users user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        application.setApplicantId(user.getUserId().intValue());

        application.setStatus("pending");
        application.setAppliedAt(LocalDateTime.now());

        repository.save(application);

        return ResponseEntity.ok("Application created");
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyApplications(Authentication authentication) {
        try {
            String username = authentication.getName();

            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            java.util.List<Jobapplications> applications = repository.findByApplicantId(user.getUserId().intValue());

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

}