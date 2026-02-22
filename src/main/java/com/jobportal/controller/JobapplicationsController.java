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

    @PostMapping("/add/hired")
    @ResponseBody
    public ResponseEntity<?> applyhired(@RequestBody Jobapplications application,
                                   Authentication authentication) {

        String username = authentication.getName(); // email или логин

        // Находим пользователя
        Users user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        application.setApplicantId(user.getUserId().intValue());

        application.setStatus("hired");
        application.setAppliedAt(LocalDateTime.now());

        repository.save(application);

        return ResponseEntity.ok("Application created");
    }

    @PutMapping("/{applicationId}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer applicationId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication
    ) {
        try {

            String username = authentication.getName();

            Users employer = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Jobapplications jobapplication = repository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!java.util.List.of("pending", "interview", "invited", "rejected")
                    .contains(request.getStatus())) {
                return ResponseEntity.badRequest().body("Invalid status");
            }

            jobapplication.setStatus(request.getStatus());
            repository.save(jobapplication);

            return ResponseEntity.ok("Status updated");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
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

    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}