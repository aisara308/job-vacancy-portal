package com.jobportal.repository;

import com.jobportal.model.Jobapplications;
import com.jobportal.model.Jobinvitations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobinvitationsRepo extends JpaRepository<Jobinvitations, Integer> {
    List<Jobinvitations> findByEmployerId(Integer employerId);
    List<Jobinvitations> findByResumeIdIn(List<Integer> resumeIds);
}
