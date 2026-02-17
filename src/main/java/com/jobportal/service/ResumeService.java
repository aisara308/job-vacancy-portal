package com.jobportal.service;

import com.jobportal.model.Resumes;
import com.jobportal.repository.ResumeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeService {
    @Autowired
    private ResumeRepo resumeRepo;

    public List<Resumes> getAllActiveResumes() {
        return resumeRepo.findByIsDeleted(false);
    }
}
