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
import java.util.List;

import com.jobportal.repository.JobapplicationsRepo;
import com.jobportal.model.Jobapplications;
import com.jobportal.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/applications")
public class JobapplicationsController {

    private static final Logger logger = LoggerFactory.getLogger(JobapplicationsController.class);

    @Autowired
    private JobapplicationsRepo repository;

    @Autowired
    private UserRepo userRepository;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> apply(@RequestBody Jobapplications application,
                                   Authentication authentication) {

        String username = authentication.getName();
        logger.info("Өтініш беру әрекеті басталды: applicantEmail={}, resumeId={}, vacancyId={}",
                username, application.getResumeId(), application.getVacancyId());

        try{// Находим пользователя
            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            application.setApplicantId(user.getUserId().intValue());

            boolean alreadyExists = repository.existsByResumeIdAndVacancyId(
                    application.getResumeId(),
                    application.getVacancyId()
            );

            if (alreadyExists) {
                logger.warn("Қайталап өтініш берілді: applicantEmail={}, resumeId={}, vacancyId={}",
                        username, application.getResumeId(), application.getVacancyId());
                return ResponseEntity
                        .badRequest()
                        .body("Сіз осы түйіндемемен осы вакансияға өтініш бергенсіз.");
            }

            application.setStatus("pending");
            application.setAppliedAt(LocalDateTime.now());

            repository.save(application);
            logger.info("Өтініш сәтті жасалды: applicantEmail={}, resumeId={}, vacancyId={}",
                    username, application.getResumeId(), application.getVacancyId());

            return ResponseEntity.ok("Application created");
        }catch (Exception e) {
            logger.error("Өтініш беру кезінде қате шықты: applicantEmail={}, resumeId={}, vacancyId={}",
                    username, application.getResumeId(), application.getVacancyId(), e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer applicationId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        logger.info("Өтініш статусы жаңарту әрекеті басталды: employerEmail={}, applicationId={}, status={}",
                username, applicationId, request.getStatus());
        try {

            Users employer = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            Jobapplications jobapplication = repository.findById(applicationId)
                    .orElseThrow(() -> {
                        logger.warn("Өтініш табылмады: applicationId={}", applicationId);
                        return new RuntimeException("Application not found");
                    });

            if (!java.util.List.of("pending", "interview", "invited", "rejected")
                    .contains(request.getStatus())) {
                logger.warn("Жарамсыз статус берілді: status={}", request.getStatus());
                return ResponseEntity.badRequest().body("Invalid status");
            }

            jobapplication.setStatus(request.getStatus());
            repository.save(jobapplication);

            logger.info("Өтініш статусы сәтті жаңартылды: applicationId={}, status={}",
                    applicationId, request.getStatus());

            return ResponseEntity.ok("Status updated");

        } catch (Exception e) {
            logger.error("Өтініш статусы жаңарту кезінде қате шықты: applicationId={}, status={}",
                    applicationId, request.getStatus(), e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyApplications(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Менің өтініштерім сұралды: applicantEmail={}", username);

        try {
            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            List<Jobapplications> applications = repository.findByApplicantId(user.getUserId().intValue());
            logger.debug("Табылған өтініштер саны: {}", applications.size());

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("Өтініштерді алу кезінде қате шықты: applicantEmail={}", username, e);
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