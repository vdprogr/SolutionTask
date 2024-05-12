package com.vdprog.service;

import com.vdprog.model.User;

import java.time.LocalDate;
import java.util.List;

public interface UserService {

    List<User> getAll();
    User getUserById(Integer id);
    User createUser(User user);
    User updateUser(Integer id, User updatedUser);
    User patchUser(Integer id, User updatedUser);
    String deleteUser(Integer id);
    List<User> searchUsersByBirthDateRange(LocalDate from, LocalDate to);
}