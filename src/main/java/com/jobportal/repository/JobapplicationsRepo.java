package com.jobportal.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jobportal.model.Jobapplications;
import java.util.List;

@Repository
public interface JobapplicationsRepo extends JpaRepository<Jobapplications, Integer> {
    List<Jobapplications> findByApplicantId(Integer applicantId);
    List<Jobapplications> findByVacancyIdIn(List<Integer> vacancyIds);
}
