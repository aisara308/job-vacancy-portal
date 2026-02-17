package com.jobportal.controller;

import com.jobportal.model.Resumes;
import com.jobportal.model.Users;
import com.jobportal.model.Vacancies;
import com.jobportal.repository.ResumeRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.repository.VacancyRepo;
import com.jobportal.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/vacancies")
public class VacancyController {

    @Autowired
    private VacancyRepo vacancyRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private VacancyService vacancyService;

    @GetMapping("/vacancies")
    public String getVacancies(Model model) {
        List<Vacancies> vacancies = vacancyService.getAllActiveVacancies();
        model.addAttribute("vacancies", vacancies);
        return "home";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addVacancy(@RequestBody VacancyController.VacancyRequest vacancyData) {
        try {
            // Найдем пользователя по ID (который прислал фронт)
            Users employer = userRepo.findById(vacancyData.getEmployerId())
                    .orElseThrow(() -> new RuntimeException("Пайдаланушы табылмады"));

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

            return ResponseEntity.ok("Вакансия сәтті қосылды!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Қате: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserVacancies(@PathVariable Long userId) {
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пайдаланушы табылмады"));

            // Ищем вакансии по employerId
            List<Vacancies> vacancies = vacancyRepo.findByEmployerId(user.getUserId().intValue());

            return ResponseEntity.ok(vacancies);
        } catch (Exception e) {
            e.printStackTrace();
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
