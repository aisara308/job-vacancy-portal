package com.jobportal.service;

import com.jobportal.model.Vacancies;
import com.jobportal.repository.VacancyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacancyService {

    @Autowired
    private VacancyRepo vacancyRepo;

    public List<Vacancies> getAllActiveVacancies() {
        return vacancyRepo.findByStatus("active");
    }
}
