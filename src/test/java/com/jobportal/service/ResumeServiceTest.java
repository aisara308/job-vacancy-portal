package com.jobportal.service;

import com.jobportal.model.Resumes;
import com.jobportal.repository.ResumeRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepo resumeRepo;

    @InjectMocks
    private ResumeService resumeService;

    @Test
    void getAllActiveResumes_shouldReturnOnlyActiveResumes() {
        Resumes r1 = new Resumes();
        r1.setDeleted(false);

        when(resumeRepo.findByIsDeleted(false)).thenReturn(List.of(r1));

        List<Resumes> result = resumeService.getAllActiveResumes();

        assertEquals(1, result.size());
        assertFalse(result.get(0).getDeleted());

        verify(resumeRepo, times(1)).findByIsDeleted(false);
    }
}