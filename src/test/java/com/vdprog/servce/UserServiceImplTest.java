package com.vdprog.servce;

import static org.junit.jupiter.api.Assertions.*;

import com.vdprog.exception.UserNotFoundException;
import com.vdprog.exception.WrongAgeException;
import com.vdprog.exception.WrongDateRangeException;
import com.vdprog.model.User;
import com.vdprog.service.UserServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceImplTest {
    @Autowired
    private UserServiceImpl userService;

    @Value("${minimumAge}")
    private Integer minimumAge;

    private static final String EMAIL = "test@example.com";
    private static final String FIRST_NAME = "Jim";
    private static final String LAST_NAME = "Karry";
    private static final String ADDRESS = "New York";
    private static final String PHONE_NUMBER = "1234567890";
    private static final String USER_API_PATTERN = "/users";

    @Test
    public void testGetUserByIdUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(100));
    }

    @Test
    void testCreateUserSuccess() {
        LocalDate allowedAge = LocalDate.now().minusYears(minimumAge);

        User createdUser = userService.createUser(createTestUser());

        assertTrue(userService.getAll().contains(createdUser));
    }

    @Test
    void testCreateUserWrongAgeException() {
        User user = new User(null, EMAIL, FIRST_NAME, LAST_NAME, LocalDate.now(), ADDRESS, PHONE_NUMBER);
        assertThrows(WrongAgeException.class, () -> userService.createUser(user));
    }

    @Test
    void testUpdateUserSuccess() {
        User createdUser = userService.createUser(createTestUser());

        User updatedUser = new User(null, "updated"+EMAIL, "Updated"+FIRST_NAME, "Updated"+LAST_NAME,
                createdUser.getBirthDate(),null, null);

        User result = userService.updateUser(createdUser.getId(), updatedUser);

        // Then
        assertEquals(result.getId(), updatedUser.getId());
        assertEquals(result.getEmail(), updatedUser.getEmail());
        assertEquals(result.getFirstName(), updatedUser.getFirstName());
        assertEquals(result.getLastName(), updatedUser.getLastName());
        assertEquals(result.getBirthDate(), updatedUser.getBirthDate());
        assertEquals(result.getAddress(), updatedUser.getAddress());
        assertEquals(result.getPhoneNumber(), updatedUser.getPhoneNumber());
    }

    @Test
    void testUpdateUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(100, new User()));
    }

    @Test
    public void testPatchValidationException() {

        User createdUser = userService.createUser(createTestUser());
        User updatedUser = new User(null, "updatedexamplecom", null, null,
                null,null, null);

        assertThrows(ConstraintViolationException.class, () -> userService.patchUser(createdUser.getId(), updatedUser));
    }

    @Test
    public void testPatchUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.patchUser(100, new User()));
    }

    @Test
    public void testPatchWrongAgeValidation() {

        User createdUser = userService.createUser(createTestUser());
        User updatedUser = new User(null, "updated"+EMAIL, "Updated"+FIRST_NAME, "Updated"+LAST_NAME,
                LocalDate.now(),null, null);

        assertThrows(WrongAgeException.class, () -> userService.patchUser(createdUser.getId(), updatedUser));
    }

    @Test
    void testDeleteUserSuccess() {
        User createdUser = userService.createUser(createTestUser());
        String result = userService.deleteUser(createdUser.getId());

        assertEquals("User deleted successfully.", result);
        assertFalse(userService.getAll().contains(createdUser));
    }

    @Test
    public void testDeleteUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(100));
    }

    @Test
    void testSearchUsersByBirthDateRangeSuccess() {

        LocalDate from = LocalDate.of(1984, 1, 1);
        LocalDate to = LocalDate.of(1999, 12, 31);

        User user1 = new User(null, "test1@example.com", "John", "Doe", LocalDate.of(1985, 5, 15), null, null);
        User user2 = new User(null, "test2@example.com", "Jane", "Smith", LocalDate.of(1982, 8, 20), null, null);
        User user3 = new User(null, "test3@example.com", "Alice", "Johnson", LocalDate.of(1988, 3, 10),null, null);

        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        List<User> result = userService.searchUsersByBirthDateRange(from, to);

        assertEquals(2, result.size()); // Expecting two users within the specified date range
        assertFalse(result.stream().allMatch(user -> user2.getBirthDate().isAfter(from) && user2.getBirthDate().isBefore(to)));
        assertTrue(result.get(0).getBirthDate().isBefore(result.get(1).getBirthDate()));
    }

    @Test
    void testSearchUsersByBirthDateRangeWrongDateRangeException() {
        LocalDate from = LocalDate.of(2000, 12, 31); // 'from' date is after 'to' date
        LocalDate to = LocalDate.of(1990, 1, 1);

        assertThrows(WrongDateRangeException.class, () -> userService.searchUsersByBirthDateRange(from, to));
    }

    private User createTestUser() {
        return User.builder()
                .Id(null)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .address(ADDRESS)
                .phoneNumber(PHONE_NUMBER)
                .birthDate(LocalDate.now().minusYears(minimumAge))
                .build();
    }
}