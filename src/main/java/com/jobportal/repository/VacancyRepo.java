package com.jobportal.repository;

import com.jobportal.model.Vacancies;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VacancyRepo extends JpaRepository<Vacancies, Long> {
    List<Vacancies> findByStatus(String status);
}
