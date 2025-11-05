package com.jobportal.repository;

import com.jobportal.model.Resumes;
import com.jobportal.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepo extends JpaRepository<Resumes, Long> {
    List<Resumes> findByUserAndIsDeletedFalse(Users user);
    List<Resumes> findByUserAndIsDeletedTrue(Users user);
}
