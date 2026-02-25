package com.jobportal.controller;

import com.jobportal.model.*;
import com.jobportal.repository.ResumeRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.repository.JobinvitationsRepo;
import com.jobportal.service.ResumeService;
import com.jobportal.service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

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
        logger.info("Барлық активті резюмелер сұралды");
        List<Resumes> resumes = resumeService.getAllActiveResumes();
        logger.debug("Активті резюмелер саны: {}", resumes.size());
        model.addAttribute("resumes", resumes);
        return "homeem";
    }

    @GetMapping
    public String showForm(Model model) {
        logger.info("Резюме қосу формасы көрсетілуде");
        model.addAttribute("resume", new Resumes());
        return "resume";
    }


    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addResume(@RequestBody ResumeRequest resumeData) {
        logger.info("Жаңа резюме қосу әрекеті: userId={}", resumeData.getUserId());
        logger.debug("Резюме деректері: {}", resumeData);
        try {
            Users user = userRepo.findById(resumeData.getUserId())
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: userId={}", resumeData.getUserId());
                        return new RuntimeException("Пайдаланушы табылмады");
                    });

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
            logger.info("Резюме сәтті қосылды: resumeId={}", resume.getResumeId());
            return ResponseEntity.ok("Резюме сәтті қосылды!");
        } catch (Exception e) {
            logger.error("Резюме қосу кезінде қате шықты: userId={}", resumeData.getUserId(), e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }
    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserResumes(@PathVariable Long userId) {
        logger.info("Пайдаланушының резюмелері сұралды: userId={}", userId);
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: userId={}", userId);
                        return new RuntimeException("Пайдаланушы табылмады");
                    });
            List<Resumes> resumes = resumeRepo.findByUserAndIsDeletedFalse(user);
            logger.debug("Табылған резюмелер саны: {}", resumes.size());
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            logger.error("Резюмелерді алу кезінде қате шықты: userId={}", userId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @PutMapping("/delete/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> deleteResume(@PathVariable Long resumeId) {
        logger.info("Резюме корзинаға жіберілуде: resumeId={}", resumeId);
        try{
            Resumes resume = resumeRepo.findById(resumeId)
                    .orElseThrow(() -> {
                        logger.warn("Резюме табылмады: resumeId={}", resumeId);
                        return new RuntimeException("Резюме табылмады");
                    });
            resume.setDeleted(true);
            resumeRepo.save(resume);
            logger.info("Резюме сәтті корзинаға жіберілді: resumeId={}", resumeId);
            return ResponseEntity.ok("Резюме корзинаға жіберілді");
        }catch (Exception e) {
            logger.error("Резюмелерді жою кезінде қате шықты: resumeId={}", resumeId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/trash/{userId}")
    @ResponseBody
    public ResponseEntity<?> getDeletedResumes(@PathVariable Long userId) {
        logger.info("Өшірілген резюмелер сұралды: userId={}", userId);
        try{
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: userId={}", userId);
                        return new RuntimeException("Пайдаланушы табылмады");
                    });
            List<Resumes> deleted = resumeRepo.findByUserAndIsDeletedTrue(user);
            logger.debug("Өшірілген резюмелер саны: {}", deleted.size());
            return ResponseEntity.ok(deleted);
        }catch (Exception e) {
            logger.error("Өшірілген резюмелерді алу кезінде қате шықты: userId={}", userId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @PutMapping("/restore/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> restoreResume(@PathVariable Long resumeId) {
        logger.info("Резюме қалпына келтірілуде: resumeId={}", resumeId);
        try{
            Resumes resume = resumeRepo.findById(resumeId)
                    .orElseThrow(() -> {
                        logger.warn("Резюме табылмады: resumeId={}", resumeId);
                        return new RuntimeException("Резюме табылмады");
                    });
            resume.setDeleted(false);
            resumeRepo.save(resume);
            logger.info("Резюме сәтті қалпына келтірілді: resumeId={}", resumeId);
            return ResponseEntity.ok("Резюме қалпына келтірілді");
        }catch (Exception e) {
            logger.error("Резюмелерді қалпына келтіру кезінде қате шықты: resumeId={}", resumeId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @PutMapping("/update/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> editResume(@PathVariable Long resumeId, @RequestBody ResumeRequest updatedData) {
        logger.info("Резюме жаңарту әрекеті: resumeId={}", resumeId);
        logger.debug("Жаңартылатын деректер: {}", updatedData);
        try {
            Resumes resume = resumeRepo.findById(resumeId)
                    .orElseThrow(() -> {
                        logger.warn("Резюме табылмады: resumeId={}", resumeId);
                        return new RuntimeException("Резюме табылмады");
                    });

            if (updatedData.getTitle() != null) resume.setTitle(updatedData.getTitle());
            if (updatedData.getEducation() != null) resume.setEducation(updatedData.getEducation());
            if (updatedData.getExperience() != null) resume.setExperience(updatedData.getExperience());
            if (updatedData.getLocation() != null) resume.setLocation(updatedData.getLocation());
            if (updatedData.getSchedule() != null) resume.setSchedule(updatedData.getSchedule());
            if (updatedData.getPaymentTime() != null) resume.setPaymentTime(updatedData.getPaymentTime());
            if (updatedData.getSkills() != null) resume.setSkills(updatedData.getSkills());
            if (updatedData.getSalaryExpectation() != null) resume.setSalaryExpectation(updatedData.getSalaryExpectation());

            resumeRepo.save(resume);
            logger.info("Резюме сәтті жаңартылды: resumeId={}", resumeId);
            return ResponseEntity.ok("Резюме сәтті жаңартылды!");
        } catch (Exception e) {
            logger.error("Резюмелерді жаңарту кезінде қате шықты: resumeId={}", resumeId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/update/{resumeId}")
    public String editResumePage(@PathVariable Long resumeId, Model model) {
        logger.info("Резюме өңдеу беті ашылды: resumeId={}", resumeId);
        Resumes resume = resumeRepo.findById(resumeId)
                .orElseThrow(() -> {
                    logger.warn("Резюме табылмады: resumeId={}", resumeId);
                    return new RuntimeException("Резюме табылмады");
                });
        model.addAttribute("resume", resume);
        return "resume-edit";
    }

    @GetMapping("/get/{resumeId}")
    @ResponseBody
    public Resumes getResume(@PathVariable Long resumeId) {
        logger.info("Резюме сұралды: resumeId={}", resumeId);
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
