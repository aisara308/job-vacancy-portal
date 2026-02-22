package com.jobportal.controller;

import com.jobportal.model.Jobinvitations;

import com.jobportal.repository.JobinvitationsRepo;
import com.jobportal.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import com.jobportal.model.Users;

@Controller
@RequestMapping("/invitations")
public class JobinvitationsController {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JobinvitationsRepo repository;

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
