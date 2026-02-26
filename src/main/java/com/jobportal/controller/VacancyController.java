package com.jobportal.controller;

import com.jobportal.model.Jobapplications;
import com.jobportal.model.Resumes;
import com.jobportal.model.Users;
import com.jobportal.model.Vacancies;
import com.jobportal.repository.JobapplicationsRepo;
import com.jobportal.repository.ResumeRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.repository.VacancyRepo;
import com.jobportal.service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vacancies")
public class VacancyController {

    private static final Logger logger = LoggerFactory.getLogger(VacancyController.class);

    @Autowired
    private VacancyRepo vacancyRepo;

    @Autowired
    private ResumeRepo resumeRepo;

    @Autowired
    private JobapplicationsRepo jobapplicationsRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private VacancyService vacancyService;

    @GetMapping("/vacancies")
    public String getVacancies(Model model) {
        logger.info("Барлық активті вакансиялар сұралды");
        List<Vacancies> vacancies = vacancyService.getAllActiveVacancies();
        logger.debug("Активті вакансиялар саны: {}", vacancies.size());
        model.addAttribute("vacancies", vacancies);
        return "home";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addVacancy(@RequestBody VacancyController.VacancyRequest vacancyData) {
        logger.info("Жаңа вакансия қосу әрекеті: employerId={}", vacancyData.getEmployerId());
        logger.debug("Вакансия деректері: {}", vacancyData);
        try {
            // Найдем пользователя по ID (который прислал фронт)
            Users employer = userRepo.findById(vacancyData.getEmployerId())
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: employerId={}", vacancyData.getEmployerId());
                        return new RuntimeException("Пайдаланушы табылмады");
                    });

            Vacancies vacancy = new Vacancies();
            vacancy.setEmployerId(employer.getUserId().intValue()); // Преобразуем Long в Integer, если нужно
            vacancy.setTitle(vacancyData.getTitle());
            vacancy.setSalaryFrom(vacancyData.getSalaryFrom());
            vacancy.setSalaryTo(vacancyData.getSalaryTo());
            vacancy.setExperience(vacancyData.getExperience());
            vacancy.setLocation(vacancyData.getLocation());
            vacancy.setAdres(vacancyData.getAdres());
            vacancy.setSchedule(vacancyData.getSchedule());
            vacancy.setPaymentTime(vacancyData.getPaymentTime());
            vacancy.setRemote(vacancyData.getRemote() != null ? vacancyData.getRemote() : false);
            vacancy.setForStudents(vacancyData.getForStudents() != null ? vacancyData.getForStudents() : false);
            vacancy.setRequirements(vacancyData.getRequirements());
            vacancy.setCreatedAt(LocalDateTime.now());
            vacancy.setStatus("active");

            vacancyRepo.save(vacancy);
            logger.info("Вакансия сәтті қосылды: vacancyId={}", vacancy.getVacancyId());
            return ResponseEntity.ok("Вакансия сәтті қосылды!");
        } catch (Exception e) {
            logger.error("Вакансия қосу кезінде қате шықты: employerId={}", vacancyData.getEmployerId(), e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/{vacancyId}")
    @ResponseBody
    public ResponseEntity<?> getVacancyById(@PathVariable Long vacancyId) {
        logger.info("Вакансия сұралды: vacancyId={}", vacancyId);
        try {
            Vacancies vacancy = vacancyRepo.findById(vacancyId)
                    .orElseThrow(() -> {
                        logger.warn("Вакансия табылмады: vacancyId={}", vacancyId);
                        return new RuntimeException("Вакансия табылмады");
                    });
            return ResponseEntity.ok(vacancy);
        } catch (Exception e) {
            logger.error("Вакансияны алу кезінде қате шықты: vacancyId={}", vacancyId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/applications/my")
    @ResponseBody
    public ResponseEntity<?> getApplicationsForMyVacancies(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Жұмысқа өтінімдер сұралды: employerEmail={}", username);
        try {

            // Находим пользователя (работодателя) по email
            Users employer = userRepo.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: email={}", username);
                        return new RuntimeException("Пайдаланушы табылмады");
                    });

            // Получаем все вакансии этого работодателя
            List<Vacancies> myVacancies = vacancyRepo.findByEmployerId(employer.getUserId().intValue());
            logger.debug("Ешбір вакансия табылмады: {}", myVacancies.isEmpty());

            // Собираем ID вакансий
            List<Integer> vacancyIds = myVacancies.stream()
                    .map(v -> v.getVacancyId().intValue()) // преобразуем Long в Integer
                    .collect(Collectors.toList());

            // Получаем все заявки на эти вакансии
            List<Jobapplications> applications = jobapplicationsRepo.findByVacancyIdIn(vacancyIds);
            logger.info("Табылған өтінімдер саны: {}", applications.size());

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("Өтінімдерді алу кезінде қате шықты: employerEmail={}", username, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }


    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserVacancies(@PathVariable Long userId) {
        logger.info("Пайдаланушының вакансиялары сұралды: userId={}", userId);
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("Пайдаланушы табылмады: userId={}", userId);
                        return new RuntimeException("Пайдаланушы табылмады");
                    });

            // Ищем вакансии по employerId
            List<Vacancies> vacancies = vacancyRepo.findByEmployerId(user.getUserId().intValue());
            logger.debug("Табылған вакансиялар саны: {}", vacancies.size());
            return ResponseEntity.ok(vacancies);
        } catch (Exception e) {
            logger.error("Вакансияларды алу кезінде қате шықты: userId={}", userId, e);
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    // Класс для JSON запроса
    public static class VacancyRequest {
        private Long employerId;
        private String title;
        private BigDecimal salaryFrom;
        private BigDecimal salaryTo;
        private String experience;
        private String location;
        private String adres;
        private String schedule;
        private String paymentTime;
        private Boolean remote;
        private Boolean forStudents;
        private String requirements;

        // Геттеры и сеттеры для всех полей
        public Long getEmployerId() { return employerId; }
        public void setEmployerId(Long employerId) { this.employerId = employerId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public BigDecimal getSalaryFrom() { return salaryFrom; }
        public void setSalaryFrom(BigDecimal salaryFrom) { this.salaryFrom = salaryFrom; }

        public BigDecimal getSalaryTo() { return salaryTo; }
        public void setSalaryTo(BigDecimal salaryTo) { this.salaryTo = salaryTo; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getAdres() { return adres; }
        public void setAdres(String adres) { this.adres = adres; }

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }

        public String getPaymentTime() { return paymentTime; }
        public void setPaymentTime(String paymentTime) { this.paymentTime = paymentTime; }

        public Boolean getRemote() { return remote; }
        public void setRemote(Boolean remote) { this.remote = remote; }

        public Boolean getForStudents() { return forStudents; }
        public void setForStudents(Boolean forStudents) { this.forStudents = forStudents; }

        public String getRequirements() { return requirements; }
        public void setRequirements(String requirements) { this.requirements = requirements; }
    }


}
