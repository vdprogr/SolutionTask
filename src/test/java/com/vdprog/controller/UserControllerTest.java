package com.vdprog.controller;
import java.time.LocalDate;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdprog.exception.UserNotFoundException;
import com.vdprog.exception.WrongAgeException;
import com.vdprog.model.User;
import com.vdprog.service.UserService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private static final Integer ID = 1;
    private static final String EMAIL = "test@example.com";
    private static final String FIRST_NAME = "Jim";
    private static final String LAST_NAME = "Karry";
    private static final String ADDRESS = "New York";
    private static final String PHONE_NUMBER = "1234567890";
    private static final String USER_API_PATTERN = "/users";
    private static final String SEARCH_API = "/search";
    private static final String DELIMITER = "/";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate FROM_DATE = LocalDate.of(1990, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2000, 1, 1);

    @Value("${minimumAge}")
    private Integer MIN_AGE;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testCreateUserSuccess() throws Exception {
        User expectedUser = createTestUser();

        String json = objectMapper.writeValueAsString(expectedUser);

        when(userService.createUser(any(User.class))).thenReturn(expectedUser);

        MvcResult mvcResult = mockMvc.perform(post(USER_API_PATTERN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        User actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(),
                User.class
        );

        assertEquals(expectedUser, actual);
    }

    @Test
    void testCreateUserValidation() throws Exception {
        mockMvc.perform(post(USER_API_PATTERN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateUserWrongAgeException() throws Exception {
        User expectedUser = createTestUser();
        String json = objectMapper.writeValueAsString(expectedUser);

        when(userService.createUser(any(User.class))).thenThrow(new WrongAgeException(
                String.format("Users must be at least {} years old", MIN_AGE)));

        mockMvc.perform(post(USER_API_PATTERN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(String.format("Users must be at least {} years old", MIN_AGE)));
    }

    @Test
    public void testDeleteSuccess()throws Exception {
        when(userService.deleteUser(ID)).thenReturn("User deleted successfully.");

        mockMvc.perform(delete(USER_API_PATTERN + DELIMITER + ID))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));

        verify(userService, times(1)).deleteUser(anyInt());
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        when(userService.deleteUser(ID)).thenThrow(
                new UserNotFoundException(String.format("User not found with id: {}", ID)));

        mockMvc.perform(delete(USER_API_PATTERN + DELIMITER + ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User not found with id: {}", ID)));
    }

    @Test
    public void testSearchUsersByBirthDateRangeSuccess() throws Exception {
        when(userService.searchUsersByBirthDateRange(FROM_DATE, TO_DATE))
                .thenReturn(Arrays.asList(createTestUser()));

        mockMvc.perform(get(USER_API_PATTERN + SEARCH_API)
                        .param("from", FROM_DATE.toString())
                        .param("to", TO_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$[0].lastName").value(LAST_NAME));

    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        User updatedUser = createTestUser();

        when(userService.updateUser(ID, updatedUser)).thenReturn(updatedUser);

        String json = objectMapper.writeValueAsString(updatedUser);

        mockMvc.perform(put(USER_API_PATTERN + DELIMITER + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.address").value(ADDRESS))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_NUMBER))
                .andExpect(jsonPath("$.birthDate").value(BIRTH_DATE.toString()));
    }
    @Test
    public void testUpdateUserUserNotFoundException() throws Exception {
        User updateUser = createTestUser();

        String json = objectMapper.writeValueAsString(updateUser);

        when(userService.updateUser(ID, updateUser)).thenThrow(
                new UserNotFoundException(String.format("User not found with id: {}", ID)));

        mockMvc.perform(put(USER_API_PATTERN + DELIMITER + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User not found with id: {}", ID)));
    }

    private User createTestUser() {
        return User.builder()
                .Id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .address(ADDRESS)
                .phoneNumber(PHONE_NUMBER)
                .birthDate(BIRTH_DATE)
                .build();
    }
}