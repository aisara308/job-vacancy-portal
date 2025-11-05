package com.jobportal.controller;

import com.jobportal.model.Vacancies;
import com.jobportal.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private VacancyService vacancyService;

    @GetMapping("/home")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        String username = authentication.getName();

        List<Vacancies> vacancies = vacancyService.getAllActiveVacancies();

        return ResponseEntity.ok(Map.of(
                "username", username,
                "vacancies", vacancies
        ));
    }
}
