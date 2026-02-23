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

import com.jobportal.model.Users;

@Controller
@RequestMapping("/invitations")
public class JobinvitationsController {

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
        Users user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Jobinvitations invitation = new Jobinvitations();
        invitation.setResumeId(request.getResumeId());
        invitation.setVacancyId(request.getVacancyId());
        invitation.setEmployerId(user.getUserId().intValue());
        invitation.setStatus("sent");
        invitation.setSentAt(LocalDateTime.now());

        repository.save(invitation);

        return ResponseEntity.ok("Invitation sent");
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyInvitation(Authentication authentication) {
        try {
            String username = authentication.getName();

            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            java.util.List<Jobinvitations> invitations = repository.findByEmployerId(user.getUserId().intValue());

            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> getInvitationsForMyResumes(Authentication authentication) {
        try {
            // Берём email текущего пользователя
            String username = authentication.getName();

            // Находим пользователя (работодателя) по email
            Users applicant = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Получаем все вакансии этого работодателя
            List<Resumes> myResumes = resumeRepo.findByUser(applicant);

            // Собираем ID вакансий
            List<Integer> resumeIds = myResumes.stream()
                    .map(v -> v.getResumeId().intValue()) // преобразуем Long в Integer
                    .collect(Collectors.toList());

            // Получаем все заявки на эти вакансии
            List<Jobinvitations> invitations = repository.findByResumeIdIn(resumeIds);

            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            e.printStackTrace();
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
