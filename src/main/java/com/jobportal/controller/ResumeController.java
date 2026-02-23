package com.jobportal.controller;

import com.jobportal.model.*;
import com.jobportal.repository.ResumeRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.repository.JobinvitationsRepo;
import com.jobportal.service.ResumeService;
import com.jobportal.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private ResumeRepo resumeRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JobinvitationsRepo jobinvitationsRepo;

    @Autowired
    private ResumeService resumeService;

    @GetMapping("/resumes")
    public String getResumes(Model model) {
        List<Resumes> resumes = resumeService.getAllActiveResumes();
        model.addAttribute("resumes", resumes);
        return "homeem";
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("resume", new Resumes());
        return "resume";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addResume(@RequestBody ResumeRequest resumeData) {
        try {
            Users user = userRepo.findById(resumeData.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пайдаланушы табылмады"));

            Resumes resume = new Resumes();
            resume.setUser(user);
            resume.setTitle(resumeData.getTitle());
            resume.setEducation(resumeData.getEducation());
            resume.setExperience(resumeData.getExperience());
            resume.setLocation(resumeData.getLocation());
            resume.setSchedule(resumeData.getSchedule());
            resume.setPaymentTime(resumeData.getPaymentTime());
            resume.setSkills(resumeData.getSkills());
            resume.setSalaryExpectation(resumeData.getSalaryExpectation());
            resume.setDeleted(false);

            resumeRepo.save(resume);

            return ResponseEntity.ok("Резюме сәтті қосылды!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }
    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserResumes(@PathVariable Long userId) {
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пайдаланушы табылмады"));
            java.util.List<Resumes> resumes = resumeRepo.findByUserAndIsDeletedFalse(user);

            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @PutMapping("/delete/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> deleteResume(@PathVariable Long resumeId) {
        Resumes resume = resumeRepo.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Резюме табылмады"));
        resume.setDeleted(true);
        resumeRepo.save(resume);
        return ResponseEntity.ok("Резюме корзинаға жіберілді");
    }

    @GetMapping("/trash/{userId}")
    @ResponseBody
    public ResponseEntity<?> getDeletedResumes(@PathVariable Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пайдаланушы табылмады"));
        return ResponseEntity.ok(resumeRepo.findByUserAndIsDeletedTrue(user));
    }

    @PutMapping("/restore/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> restoreResume(@PathVariable Long resumeId) {
        Resumes resume = resumeRepo.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Резюме табылмады"));
        resume.setDeleted(false);
        resumeRepo.save(resume);
        return ResponseEntity.ok("Резюме қалпына келтірілді");
    }

    @PutMapping("/update/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> editResume(@PathVariable Long resumeId, @RequestBody ResumeRequest updatedData) {
        try {
            Resumes resume = resumeRepo.findById(resumeId)
                    .orElseThrow(() -> new RuntimeException("Резюме табылмады"));

            if (updatedData.getTitle() != null) resume.setTitle(updatedData.getTitle());
            if (updatedData.getEducation() != null) resume.setEducation(updatedData.getEducation());
            if (updatedData.getExperience() != null) resume.setExperience(updatedData.getExperience());
            if (updatedData.getLocation() != null) resume.setLocation(updatedData.getLocation());
            if (updatedData.getSchedule() != null) resume.setSchedule(updatedData.getSchedule());
            if (updatedData.getPaymentTime() != null) resume.setPaymentTime(updatedData.getPaymentTime());
            if (updatedData.getSkills() != null) resume.setSkills(updatedData.getSkills());
            if (updatedData.getSalaryExpectation() != null) resume.setSalaryExpectation(updatedData.getSalaryExpectation());

            resumeRepo.save(resume);

            return ResponseEntity.ok("Резюме сәтті жаңартылды!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/update/{resumeId}")
    public String editResumePage(@PathVariable Long resumeId, Model model) {
        Resumes resume = resumeRepo.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Резюме табылмады"));
        model.addAttribute("resume", resume);
        return "resume-edit";
    }

    @GetMapping("/get/{resumeId}")
    @ResponseBody
    public Resumes getResume(@PathVariable Long resumeId) {
        return resumeRepo.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Резюме табылмады"));
    }

    public static class ResumeRequest {
        private Long userId;
        private String title;
        private String education;
        private String experience;
        private String location;
        private String schedule;
        private String paymentTime;
        private String skills;
        private java.math.BigDecimal salaryExpectation;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }

        public String getPaymentTime() { return paymentTime; }
        public void setPaymentTime(String paymentTime) { this.paymentTime = paymentTime; }

        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }

        public java.math.BigDecimal getSalaryExpectation() { return salaryExpectation; }
        public void setSalaryExpectation(java.math.BigDecimal salaryExpectation) { this.salaryExpectation = salaryExpectation; }
    }
}
