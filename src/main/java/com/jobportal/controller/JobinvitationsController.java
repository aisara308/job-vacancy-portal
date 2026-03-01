package com.jobportal.controller;

import com.jobportal.model.Jobapplications;
import com.jobportal.model.Jobinvitations;

import com.jobportal.model.Resumes;
import com.jobportal.repository.JobinvitationsRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.repository.ResumeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jobportal.model.Users;

@Controller
@RequestMapping("/invitations")
public class JobinvitationsController {

    private static final Logger logger = LoggerFactory.getLogger(JobinvitationsController.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JobinvitationsRepo repository;

    @Autowired
    private ResumeRepo resumeRepo;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> sendInvitation(@RequestBody InvitationRequest request,
                                            Authentication authentication) {

        String username = authentication.getName();
        logger.info("Шақыру жіберу әрекеті басталды: employerEmail={}, resumeId={}, vacancyId={}",
                username, request.getResumeId(), request.getVacancyId());

        try{
            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            Jobinvitations invitation = new Jobinvitations();
            invitation.setResumeId(request.getResumeId());
            invitation.setVacancyId(request.getVacancyId());
            invitation.setEmployerId(user.getUserId().intValue());

            boolean alreadyExists = repository.existsByResumeIdAndVacancyId(
                    invitation.getResumeId(),
                    invitation.getVacancyId()
            );

            if (alreadyExists) {
                logger.warn("Шақыру бар: employerEmail={}, resumeId={}, vacancyId={}",
                        username, request.getResumeId(), request.getVacancyId());
                return ResponseEntity
                        .badRequest()
                        .body("Сіз осы вакансиямен осы түйіндемеге шақыру жібергенсіз.");
            }

            invitation.setStatus("sent");
            invitation.setSentAt(LocalDateTime.now());

            repository.save(invitation);
            logger.info("Шақыру сәтті жіберілді: employerEmail={}, resumeId={}, vacancyId={}",
                    username, request.getResumeId(), request.getVacancyId());

            return ResponseEntity.ok("Invitation sent");
        }catch (Exception e) {
            logger.error("Шақыру жіберу кезінде қате шықты: employerEmail={}, resumeId={}, vacancyId={}",
                    username, request.getResumeId(), request.getVacancyId(), e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyInvitation(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Менің жіберген шақыруларым сұралды: employerEmail={}", username);
        try {
            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            java.util.List<Jobinvitations> invitations = repository.findByEmployerId(user.getUserId().intValue());
            logger.debug("Табылған шақырулар саны: {}", invitations.size());

            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            logger.error("Шақыруларды алу кезінде қате шықты: employerEmail={}", username, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> getInvitationsForMyResumes(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Менің түйіндемелеріме шақырулар сұралды: applicantEmail={}", username);
        try {

            // Находим пользователя (работодателя) по email
            Users applicant = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("User not found");
                    });

            // Получаем все вакансии этого работодателя
            List<Resumes> myResumes = resumeRepo.findByUser(applicant);

            // Собираем ID вакансий
            List<Integer> resumeIds = myResumes.stream()
                    .map(v -> v.getResumeId().intValue()) // преобразуем Long в Integer
                    .collect(Collectors.toList());

            // Получаем все заявки на эти вакансии
            List<Jobinvitations> invitations = repository.findByResumeIdIn(resumeIds);
            logger.debug("Табылған шақырулар саны: {}", invitations.size());

            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            logger.error("Түйіндемелерге шақыруларды алу кезінде қате шықты: applicantEmail={}", username, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/count/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> getApplicationsCount(@PathVariable Integer resumeId) {
        try {
            long count = repository.countByResumeId(resumeId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }


    public static class InvitationRequest {
        private Integer resumeId;
        private Integer vacancyId;

        // геттеры и сеттеры
        public Integer getResumeId() {
            return resumeId;
        }

        public void setResumeId(Integer resumeId) {
            this.resumeId = resumeId;
        }

        public Integer getVacancyId() {
            return vacancyId;
        }

        public void setVacancyId(Integer vacancyId) {
            this.vacancyId = vacancyId;
        }
    }
}
