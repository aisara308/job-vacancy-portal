package com.jobportal.controller;

import com.jobportal.model.Resumes;
import com.jobportal.model.Users;
import com.jobportal.repository.ResumeRepo;
import com.jobportal.repository.UserRepo;
import com.jobportal.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    @Mock
    private ResumeRepo resumeRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    @Test
    void deleteResume_shouldMarkAsDeleted() {
        Resumes resume = new Resumes();
        resume.setResumeId(1L);
        resume.setDeleted(false);

        when(resumeRepo.findById(1L)).thenReturn(Optional.of(resume));

        ResponseEntity<?> response = resumeController.deleteResume(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(resume.getDeleted());

        verify(resumeRepo).save(resume);
    }

    @Test
    void restoreResume_shouldSetDeletedFalse() {
        Resumes resume = new Resumes();
        resume.setResumeId(1L);
        resume.setDeleted(true);

        when(resumeRepo.findById(1L)).thenReturn(Optional.of(resume));

        ResponseEntity<?> response = resumeController.restoreResume(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(resume.getDeleted());

        verify(resumeRepo).save(resume);
    }

    @Test
    void addResume_shouldReturnErrorIfUserNotFound() {
        ResumeController.ResumeRequest request = new ResumeController.ResumeRequest();
        request.setUserId(99L);

        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = resumeController.addResume(request);

        assertEquals(500, response.getStatusCodeValue());
    }
}