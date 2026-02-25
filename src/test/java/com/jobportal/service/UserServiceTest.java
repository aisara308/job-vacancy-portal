package com.jobportal.service;

import com.jobportal.model.Users;
import com.jobportal.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepo repo;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserAndReturnToken() {
        Users user = new Users("Test", "test@mail.com", "password", "USER", null);

        when(repo.save(any())).thenReturn(user);
        when(jwtService.generateToken("test@mail.com")).thenReturn("jwt-token");

        String token = service.register(user);

        assertEquals("jwt-token", token);
        verify(repo).save(any());
    }

    @Test
    void shouldReturnUserNotFound() {
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.empty());

        Users user = new Users();
        user.setEmail("test@mail.com");

        assertEquals("user_not_found", service.verify(user));
    }

    @Test
    void shouldReturnWrongPassword() {
        Users dbUser = new Users();
        dbUser.setEmail("test@mail.com");

        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(dbUser));
        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad"));

        Users loginUser = new Users();
        loginUser.setEmail("test@mail.com");
        loginUser.setPasswordHash("wrong");

        assertEquals("wrong_password", service.verify(loginUser));
    }

    @Test
    void shouldGenerateAndSendResetCode() {
        Users user = new Users();
        user.setEmail("test@mail.com");

        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        boolean result = service.generateAndSendResetCode("test@mail.com");

        assertTrue(result);
        assertNotNull(user.getResetCode());
        assertNotNull(user.getResetCodeExpiry());

        verify(repo).save(user);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldUpdatePasswordAndClearResetCode() {
        Users user = new Users();
        user.setEmail("test@mail.com");
        user.setResetCode("123456");

        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        service.updatePassword("test@mail.com", "newPassword");

        assertNull(user.getResetCode());
        assertNull(user.getResetCodeExpiry());
        verify(repo).save(user);
    }
}