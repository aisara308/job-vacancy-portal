package com.jobportal.controller;

import com.jobportal.model.Vacancies;
import com.jobportal.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class VacancyController {

    @Autowired
    private VacancyService vacancyService;

    @GetMapping("/vacancies")
    public String getVacancies(Model model) {
        List<Vacancies> vacancies = vacancyService.getAllActiveVacancies();
        model.addAttribute("vacancies", vacancies);
        return "home";
    }
}
