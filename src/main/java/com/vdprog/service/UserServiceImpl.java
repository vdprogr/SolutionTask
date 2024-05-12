package com.vdprog.service;

import com.vdprog.exception.UserNotFoundException;
import com.vdprog.exception.WrongAgeException;
import com.vdprog.exception.WrongDateRangeException;
import com.vdprog.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static Integer nextId = 1;

    public void setUsers(List<User> users) {
        this.users = users;
    }

    private List<User> users = new ArrayList<>();
    @Autowired
    Validator validator;
    @Value("${minimumAge}")
    private Integer minimumAge;

    @Override
    public List<User> getAll() {
        return users;
    }

    @Override
    public User getUserById(Integer id) {
        Optional<User> user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
        return user.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(User user) {
        log.info("creating new user");
        if (LocalDate.now().minusYears(user.getBirthDate().getYear()).getYear() < minimumAge) {
            throw new WrongAgeException("Users must be at least 18 years old");
        }
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    @Override
    public User updateUser(Integer id, User updatedUser) {
        log.info("Start updating user with id {}", id);
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        int index = users.indexOf(user);

        if (LocalDate.now().minusYears(user.getBirthDate().getYear()).getYear() < minimumAge) {
            throw new WrongAgeException("Users must be at least 18 years old");
        }

        updatedUser.setId(user.getId());
        users.set(index, updatedUser);
        log.info("User id {} updated successfully", id);
        return updatedUser;
    }

    @Override
    public User patchUser(Integer id, User updatedUser) {
        log.info("Patching user id {} ", id);
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        int index = users.indexOf(user);

        User preparedUser = new User(user);

        if (updatedUser.getEmail() != null) {
            preparedUser.setEmail(updatedUser.getEmail()); }
        if (updatedUser.getFirstName() != null) {
            preparedUser.setFirstName(updatedUser.getFirstName());}
        if (updatedUser.getLastName() != null) {
            preparedUser.setLastName(updatedUser.getLastName());}
        if (updatedUser.getBirthDate() != null) {
            if (LocalDate.now().minusYears(updatedUser.getBirthDate().getYear()).getYear() < minimumAge) {
                throw new WrongAgeException("Users must be at least 18 years old");
            }
            preparedUser.setBirthDate(updatedUser.getBirthDate());}
        if (updatedUser.getAddress() != null) {
            preparedUser.setAddress(updatedUser.getAddress());}
        if (updatedUser.getPhoneNumber() != null) {
            preparedUser.setPhoneNumber(updatedUser.getPhoneNumber());}

        Set<ConstraintViolation<User>> violations = validator.validate(preparedUser);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        users.set(index, preparedUser);
        log.info("User with id {} patched successfully", id);
        return preparedUser;
    }

    @Override
    public String deleteUser(Integer id) {
        log.info("Start process deleting user with id {}", id);
        if (!users.removeIf(user -> user.getId().equals(id))) throw new UserNotFoundException("User not found with id: " + id);
        log.info("Deleted successfully");
        return "User deleted successfully.";
    }

    @Override
    public List<User> searchUsersByBirthDateRange(LocalDate from, LocalDate to) throws RuntimeException {
        log.info(from.toString());
        log.info(to.toString());

        if (from.isAfter(to)) {
            throw new WrongDateRangeException("Swap the dates. First must be less than equal to second date");
        }

        return users.stream()
                .filter(user -> (user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to)))
                .sorted(Comparator.comparing(User::getBirthDate))
                .collect(Collectors.toList());
    }

}