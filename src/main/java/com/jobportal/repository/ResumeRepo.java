package com.jobportal.repository;

import com.jobportal.model.Resumes;
import com.jobportal.model.Users;
import com.jobportal.model.Vacancies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResumeRepo extends JpaRepository<Resumes, Long> {
    List<Resumes> findByUserAndIsDeletedFalse(Users user);
    List<Resumes> findByUserAndIsDeletedTrue(Users user);
    List<Resumes> findByIsDeleted(boolean isDeleted);
    List<Resumes> findByUser(Users user);
}
