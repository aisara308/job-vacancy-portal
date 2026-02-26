package com.jobportal.model;

import com.jobportal.model.Users;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    @Test
    void shouldReturnTrueWhenCodeValidAndNotExpired() {
        Users user = new Users();
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(5));

        assertTrue(user.isResetCodeValid("123456"));
    }

    @Test
    void shouldReturnFalseWhenCodeDoesNotMatch() {
        Users user = new Users();
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(5));

        assertFalse(user.isResetCodeValid("000000"));
    }

    @Test
    void shouldReturnFalseWhenExpired() {
        Users user = new Users();
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().minusMinutes(1));

        assertFalse(user.isResetCodeValid("123456"));
    }

    @Test
    void shouldReturnFalseWhenCodeNull() {
        Users user = new Users();
        assertFalse(user.isResetCodeValid("123456"));
    }
}